package com.smarttravel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class RealTimeDataService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get enhanced real-time data for a location
     */
    public Map<String, String> getRealTimeData(String lat, String lon, String placeName) {
        Map<String, String> data = new HashMap<>();
        
        try {
            // Add current timestamp
            data.put("current_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            data.put("day_of_week", LocalDateTime.now().getDayOfWeek().toString());
            data.put("time_of_day", getTimeOfDay());
            
            // Add seasonal information
            data.put("season", getCurrentSeason());
            
            // Add traffic pattern information
            data.put("traffic_pattern", getTrafficPattern());
            
            // Add weather considerations
            data.put("weather_considerations", getWeatherConsiderations());
            
            // Add local events/construction notes
            data.put("local_events", getLocalEventsNote(placeName));
            
            // Add accessibility information
            data.put("accessibility", getAccessibilityInfo(placeName));
            
            // Add parking information
            data.put("parking_info", getParkingInfo(placeName));
            
            // Add public transport info
            data.put("public_transport", getPublicTransportInfo(placeName));
            
        } catch (Exception e) {
            System.err.println("Real-time data error: " + e.getMessage());
            data.put("error", "Unable to fetch real-time data: " + e.getMessage());
        }
        
        return data;
    }

    private String getTimeOfDay() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 21) return "Evening";
        return "Night";
    }

    private String getCurrentSeason() {
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5) return "Spring";
        if (month >= 6 && month <= 8) return "Summer";
        if (month >= 9 && month <= 11) return "Autumn";
        return "Winter";
    }

    private String getTrafficPattern() {
        int hour = LocalDateTime.now().getHour();
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        
        if (dayOfWeek >= 1 && dayOfWeek <= 5) { // Weekday
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "Peak hours - expect heavy traffic and delays";
            } else if (hour >= 10 && hour <= 16) {
                return "Moderate traffic - good travel conditions";
            } else {
                return "Light traffic - optimal travel conditions";
            }
        } else { // Weekend
            if (hour >= 10 && hour <= 18) {
                return "Weekend traffic - moderate congestion possible";
            } else {
                return "Light weekend traffic - good travel conditions";
            }
        }
    }

    private String getWeatherConsiderations() {
        String season = getCurrentSeason();
        switch (season) {
            case "Winter":
                return "Winter conditions - check for snow, ice, and road closures";
            case "Spring":
                return "Spring weather - possible rain and seasonal road maintenance";
            case "Summer":
                return "Summer conditions - good weather but watch for heat-related delays";
            case "Autumn":
                return "Autumn weather - possible rain and falling leaves affecting roads";
            default:
                return "Check current weather conditions before travel";
        }
    }

    private String getLocalEventsNote(String placeName) {
        if (placeName == null) return "Check local event calendars for potential traffic impacts";
        
        String lowerName = placeName.toLowerCase();
        if (lowerName.contains("downtown") || lowerName.contains("city center")) {
            return "Downtown area - check for events, festivals, or construction";
        } else if (lowerName.contains("airport")) {
            return "Airport area - check for flight delays and security wait times";
        } else if (lowerName.contains("university") || lowerName.contains("college")) {
            return "Educational area - consider student traffic patterns and events";
        } else if (lowerName.contains("hospital")) {
            return "Medical facility - emergency access routes may be affected";
        } else {
            return "Check local news and event calendars for current conditions";
        }
    }

    private String getAccessibilityInfo(String placeName) {
        if (placeName == null) return "Accessibility information varies by location";
        
        String lowerName = placeName.toLowerCase();
        if (lowerName.contains("airport")) {
            return "Airport accessibility - wheelchair access, elevators, and assistance available";
        } else if (lowerName.contains("station")) {
            return "Transit station - check for elevator status and accessibility features";
        } else if (lowerName.contains("hospital")) {
            return "Medical facility - full accessibility features available";
        } else {
            return "Check location-specific accessibility features and requirements";
        }
    }

    private String getParkingInfo(String placeName) {
        if (placeName == null) return "Parking availability varies by location and time";
        
        String lowerName = placeName.toLowerCase();
        if (lowerName.contains("downtown") || lowerName.contains("city center")) {
            return "Downtown parking - limited availability, consider public transport";
        } else if (lowerName.contains("airport")) {
            return "Airport parking - multiple options available, check rates and availability";
        } else if (lowerName.contains("university") || lowerName.contains("college")) {
            return "Campus parking - permits may be required, limited visitor parking";
        } else if (lowerName.contains("hospital")) {
            return "Hospital parking - usually available but may be limited during peak hours";
        } else {
            return "Check parking availability and restrictions for your destination";
        }
    }

    private String getPublicTransportInfo(String placeName) {
        if (placeName == null) return "Public transport options vary by location";
        
        String lowerName = placeName.toLowerCase();
        if (lowerName.contains("airport")) {
            return "Airport connections - buses, trains, and shuttles available";
        } else if (lowerName.contains("station")) {
            return "Transit hub - multiple transport options and connections";
        } else if (lowerName.contains("downtown") || lowerName.contains("city center")) {
            return "City center - extensive public transport network available";
        } else {
            return "Check local transit schedules and routes for your destination";
        }
    }
}
