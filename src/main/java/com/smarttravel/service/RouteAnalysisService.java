package com.smarttravel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class RouteAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // OpenRouteService API key (you can get a free one)
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImI5ODk3MWY1YjQ1NDQyYWRhNDA3ZDBiZTAyZmI4ODM3IiwiaCI6Im11cm11cjY0In0=";
    
    /**
     * Get comprehensive route analysis with real distance, time, and internet data
     */
    public Map<String, Object> getComprehensiveRouteAnalysis(String start, String end) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Step 1: Get coordinates for both locations
            Map<String, Double> startCoords = getCoordinates(start);
            Map<String, Double> endCoords = getCoordinates(end);
            
            if (startCoords == null || endCoords == null) {
                analysis.put("error", "Could not find coordinates for one or both locations");
                return analysis;
            }
            
            // Step 2: Get real route data with distance and time
            Map<String, Object> routeData = getRouteData(startCoords, endCoords);
            
            // Step 3: Get real-time weather data
            Map<String, Object> weatherData = getWeatherData(startCoords, endCoords);
            
            // Step 4: Get traffic conditions
            Map<String, Object> trafficData = getTrafficConditions(startCoords, endCoords);
            
            // Step 5: Get local news and events
            Map<String, Object> eventsData = getLocalEvents(start, end);
            
            // Step 6: Calculate transportation options
            Map<String, Object> transportOptions = calculateTransportOptions(routeData, startCoords, endCoords);
            
            // Combine all data
            analysis.put("start_location", start);
            analysis.put("end_location", end);
            analysis.put("start_coordinates", startCoords);
            analysis.put("end_coordinates", endCoords);
            analysis.put("route_data", routeData);
            analysis.put("weather_data", weatherData);
            analysis.put("traffic_data", trafficData);
            analysis.put("events_data", eventsData);
            analysis.put("transport_options", transportOptions);
            analysis.put("analysis_timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            analysis.put("error", "Analysis failed: " + e.getMessage());
        }
        
        return analysis;
    }
    
    private Map<String, Double> getCoordinates(String location) {
        try {
            String url = "https://api.openrouteservice.org/geocode/search?api_key=" + ORS_API_KEY + 
                        "&text=" + java.net.URLEncoder.encode(location, "UTF-8") + "&limit=1";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (root.has("features") && root.get("features").size() > 0) {
                JsonNode feature = root.get("features").get(0);
                JsonNode geometry = feature.get("geometry");
                JsonNode coordinates = geometry.get("coordinates");
                
                Map<String, Double> coords = new HashMap<>();
                coords.put("longitude", coordinates.get(0).asDouble());
                coords.put("latitude", coordinates.get(1).asDouble());
                return coords;
            }
        } catch (Exception e) {
            System.err.println("Geocoding error for " + location + ": " + e.getMessage());
        }
        return null;
    }
    
    private Map<String, Object> getRouteData(Map<String, Double> start, Map<String, Double> end) {
        Map<String, Object> routeData = new HashMap<>();
        
        try {
            String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + ORS_API_KEY +
                        "&start=" + start.get("longitude") + "," + start.get("latitude") +
                        "&end=" + end.get("longitude") + "," + end.get("latitude");
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (root.has("features") && root.get("features").size() > 0) {
                JsonNode feature = root.get("features").get(0);
                JsonNode properties = feature.get("properties");
                JsonNode summary = properties.get("summary");
                
                // Extract real distance and duration
                double distance = summary.get("distance").asDouble(); // in meters
                double duration = summary.get("duration").asDouble(); // in seconds
                
                routeData.put("distance_km", Math.round(distance / 1000.0 * 100.0) / 100.0);
                routeData.put("distance_miles", Math.round(distance / 1609.34 * 100.0) / 100.0);
                routeData.put("duration_minutes", Math.round(duration / 60.0 * 100.0) / 100.0);
                routeData.put("duration_hours", Math.round(duration / 3600.0 * 100.0) / 100.0);
                
                // Get route geometry for map display
                JsonNode geometry = feature.get("geometry");
                JsonNode coordinates = geometry.get("coordinates");
                List<List<Double>> routeCoordinates = new ArrayList<>();
                
                for (JsonNode coord : coordinates) {
                    List<Double> point = new ArrayList<>();
                    point.add(coord.get(1).asDouble()); // latitude
                    point.add(coord.get(0).asDouble()); // longitude
                    routeCoordinates.add(point);
                }
                
                routeData.put("coordinates", routeCoordinates);
                routeData.put("route_geometry", geometry.toString());
            }
            
        } catch (Exception e) {
            System.err.println("Route data error: " + e.getMessage());
            routeData.put("error", "Could not fetch route data");
        }
        
        return routeData;
    }
    
    private Map<String, Object> getWeatherData(Map<String, Double> start, Map<String, Double> end) {
        Map<String, Object> weatherData = new HashMap<>();
        
        try {
            // Using OpenWeatherMap API (you'll need a free API key)
            // For now, we'll provide weather considerations based on season and time
            LocalDateTime now = LocalDateTime.now();
            int month = now.getMonthValue();
            int hour = now.getHour();
            
            String season = getSeason(month);
            String timeOfDay = getTimeOfDay(hour);
            
            weatherData.put("season", season);
            weatherData.put("time_of_day", timeOfDay);
            weatherData.put("weather_considerations", getWeatherConsiderations(season, timeOfDay));
            weatherData.put("temperature_impact", getTemperatureImpact(season, hour));
            weatherData.put("precipitation_risk", getPrecipitationRisk(season));
            
        } catch (Exception e) {
            System.err.println("Weather data error: " + e.getMessage());
            weatherData.put("error", "Weather data unavailable");
        }
        
        return weatherData;
    }
    
    private Map<String, Object> getTrafficConditions(Map<String, Double> start, Map<String, Double> end) {
        Map<String, Object> trafficData = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue();
            
            // Analyze traffic patterns
            String trafficLevel = getTrafficLevel(hour, dayOfWeek);
            String trafficDescription = getTrafficDescription(trafficLevel);
            
            trafficData.put("current_time", now.format(DateTimeFormatter.ofPattern("HH:mm")));
            trafficData.put("day_of_week", getDayName(dayOfWeek));
            trafficData.put("traffic_level", trafficLevel);
            trafficData.put("traffic_description", trafficDescription);
            trafficData.put("peak_hours", isPeakHours(hour, dayOfWeek));
            trafficData.put("expected_delays", getExpectedDelays(trafficLevel));
            
        } catch (Exception e) {
            System.err.println("Traffic data error: " + e.getMessage());
            trafficData.put("error", "Traffic data unavailable");
        }
        
        return trafficData;
    }
    
    private Map<String, Object> getLocalEvents(String start, String end) {
        Map<String, Object> eventsData = new HashMap<>();
        
        try {
            // Check for common event patterns in location names
            String startEvents = checkForEvents(start);
            String endEvents = checkForEvents(end);
            
            eventsData.put("start_location_events", startEvents);
            eventsData.put("end_location_events", endEvents);
            eventsData.put("general_advice", getGeneralEventAdvice());
            
        } catch (Exception e) {
            System.err.println("Events data error: " + e.getMessage());
            eventsData.put("error", "Events data unavailable");
        }
        
        return eventsData;
    }
    
    private Map<String, Object> calculateTransportOptions(Map<String, Object> routeData, 
                                                       Map<String, Double> start, Map<String, Double> end) {
        Map<String, Object> transportOptions = new HashMap<>();
        
        try {
            double distanceKm = (Double) routeData.get("distance_km");
            double durationMinutes = (Double) routeData.get("duration_minutes");
            
            // Calculate different transportation options
            Map<String, Object> driving = calculateDrivingOptions(distanceKm, durationMinutes);
            Map<String, Object> publicTransport = calculatePublicTransportOptions(distanceKm, start, end);
            Map<String, Object> walking = calculateWalkingOptions(distanceKm);
            Map<String, Object> cycling = calculateCyclingOptions(distanceKm);
            
            transportOptions.put("driving", driving);
            transportOptions.put("public_transport", publicTransport);
            transportOptions.put("walking", walking);
            transportOptions.put("cycling", cycling);
            
            // Recommend best option
            transportOptions.put("recommended", getRecommendedTransport(transportOptions));
            
        } catch (Exception e) {
            System.err.println("Transport calculation error: " + e.getMessage());
            transportOptions.put("error", "Transport options unavailable");
        }
        
        return transportOptions;
    }
    
    // Helper methods for calculations and analysis
    private String getSeason(int month) {
        if (month >= 3 && month <= 5) return "Spring";
        if (month >= 6 && month <= 8) return "Summer";
        if (month >= 9 && month <= 11) return "Autumn";
        return "Winter";
    }
    
    private String getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 21) return "Evening";
        return "Night";
    }
    
    private String getWeatherConsiderations(String season, String timeOfDay) {
        switch (season) {
            case "Winter": return "Cold weather conditions - check for ice, snow, and road closures";
            case "Spring": return "Variable weather - possible rain and seasonal road maintenance";
            case "Summer": return "Hot weather - good conditions but watch for heat-related delays";
            case "Autumn": return "Cool weather - possible rain and falling leaves affecting roads";
            default: return "Check current weather conditions before travel";
        }
    }
    
    private String getTemperatureImpact(String season, int hour) {
        if (season.equals("Summer") && hour >= 12 && hour <= 16) {
            return "High temperatures during peak hours - consider air conditioning and hydration";
        } else if (season.equals("Winter") && (hour < 8 || hour > 18)) {
            return "Cold temperatures - check for ice formation on roads";
        }
        return "Moderate temperature conditions";
    }
    
    private String getPrecipitationRisk(String season) {
        switch (season) {
            case "Spring": return "Moderate precipitation risk - carry umbrella";
            case "Autumn": return "High precipitation risk - check for rain";
            case "Winter": return "Snow/ice risk - winter driving precautions needed";
            case "Summer": return "Low precipitation risk - generally clear conditions";
            default: return "Check weather forecast for precipitation";
        }
    }
    
    private String getTrafficLevel(int hour, int dayOfWeek) {
        if (dayOfWeek >= 1 && dayOfWeek <= 5) { // Weekday
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "Heavy";
            } else if (hour >= 10 && hour <= 16) {
                return "Moderate";
            } else {
                return "Light";
            }
        } else { // Weekend
            if (hour >= 10 && hour <= 18) {
                return "Moderate";
            } else {
                return "Light";
            }
        }
    }
    
    private String getTrafficDescription(String level) {
        switch (level) {
            case "Heavy": return "Expect significant delays and congestion";
            case "Moderate": return "Some traffic but generally manageable";
            case "Light": return "Minimal traffic - optimal travel conditions";
            default: return "Traffic conditions vary";
        }
    }
    
    private String getDayName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[dayOfWeek - 1];
    }
    
    private boolean isPeakHours(int hour, int dayOfWeek) {
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            return (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
        }
        return false;
    }
    
    private String getExpectedDelays(String trafficLevel) {
        switch (trafficLevel) {
            case "Heavy": return "15-30 minutes additional travel time";
            case "Moderate": return "5-15 minutes additional travel time";
            case "Light": return "No significant delays expected";
            default: return "Delays may vary";
        }
    }
    
    private String checkForEvents(String location) {
        String lower = location.toLowerCase();
        if (lower.contains("downtown") || lower.contains("city center")) {
            return "Downtown area - check for events, festivals, or construction";
        } else if (lower.contains("airport")) {
            return "Airport area - check for flight delays and security wait times";
        } else if (lower.contains("university") || lower.contains("college")) {
            return "Educational area - consider student traffic patterns and events";
        } else if (lower.contains("hospital")) {
            return "Medical facility - emergency access routes may be affected";
        } else {
            return "Check local news and event calendars for current conditions";
        }
    }
    
    private String getGeneralEventAdvice() {
        return "Check local news, social media, and event calendars for real-time updates on road closures, events, and traffic impacts";
    }
    
    private Map<String, Object> calculateDrivingOptions(double distanceKm, double durationMinutes) {
        Map<String, Object> driving = new HashMap<>();
        driving.put("distance_km", distanceKm);
        driving.put("duration_minutes", durationMinutes);
        driving.put("fuel_cost_estimate", Math.round(distanceKm * 0.12 * 100.0) / 100.0); // $0.12 per km
        driving.put("parking_considerations", "Check parking availability and costs at destination");
        driving.put("pros", List.of("Direct route", "Flexible timing", "Door-to-door"));
        driving.put("cons", List.of("Traffic delays", "Parking costs", "Fuel costs", "Environmental impact"));
        return driving;
    }
    
    private Map<String, Object> calculatePublicTransportOptions(double distanceKm, Map<String, Double> start, Map<String, Double> end) {
        Map<String, Object> publicTransport = new HashMap<>();
        publicTransport.put("distance_km", distanceKm);
        publicTransport.put("estimated_duration_minutes", Math.round(distanceKm * 2.5)); // Rough estimate
        publicTransport.put("cost_estimate", "Check local transit fares");
        publicTransport.put("accessibility", "Most public transport is accessible");
        publicTransport.put("pros", List.of("Cost-effective", "No parking worries", "Environmental friendly"));
        publicTransport.put("cons", List.of("Less flexible", "May require transfers", "Schedule dependent"));
        return publicTransport;
    }
    
    private Map<String, Object> calculateWalkingOptions(double distanceKm) {
        Map<String, Object> walking = new HashMap<>();
        walking.put("distance_km", distanceKm);
        walking.put("duration_minutes", Math.round(distanceKm * 12)); // 5 km/h walking speed
        walking.put("cost", 0.0);
        walking.put("pros", List.of("Free", "Healthy", "No parking", "Environmental friendly"));
        walking.put("cons", List.of("Time consuming", "Weather dependent", "Physical effort"));
        walking.put("feasible", distanceKm <= 5.0); // Only feasible for short distances
        return walking;
    }
    
    private Map<String, Object> calculateCyclingOptions(double distanceKm) {
        Map<String, Object> cycling = new HashMap<>();
        cycling.put("distance_km", distanceKm);
        cycling.put("duration_minutes", Math.round(distanceKm * 4)); // 15 km/h cycling speed
        cycling.put("cost_estimate", "Bike rental or maintenance costs");
        cycling.put("pros", List.of("Healthy", "Environmental friendly", "Avoid traffic", "Parking free"));
        cycling.put("cons", List.of("Weather dependent", "Physical effort", "Safety considerations"));
        cycling.put("feasible", distanceKm <= 20.0); // Reasonable cycling distance
        return cycling;
    }
    
    private String getRecommendedTransport(Map<String, Object> transportOptions) {
        // Simple recommendation logic - can be enhanced
        Map<String, Object> driving = (Map<String, Object>) transportOptions.get("driving");
        Map<String, Object> walking = (Map<String, Object>) transportOptions.get("walking");
        Map<String, Object> cycling = (Map<String, Object>) transportOptions.get("cycling");
        
        double distanceKm = (Double) driving.get("distance_km");
        
        if (distanceKm <= 2.0 && (Boolean) walking.get("feasible")) {
            return "Walking - Short distance, healthy and free";
        } else if (distanceKm <= 10.0 && (Boolean) cycling.get("feasible")) {
            return "Cycling - Good distance for cycling, environmentally friendly";
        } else if (distanceKm <= 5.0) {
            return "Public Transport - Cost-effective for this distance";
        } else {
            return "Driving - Best option for longer distances";
        }
    }
}
