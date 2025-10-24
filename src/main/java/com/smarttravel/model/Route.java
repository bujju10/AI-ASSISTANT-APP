package com.smarttravel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "source", length = 100, nullable = false)
    private String source;

    @Column(name = "destination")
    private String destination;

    @Column(name = "distance", nullable = false)
    private Double distance;

    @Column(name = "suggested_transport", length = 50)
    private String suggestedTransport;

    @Column(name = "start_location")
    private String startLocation;

    @Column(name = "transport_mode")
    private String transportMode;

    public Route() {}

    public Route(String source, String destination, Double distance, String suggestedTransport, 
                 String startLocation, String transportMode) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.suggestedTransport = suggestedTransport;
        this.startLocation = startLocation;
        this.transportMode = transportMode;
    }

    // Getters and Setters
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getSuggestedTransport() { return suggestedTransport; }
    public void setSuggestedTransport(String suggestedTransport) { this.suggestedTransport = suggestedTransport; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
}
