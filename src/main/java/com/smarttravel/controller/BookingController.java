package com.smarttravel.controller;

import com.smarttravel.model.Booking;
import com.smarttravel.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<?> createBooking(@RequestBody java.util.Map<String,Object> payload,
                                                                     @RequestParam(required = false) Long userId) {
        try {
            Booking booking = new Booking();
            // userId may come as query param or in payload
            if (userId == null && payload.containsKey("userId")) {
                userId = Long.valueOf(String.valueOf(payload.get("userId")));
            }
            booking.setUserId(userId);

            if (payload.containsKey("pickup")) booking.setFromLocation(String.valueOf(payload.get("pickup")));
            if (payload.containsKey("destination")) booking.setToLocation(String.valueOf(payload.get("destination")));
            if (payload.containsKey("cabType")) booking.setTransportType(String.valueOf(payload.get("cabType")));
            if (payload.containsKey("passengerName")) booking.setPassengerName(String.valueOf(payload.get("passengerName")));

            // parse travelDate (from frontend 'travelDate', format YYYY-MM-DD)
            if (payload.containsKey("travelDate")) {
                String d = String.valueOf(payload.get("travelDate"));
                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(d);
                    booking.setDateTime(ld.atStartOfDay());
                } catch (Exception ex) {
                    // try parse as datetime
                    try {
                        booking.setDateTime(java.time.LocalDateTime.parse(d));
                    } catch (Exception ex2) {
                        booking.setDateTime(LocalDateTime.now());
                    }
                }
            } else if (booking.getDateTime() == null) {
                booking.setDateTime(LocalDateTime.now());
            }

            Booking saved = bookingRepository.save(booking);
            return org.springframework.http.ResponseEntity.ok(saved);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body("Booking failed: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingRepository.findByUserIdOrderByDateTimeDesc(userId);
    }
}
