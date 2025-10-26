package com.smarttravel.controller;

import com.smarttravel.model.Route;
import com.smarttravel.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {
    
    @Autowired
    private RouteService routeService;

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        return routeService.saveRoute(route);
    }

    /**
     * Auto-save a searched route
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchAndSaveRoute(@RequestBody Map<String, Object> request) {
        try {
            String source = request.get("source").toString();
            String destination = request.get("destination").toString();
            String transportType = request.getOrDefault("transportType", "cab").toString();
            
            // Use actual distance from request if provided, otherwise calculate
            Double distance;
            if (request.containsKey("distance") && request.get("distance") != null) {
                Object distObj = request.get("distance");
                if (distObj instanceof Number) {
                    distance = ((Number) distObj).doubleValue();
                } else {
                    distance = Double.parseDouble(distObj.toString());
                }
            } else {
                // Fallback: calculate distance (simplified - in real app, use map service)
                distance = calculateDistance(source, destination);
            }
            
            // Save the searched route
            Route savedRoute = routeService.saveSearchedRoute(source, destination, distance, transportType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Route saved to search history",
                "route", savedRoute
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to save route: " + e.getMessage()
            ));
        }
    }

    /**
     * Get search history (recent routes)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getSearchHistory() {
        try {
            List<Route> recentRoutes = routeService.getRecentRoutes();
            return ResponseEntity.ok(Map.of(
                "routes", recentRoutes,
                "total", recentRoutes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get search history: " + e.getMessage()
            ));
        }
    }

    /**
     * Search routes by query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchRoutes(@RequestParam String q) {
        try {
            List<Route> routes = routeService.searchRoutes(q);
            return ResponseEntity.ok(Map.of(
                "routes", routes,
                "query", q,
                "total", routes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to search routes: " + e.getMessage()
            ));
        }
    }

    /**
     * Get routes by source location
     */
    @GetMapping("/from/{source}")
    public ResponseEntity<?> getRoutesFromSource(@PathVariable String source) {
        try {
            List<Route> routes = routeService.getRoutesBySource(source);
            return ResponseEntity.ok(Map.of(
                "routes", routes,
                "source", source,
                "total", routes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get routes from source: " + e.getMessage()
            ));
        }
    }

    /**
     * Get routes by destination location
     */
    @GetMapping("/to/{destination}")
    public ResponseEntity<?> getRoutesToDestination(@PathVariable String destination) {
        try {
            List<Route> routes = routeService.getRoutesByDestination(destination);
            return ResponseEntity.ok(Map.of(
                "routes", routes,
                "destination", destination,
                "total", routes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get routes to destination: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete a route from history
     */
    @DeleteMapping("/{routeId}")
    public ResponseEntity<?> deleteRoute(@PathVariable Long routeId) {
        try {
            routeService.deleteRoute(routeId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Route deleted from history"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete route: " + e.getMessage()
            ));
        }
    }

    /**
     * Simple distance calculation (replace with real map service)
     */
    private Double calculateDistance(String source, String destination) {
        // This is a simplified calculation - in real app, use Google Maps API or similar
        // For demo purposes, return a random distance between 5-50 km
        return Math.random() * 45 + 5;
    }
}
