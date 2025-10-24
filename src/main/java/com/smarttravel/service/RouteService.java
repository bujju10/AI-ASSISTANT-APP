package com.smarttravel.service;

import com.smarttravel.model.Route;
import com.smarttravel.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    /**
     * Save or update a searched route
     */
    public Route saveSearchedRoute(String source, String destination, Double distance, String transportType) {
        // Check if route already exists
        Optional<Route> existingRoute = routeRepository.findBySourceAndDestination(source, destination);
        
        if (existingRoute.isPresent()) {
            // Update existing route
            Route route = existingRoute.get();
            route.setDistance(distance);
            route.setTransportMode(transportType);
            route.setSuggestedTransport(transportType);
            return routeRepository.save(route);
        } else {
            // Create new route
            Route newRoute = new Route(source, destination, distance, transportType, source, transportType);
            return routeRepository.save(newRoute);
        }
    }

    /**
     * Get routes by source location
     */
    public List<Route> getRoutesBySource(String source) {
        return routeRepository.findBySourceContainingIgnoreCase(source);
    }

    /**
     * Get routes by destination location
     */
    public List<Route> getRoutesByDestination(String destination) {
        return routeRepository.findByDestinationContainingIgnoreCase(destination);
    }

    /**
     * Get recent routes (last 10)
     */
    public List<Route> getRecentRoutes() {
        return routeRepository.findTop10ByOrderByRouteIdDesc();
    }

    /**
     * Delete a route
     */
    public void deleteRoute(Long routeId) {
        routeRepository.deleteById(routeId);
    }

    /**
     * Search routes by query
     */
    public List<Route> searchRoutes(String query) {
        return routeRepository.searchRoutes(query);
    }
}
