package com.smarttravel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;               // store user id (simple)
    @Column(name = "passenger_name")
    private String passengerName;
    @Column(name = "transport_type")
    private String transportType;      // Cab/Auto/Train/Flight
    @Column(name = "from_location")
    private String fromLocation;
    @Column(name = "to_location")
    private String toLocation;
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    public Booking() {}

    public Booking(Long userId, String passengerName, String transportType, String fromLocation, String toLocation, LocalDateTime dateTime) {
        this.userId = userId;
        this.passengerName = passengerName;
        this.transportType = transportType;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.dateTime = dateTime;
    }

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}
