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

    public BookingService(BookingRepository bookingRepository, BookingStatusRepository bookingStatusRepository,
                          ClientSubscriptionRepository clientSubscriptionRepository,
                          SubscriptionRepository subscriptionRepository,
                          SubscriptionStatusRepository subscriptionStatusRepository){
        this.bookingRepository = bookingRepository;
        this.bookingStatusRepository  = bookingStatusRepository;
        this.clientSubscriptionRepository = clientSubscriptionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionStatusRepository = subscriptionStatusRepository;
    }
}
