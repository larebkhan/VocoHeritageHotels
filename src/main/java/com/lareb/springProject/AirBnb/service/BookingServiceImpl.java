package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.Strategy.PricingService;
import com.lareb.springProject.AirBnb.dto.BookingDto;
import com.lareb.springProject.AirBnb.dto.BookingRequest;
import com.lareb.springProject.AirBnb.dto.GuestDto;
import com.lareb.springProject.AirBnb.entity.*;
import com.lareb.springProject.AirBnb.entity.enums.BookingStatus;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.exception.UnAuthorizedException;
import com.lareb.springProject.AirBnb.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements  BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;

    private final PricingService pricingService;
    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initializing booking for hotel: {} , rooms: {}, date: {}-{}",
                bookingRequest.getHotelId(),
                bookingRequest.getRooms(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + bookingRequest.getHotelId()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalRooms = 0;
        Set<BookedRoom> bookedRooms = new HashSet<>();

        for (BookingRequest.RoomBookingRequest r : bookingRequest.getRooms()) {
            Room room = roomRepository.findById(r.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found for ID: " + r.getRoomId()));

            List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                    r.getRoomId(),
                    bookingRequest.getCheckInDate(),
                    bookingRequest.getCheckOutDate(),
                    r.getRoomsCount()
            );

            long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

            if (inventoryList.size() != daysCount) {
                throw new IllegalStateException("Room is not available anymore");
            }

            inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), r.getRoomsCount());

            BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
            BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(r.getRoomsCount()));
            grandTotal = grandTotal.add(totalPrice);

            BookedRoom bookedRoom = BookedRoom.builder()
                    .room(room)
                    .roomsCount(r.getRoomsCount())
                    .pricePerRoom(priceForOneRoom)
                    .totalPrice(totalPrice)
                    .booking(booking)
                    .build();
            totalRooms += r.getRoomsCount();
            bookedRooms.add(bookedRoom);
        }

        booking.setBookedRooms(bookedRooms);
        booking.setAmount(grandTotal);
        booking.setTotalRoomsCount(totalRooms);
        bookingRepository.save(booking);

        return  modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking not found with booking Id: {}"+ bookingId));
        User user = guestDtoList.get(0).getUser();
        User authenticatedUser =getCurrentUser();
        if(!booking.getUser().getId().equals(authenticatedUser.getId())){
            throw new IllegalStateException("You are not authorized to add guests to this booking");
        }
        
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has expired");
        }
        if(booking.getBookingStatus()!= BookingStatus.RESERVED){
            throw  new IllegalStateException("Booking is not under reserved state , cannot add guests");
        }
        for(GuestDto guestDto : guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(authenticatedUser);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("booking not found with booking id "+ bookingId));

        User authenticatedUser = getCurrentUser();
        System.out.println("Authenticated user: " + authenticatedUser.getId() + " (" + authenticatedUser.getId().getClass().getName() + ")");
        System.out.println("Booking user: " + booking.getUser().getId() + " (" + booking.getUser().getId().getClass().getName() + ")");
        System.out.println("User equals booking user: " + authenticatedUser.getId().equals(booking.getUser().getId()));
        System.out.println("Using toString comparison: " + authenticatedUser.getId().toString().equals(booking.getUser().getId().toString()));


        if(!authenticatedUser.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to the current user");
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has expired");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payments/success",frontendUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Fetch the dummy user from the database
       
    }
}
