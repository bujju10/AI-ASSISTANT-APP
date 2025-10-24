package com.smarttravel.service;

import com.smarttravel.model.User;
import com.smarttravel.model.Payment;
import com.smarttravel.model.Booking;
import com.smarttravel.repository.UserRepository;
import com.smarttravel.repository.PaymentRepository;
import com.smarttravel.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Add money to user's wallet
     */
    @Transactional
    public boolean addToWallet(Long userId, BigDecimal amount, String method) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            BigDecimal currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
            BigDecimal newBalance = currentBalance.add(amount);
            user.setWalletBalance(newBalance);
            userRepository.save(user);

            // Record the payment
            Payment payment = new Payment(null, userId, amount, method);
            paymentRepository.save(payment);

            return true;
        } catch (Exception e) {
            System.err.println("Error adding to wallet: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deduct money from user's wallet for booking
     */
    @Transactional
    public boolean deductFromWallet(Long userId, BigDecimal amount, Long bookingId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            BigDecimal currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
            
            if (currentBalance.compareTo(amount) < 0) {
                return false; // Insufficient balance
            }

            BigDecimal newBalance = currentBalance.subtract(amount);
            user.setWalletBalance(newBalance);
            userRepository.save(user);

            // Record the payment
            Payment payment = new Payment(bookingId, userId, amount.negate(), "WALLET");
            paymentRepository.save(payment);

            return true;
        } catch (Exception e) {
            System.err.println("Error deducting from wallet: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user's wallet balance
     */
    public BigDecimal getWalletBalance(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return userOpt.get().getWalletBalance() != null ? userOpt.get().getWalletBalance() : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error getting wallet balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get user's payment history
     */
    public List<Payment> getPaymentHistory(Long userId) {
        try {
            return paymentRepository.findByUserIdOrderByPaymentDateDesc(userId);
        } catch (Exception e) {
            System.err.println("Error getting payment history: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Calculate fare based on distance and transport type
     */
    public BigDecimal calculateFare(Double distance, String transportType) {
        BigDecimal baseRate;
        
        switch (transportType.toLowerCase()) {
            case "cab":
            case "taxi":
                baseRate = new BigDecimal("15.0"); // ₹15 per km
                break;
            case "auto":
            case "rickshaw":
                baseRate = new BigDecimal("12.0"); // ₹12 per km
                break;
            case "bus":
                baseRate = new BigDecimal("8.0"); // ₹8 per km
                break;
            case "metro":
                baseRate = new BigDecimal("10.0"); // ₹10 per km
                break;
            case "train":
                baseRate = new BigDecimal("5.0"); // ₹5 per km
                break;
            case "flight":
                baseRate = new BigDecimal("25.0"); // ₹25 per km (base rate)
                break;
            default:
                baseRate = new BigDecimal("10.0"); // Default rate
        }
        
        return baseRate.multiply(new BigDecimal(distance.toString()));
    }

    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        BigDecimal balance = getWalletBalance(userId);
        return balance.compareTo(amount) >= 0;
    }

    /**
     * Process booking payment
     */
    @Transactional
    public boolean processBookingPayment(Long userId, Long bookingId, BigDecimal fare) {
        try {
            if (!hasSufficientBalance(userId, fare)) {
                return false;
            }

            return deductFromWallet(userId, fare, bookingId);
        } catch (Exception e) {
            System.err.println("Error processing booking payment: " + e.getMessage());
            return false;
        }
    }
}
