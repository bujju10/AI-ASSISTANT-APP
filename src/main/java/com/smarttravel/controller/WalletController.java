package com.smarttravel.controller;

import com.smarttravel.model.Payment;
import com.smarttravel.model.Booking;
import com.smarttravel.model.Route;
import com.smarttravel.service.WalletService;
import com.smarttravel.service.RouteService;
import com.smarttravel.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private BookingService bookingService;

    /**
     * Get wallet balance
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getWalletBalance(@PathVariable Long userId) {
        try {
            BigDecimal balance = walletService.getWalletBalance(userId);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "balance", balance,
                "currency", "INR"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get wallet balance: " + e.getMessage()));
        }
    }

    /**
     * Add money to wallet
     */
    @PostMapping("/add-money")
    public ResponseEntity<?> addMoney(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String method = request.getOrDefault("method", "DEMO").toString();

            boolean success = walletService.addToWallet(userId, amount, method);
            
            if (success) {
                BigDecimal newBalance = walletService.getWalletBalance(userId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Money added successfully",
                    "amount", amount,
                    "newBalance", newBalance,
                    "method", method
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to add money to wallet"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add money: " + e.getMessage()));
        }
    }

    /**
     * Get payment history
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long userId) {
        try {
            List<Payment> payments = walletService.getPaymentHistory(userId);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "payments", payments,
                "totalTransactions", payments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get payment history: " + e.getMessage()));
        }
    }

    /**
     * Calculate fare for a route
     */
    @PostMapping("/calculate-fare")
    public ResponseEntity<?> calculateFare(@RequestBody Map<String, Object> request) {
        try {
            String start = request.get("start").toString();
            String end = request.get("end").toString();
            String transportType = request.getOrDefault("transportType", "cab").toString();

            // Get route distance (you can integrate with your route analysis service)
            Double distance = 10.0; // Default distance for demo
            
            BigDecimal fare = walletService.calculateFare(distance, transportType);
            
            return ResponseEntity.ok(Map.of(
                "start", start,
                "end", end,
                "distance", distance,
                "transportType", transportType,
                "fare", fare,
                "currency", "INR"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to calculate fare: " + e.getMessage()));
        }
    }

    /**
     * Process booking with wallet payment
     */
    @PostMapping("/book-ride")
    public ResponseEntity<?> bookRide(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String passengerName = request.get("passengerName").toString();
            String fromLocation = request.get("fromLocation").toString();
            String toLocation = request.get("toLocation").toString();
            String transportType = request.get("transportType").toString();
            String travelDate = request.get("travelDate").toString();
            String travelTime = request.get("travelTime").toString();

            // Calculate fare
            Double distance = 10.0; // You can integrate with real distance calculation
            BigDecimal fare = walletService.calculateFare(distance, transportType);

            // Check if user has sufficient balance
            if (!walletService.hasSufficientBalance(userId, fare)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Insufficient wallet balance",
                    "required", fare,
                    "available", walletService.getWalletBalance(userId)
                ));
            }

            // Create booking
            LocalDateTime dateTime = LocalDateTime.parse(travelDate + "T" + travelTime);
            Booking booking = new Booking(userId, passengerName, transportType, fromLocation, toLocation, dateTime, fare);
            booking = bookingService.saveBooking(booking);

            // Process payment
            boolean paymentSuccess = walletService.processBookingPayment(userId, booking.getBookingId(), fare);
            
            if (paymentSuccess) {
                BigDecimal newBalance = walletService.getWalletBalance(userId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ride booked successfully",
                    "bookingId", booking.getBookingId(),
                    "fare", fare,
                    "newBalance", newBalance,
                    "bookingDate", booking.getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment processing failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to book ride: " + e.getMessage()));
        }
    }

    /**
     * Get user's booking history
     */
    @GetMapping("/bookings/{userId}")
    public ResponseEntity<?> getBookingHistory(@PathVariable Long userId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "bookings", bookings,
                "totalBookings", bookings.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get booking history: " + e.getMessage()));
        }
    }

    /**
     * Get quick add money options
     */
    @GetMapping("/quick-add/{userId}")
    public ResponseEntity<?> getQuickAddOptions(@PathVariable Long userId) {
        try {
            BigDecimal currentBalance = walletService.getWalletBalance(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "currentBalance", currentBalance,
                "quickAddOptions", List.of(
                    Map.of("amount", 500, "label", "Add ₹500"),
                    Map.of("amount", 1000, "label", "Add ₹1000"),
                    Map.of("amount", 2000, "label", "Add ₹2000"),
                    Map.of("amount", 5000, "label", "Add ₹5000")
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get quick add options: " + e.getMessage()));
        }
    }
}
