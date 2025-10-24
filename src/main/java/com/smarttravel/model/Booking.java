package com.smarttravel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "route_id")
    private Long routeId;
    
    @Column(name = "transport_type")
    private String transportType;
    
    @Column(name = "fare", precision = 10, scale = 2)
    private BigDecimal fare;
    
    @Column(name = "status", length = 20)
    private String status = "CONFIRMED";
    
    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();
    
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    
    @Column(name = "from_location")
    private String fromLocation;
    
    @Column(name = "passenger_name")
    private String passengerName;
    
    @Column(name = "to_location")
    private String toLocation;

    public Booking() {}

    public Booking(Long userId, String passengerName, String transportType, String fromLocation, 
                   String toLocation, LocalDateTime dateTime, BigDecimal fare) {
        this.userId = userId;
        this.passengerName = passengerName;
        this.transportType = transportType;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.dateTime = dateTime;
        this.fare = fare;
        this.status = "CONFIRMED";
        this.bookingDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    
    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal fare) { this.fare = fare; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
}
