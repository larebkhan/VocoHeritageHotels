package com.lareb.springProject.AirBnb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lareb.springProject.AirBnb.Strategy.PricingService;
import com.lareb.springProject.AirBnb.dto.BookingDto;
import com.lareb.springProject.AirBnb.dto.BookingRequest;
import com.lareb.springProject.AirBnb.dto.GuestDto;
import com.lareb.springProject.AirBnb.dto.HotelReportDto;
import com.lareb.springProject.AirBnb.entity.*;
import com.lareb.springProject.AirBnb.entity.enums.BookingStatus;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.exception.UnAuthorizedException;
import com.lareb.springProject.AirBnb.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.AccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lareb.springProject.AirBnb.util.AppUtils.getCurrentUser;


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
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("booking not found with booking id "+ bookingId));

        User authenticatedUser = getCurrentUser();
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

    @Override
    @Transactional
    public void capturePayment(Event event) {
        log.info("Stripe Event Received: {}", event.getType());
        if("checkout.session.completed".equals(event.getType())){
            log.info("Handling checkout.session.completed event: {}", event.getId());
            Session session =retrieveSessionFromEvent(event);


            if(session == null){
                log.warn("Deserialized object for session is empty!");
                return;
            }

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()-> new ResourceNotFoundException("Booking not found for session id : "+ sessionId));
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            for (BookedRoom r :booking.getBookedRooms()) {
                inventoryRepository.findAndLockReservedInventory(r.getRoom().getId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        r.getRoomsCount());

                inventoryRepository.confirmBooking(r.getRoom().getId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        r.getRoomsCount());
            }
            log.info("Successfully confirmed booking for booking ID : {} ",booking.getId());

        }else{
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("booking not found with booking id "+ bookingId));

        User authenticatedUser = getCurrentUser();
        if(!authenticatedUser.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to the current user");
        }
        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed booking can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        for (BookedRoom r :booking.getBookedRooms()) {
            inventoryRepository.findAndLockReservedInventory(r.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    r.getRoomsCount());

            inventoryRepository.cancelBooking(r.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    r.getRoomsCount());
        }
        //handle the refund
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }



    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("booking not found with booking id "+ bookingId));

        User authenticatedUser = getCurrentUser();
        if(!authenticatedUser.getId().equals(booking.getUser().getId())){
            throw new UnAuthorizedException("Booking does not belong to the current user");
        }

        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with hotel Id: "+hotelId));
        User user = getCurrentUser();
        log.info("Getting all bookings for the hotel with id : "+ hotelId);
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id : "+ hotelId);
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return  bookings.stream().map((element) -> modelMapper.map(element, BookingDto.class)).collect(Collectors.toList());

    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with hotel Id: "+hotelId));
        User user = getCurrentUser();
        log.info("Getting all bookings for the hotel with id : "+ hotelId);
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id : "+ hotelId);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);
        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings==0?BigDecimal.ZERO: totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);


    }

    @Override
    public List<BookingDto> getMyBookings() {

        User user = getCurrentUser();
        List<Booking> bookings =  bookingRepository.findByUser(user);
        return bookings.stream().map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    private Session retrieveSessionFromEvent(Event event) {
        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                return (Session) deserializer.getObject().get();
            } else {
                String rawJson = event.getData().getObject().toJson();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawJson);
                String sessionId = jsonNode.get("id").asText();
                return Session.retrieve(sessionId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to retrieve session data");
        }
    }
    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }


}
