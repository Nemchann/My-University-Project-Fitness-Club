package com.nemchann.fitnessbackend.booking.service;

import com.nemchann.fitnessbackend.booking.repository.*;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private BookingRepository bookingRepository;
    private BookingStatusRepository bookingStatusRepository;
    private ClientSubscriptionRepository clientSubscriptionRepository;
    private SubscriptionRepository subscriptionRepository;
    private SubscriptionStatusRepository subscriptionStatusRepository;
}
