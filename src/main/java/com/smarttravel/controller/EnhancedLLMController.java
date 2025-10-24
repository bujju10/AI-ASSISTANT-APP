package com.smarttravel.controller;

import com.smarttravel.service.RouteAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/enhanced-llm")
public class EnhancedLLMController {

    @Autowired
    private RouteAnalysisService routeAnalysisService;

    // Enhanced LLM endpoint with real data
    private static final String LLM_API_URL = "http://127.0.0.1:1234/v1/chat/completions";
    
    // Much smarter system prompt with real data context
    private static final String ENHANCED_SYSTEM_PROMPT = 
        "You are an advanced AI travel planning assistant with access to real-time data and comprehensive route analysis. " +
        "You have access to actual distance measurements, travel times, weather conditions, traffic patterns, and local events. " +
        "When analyzing travel routes, provide detailed, data-driven recommendations including: " +
        "1) Real distance and time calculations with multiple transportation options " +
        "2) Cost analysis for each transportation method " +
        "3) Weather and seasonal considerations " +
        "4) Current traffic conditions and expected delays " +
        "5) Local events and their potential impact " +
        "6) Accessibility and parking considerations " +
        "7) Environmental impact assessment " +
        "8) Safety recommendations " +
        "9) Alternative routes and backup options " +
        "10) Time-sensitive advice based on current conditions " +
        "Format your response with clear sections, use emojis for visual appeal, and provide actionable, specific advice. " +
        "Always prioritize safety, efficiency, and user convenience.";

    @PostMapping("/smart-route-analysis")
    public ResponseEntity<?> getSmartRouteAnalysis(@RequestBody Map<String, Object> payload) {
        try {
            String start = (String) payload.get("start");
            String end = (String) payload.get("end");
            
            if (start == null || end == null || start.trim().isEmpty() || end.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Start and end locations are required"));
            }
            
            // Get comprehensive real-time analysis
            Map<String, Object> routeAnalysis = routeAnalysisService.getComprehensiveRouteAnalysis(start, end);
            
            if (routeAnalysis.containsKey("error")) {
                return ResponseEntity.status(500).body(routeAnalysis);
            }
            
            // Create enhanced prompt with real data
            String enhancedPrompt = createEnhancedPrompt(routeAnalysis);
            
            // Send to LLM with real data context
            Map<String, Object> llmResponse = callEnhancedLLM(enhancedPrompt);
            
            // Combine real data with LLM insights
            Map<String, Object> response = new HashMap<>();
            response.put("real_data", routeAnalysis);
            response.put("ai_analysis", llmResponse);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/smart-chat")
    public ResponseEntity<?> smartChat(@RequestBody Map<String, Object> payload) {
        try {
            String prompt = (String) payload.get("prompt");
            String start = (String) payload.get("start");
            String end = (String) payload.get("end");
            
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
            }
            
            String enhancedPrompt = prompt;
            
            // If locations are provided, add real-time context
            if (start != null && end != null && !start.trim().isEmpty() && !end.trim().isEmpty()) {
                Map<String, Object> routeAnalysis = routeAnalysisService.getComprehensiveRouteAnalysis(start, end);
                if (!routeAnalysis.containsKey("error")) {
                    enhancedPrompt = createContextualPrompt(prompt, routeAnalysis);
                }
            }
            
            Map<String, Object> llmResponse = callEnhancedLLM(enhancedPrompt);
            return ResponseEntity.ok(llmResponse);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Chat failed: " + e.getMessage()));
        }
    }
    
    private String createEnhancedPrompt(Map<String, Object> routeAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("🌍 COMPREHENSIVE TRAVEL ANALYSIS REQUEST\n");
        prompt.append("Current Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        // Route Data
        Map<String, Object> routeData = (Map<String, Object>) routeAnalysis.get("route_data");
        if (routeData != null && !routeData.containsKey("error")) {
            prompt.append("📍 ROUTE INFORMATION:\n");
            prompt.append("Start: ").append(routeAnalysis.get("start_location")).append("\n");
            prompt.append("End: ").append(routeAnalysis.get("end_location")).append("\n");
            prompt.append("Distance: ").append(routeData.get("distance_km")).append(" km (").append(routeData.get("distance_miles")).append(" miles)\n");
            prompt.append("Driving Time: ").append(routeData.get("duration_minutes")).append(" minutes (").append(routeData.get("duration_hours")).append(" hours)\n\n");
        }
        
        // Weather Data
        Map<String, Object> weatherData = (Map<String, Object>) routeAnalysis.get("weather_data");
        if (weatherData != null) {
            prompt.append("🌤️ WEATHER CONDITIONS:\n");
            prompt.append("Season: ").append(weatherData.get("season")).append("\n");
            prompt.append("Time of Day: ").append(weatherData.get("time_of_day")).append("\n");
            prompt.append("Considerations: ").append(weatherData.get("weather_considerations")).append("\n");
            prompt.append("Temperature Impact: ").append(weatherData.get("temperature_impact")).append("\n");
            prompt.append("Precipitation Risk: ").append(weatherData.get("precipitation_risk")).append("\n\n");
        }
        
        // Traffic Data
        Map<String, Object> trafficData = (Map<String, Object>) routeAnalysis.get("traffic_data");
        if (trafficData != null) {
            prompt.append("🚦 TRAFFIC CONDITIONS:\n");
            prompt.append("Current Time: ").append(trafficData.get("current_time")).append("\n");
            prompt.append("Day: ").append(trafficData.get("day_of_week")).append("\n");
            prompt.append("Traffic Level: ").append(trafficData.get("traffic_level")).append("\n");
            prompt.append("Description: ").append(trafficData.get("traffic_description")).append("\n");
            prompt.append("Peak Hours: ").append(trafficData.get("peak_hours")).append("\n");
            prompt.append("Expected Delays: ").append(trafficData.get("expected_delays")).append("\n\n");
        }
        
        // Events Data
        Map<String, Object> eventsData = (Map<String, Object>) routeAnalysis.get("events_data");
        if (eventsData != null) {
            prompt.append("📅 LOCAL EVENTS & CONDITIONS:\n");
            prompt.append("Start Location: ").append(eventsData.get("start_location_events")).append("\n");
            prompt.append("End Location: ").append(eventsData.get("end_location_events")).append("\n");
            prompt.append("General Advice: ").append(eventsData.get("general_advice")).append("\n\n");
        }
        
        // Transportation Options
        Map<String, Object> transportOptions = (Map<String, Object>) routeAnalysis.get("transport_options");
        if (transportOptions != null) {
            prompt.append("🚗 TRANSPORTATION OPTIONS:\n");
            
            Map<String, Object> driving = (Map<String, Object>) transportOptions.get("driving");
            if (driving != null) {
                prompt.append("DRIVING: ").append(driving.get("distance_km")).append(" km, ")
                      .append(driving.get("duration_minutes")).append(" min, $")
                      .append(driving.get("fuel_cost_estimate")).append(" fuel cost\n");
            }
            
            Map<String, Object> publicTransport = (Map<String, Object>) transportOptions.get("public_transport");
            if (publicTransport != null) {
                prompt.append("PUBLIC TRANSPORT: ").append(publicTransport.get("distance_km")).append(" km, ")
                      .append(publicTransport.get("estimated_duration_minutes")).append(" min estimated\n");
            }
            
            Map<String, Object> walking = (Map<String, Object>) transportOptions.get("walking");
            if (walking != null) {
                prompt.append("WALKING: ").append(walking.get("distance_km")).append(" km, ")
                      .append(walking.get("duration_minutes")).append(" min, Feasible: ")
                      .append(walking.get("feasible")).append("\n");
            }
            
            Map<String, Object> cycling = (Map<String, Object>) transportOptions.get("cycling");
            if (cycling != null) {
                prompt.append("CYCLING: ").append(cycling.get("distance_km")).append(" km, ")
                      .append(cycling.get("duration_minutes")).append(" min, Feasible: ")
                      .append(cycling.get("feasible")).append("\n");
            }
            
            prompt.append("RECOMMENDED: ").append(transportOptions.get("recommended")).append("\n\n");
        }
        
        prompt.append("📋 ANALYSIS REQUEST:\n");
        prompt.append("Based on all this real-time data, provide a comprehensive travel analysis including:\n");
        prompt.append("1. Best transportation option with detailed reasoning\n");
        prompt.append("2. Expected total travel time including delays\n");
        prompt.append("3. Total cost breakdown\n");
        prompt.append("4. Weather and safety considerations\n");
        prompt.append("5. Alternative routes if available\n");
        prompt.append("6. Time-sensitive recommendations\n");
        prompt.append("7. Local tips and considerations\n");
        prompt.append("8. Environmental impact assessment\n");
        prompt.append("9. Accessibility information\n");
        prompt.append("10. Backup plans and contingencies\n");
        prompt.append("Format your response with clear sections and actionable advice.");
        
        return prompt.toString();
    }
    
    private String createContextualPrompt(String userPrompt, Map<String, Object> routeAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("🌍 CONTEXTUAL TRAVEL ASSISTANCE\n");
        prompt.append("Current Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        // Add relevant context based on the analysis
        Map<String, Object> routeData = (Map<String, Object>) routeAnalysis.get("route_data");
        if (routeData != null && !routeData.containsKey("error")) {
            prompt.append("📍 Current Route Context:\n");
            prompt.append("Distance: ").append(routeData.get("distance_km")).append(" km\n");
            prompt.append("Driving Time: ").append(routeData.get("duration_minutes")).append(" minutes\n\n");
        }
        
        Map<String, Object> trafficData = (Map<String, Object>) routeAnalysis.get("traffic_data");
        if (trafficData != null) {
            prompt.append("🚦 Current Traffic: ").append(trafficData.get("traffic_level")).append(" - ")
                  .append(trafficData.get("traffic_description")).append("\n\n");
        }
        
        prompt.append("💬 User Question: ").append(userPrompt).append("\n\n");
        prompt.append("Please provide helpful, context-aware advice based on the current route and conditions.");
        
        return prompt.toString();
    }
    
    private Map<String, Object> callEnhancedLLM(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-2-7b-chat-hf-function-calling-v2");
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", ENHANCED_SYSTEM_PROMPT);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);
            
            Map<String, Object> entity = new HashMap<>();
            entity.put("request", requestBody);
            entity.put("headers", headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(LLM_API_URL, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        return Map.of(
                            "content", message.get("content"),
                            "role", message.get("role"),
                            "model", "enhanced-travel-assistant"
                        );
                    }
                }
            }
            
            return Map.of("error", "No valid response from LLM");
            
        } catch (Exception e) {
            return Map.of("error", "LLM communication failed: " + e.getMessage());
        }
    }
}
