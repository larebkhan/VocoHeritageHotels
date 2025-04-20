package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
