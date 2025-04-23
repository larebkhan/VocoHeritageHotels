package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.entity.BookedRoom;
import com.lareb.springProject.AirBnb.entity.Booking;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating session for booking with ID: {}", booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            // Create a Stripe Customer for this user
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();

            Customer customer = Customer.create(customerParams);

            // Start building the session
            SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl);

            // Add a line item for each room type in the booking
            for (BookedRoom bookedRoom : booking.getBookedRooms()) {
                sessionBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) bookedRoom.getRoomsCount())
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount(bookedRoom.getPricePerRoom().multiply(BigDecimal.valueOf(100)).longValue()) // price in paisa
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(booking.getHotel().getName() + " : " + bookedRoom.getRoom().getType())
                                                                .setDescription("Booking ID: " + booking.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );
            }

            // Create the checkout session
            Session session = Session.create(sessionBuilder.build());

            // Save session ID to the booking
            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Session created successfully with session ID: {}", session.getId());
            return session.getUrl();

        } catch (StripeException e) {
            log.error("StripeException occurred while creating session: ", e);
            throw new RuntimeException("Stripe session creation failed", e);
        }
    }
}
