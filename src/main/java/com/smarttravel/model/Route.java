package com.smarttravel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeid;

    private String startLocation;
    private String destination;
    private double distance;
    private String transportMode;

    public Route() {}

    public Route(String startLocation, String destination, double distance, String transportMode) {
        this.startLocation = startLocation;
        this.destination = destination;
        this.distance = distance;
        this.transportMode = transportMode;
    }

    // Getters and Setters
    public Long getId() { return routeid; }
    public void setId(Long id) { this.routeid = id; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
}
