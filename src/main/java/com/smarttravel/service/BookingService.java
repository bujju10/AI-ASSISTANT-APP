package com.smarttravel.service;

import com.smarttravel.model.Booking;
import com.smarttravel.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository repo;

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    public Booking saveBooking(Booking booking) {
        return repo.save(booking);
    }

    public List<Booking> getAllBookings() {
        return repo.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    public Booking getBookingById(Long bookingId) {
        return repo.findById(bookingId).orElse(null);
    }
}
