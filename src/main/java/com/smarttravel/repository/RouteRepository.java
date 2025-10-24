package com.smarttravel.repository;

import com.smarttravel.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    // Find route by source and destination
    Optional<Route> findBySourceAndDestination(String source, String destination);
    
    // Find routes by source (case insensitive)
    List<Route> findBySourceContainingIgnoreCase(String source);
    
    // Find routes by destination (case insensitive)
    List<Route> findByDestinationContainingIgnoreCase(String destination);
    
    // Get recent routes (last 10)
    @Query("SELECT r FROM Route r ORDER BY r.routeId DESC")
    List<Route> findTop10ByOrderByRouteIdDesc();
    
    // Search routes by source or destination
    @Query("SELECT r FROM Route r WHERE LOWER(r.source) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.destination) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Route> searchRoutes(String query);
}
