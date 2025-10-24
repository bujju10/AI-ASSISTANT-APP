package com.smarttravel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.smarttravel.service.SearchFreeService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LLMController {
    // Change this to your LM Studio LLM endpoint
    private static final String LLM_API_URL = "http://127.0.0.1:1234/v1/chat/completions";
    // Enhanced system prompt for smarter travel assistance with internet data
    private static final String DEFAULT_SYSTEM_PROMPT =
        "You are an advanced AI travel planning assistant with access to real-time data. "
        + "When analyzing travel routes, you should: "
        + "1) Analyze the provided route coordinates and real-time context data; "
        + "2) Consider current weather conditions, traffic patterns, and local events; "
        + "3) Recommend the best transportation modes with detailed pros/cons; "
        + "4) Provide estimated travel times, costs, and environmental impact; "
        + "5) Suggest alternative routes if available; "
        + "6) Include local tips, safety considerations, and accessibility information; "
        + "7) Mention any current roadblocks, construction, or events that might affect travel; "
        + "8) Consider time of day, day of week, and seasonal factors; "
        + "9) Provide practical advice for the specific locations mentioned; "
        + "10) Format your response clearly with sections for different aspects of the journey. "
        + "Be comprehensive but concise, and always prioritize safety and efficiency.";
    // store current system prompt in-memory
    private static volatile String CURRENT_SYSTEM_PROMPT = DEFAULT_SYSTEM_PROMPT;

    @PostMapping("/route-suggest")
    public ResponseEntity<?> getRouteSuggestion(@RequestBody Map<String, Object> payload) {
        String prompt = (String) payload.get("prompt");
        // allow caller to pass a custom system prompt; otherwise use default
    String systemPrompt = payload.containsKey("system") ? (String) payload.get("system") : CURRENT_SYSTEM_PROMPT;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-2-7b-chat-hf-function-calling-v2");
        // build messages: system first, then user
        java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>(); sysMsg.put("role", "system"); sysMsg.put("content", systemPrompt);
        Map<String, String> userMsg = new HashMap<>(); userMsg.put("role", "user"); userMsg.put("content", prompt);
        messages.add(sysMsg); messages.add(userMsg);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 256);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(LLM_API_URL, entity, java.util.Map.class);
            java.util.Map<?,?> rawBody = response.getBody();
            java.util.Map<String,Object> body = null;
            if (rawBody != null) {
                body = new java.util.HashMap<>();
                for (java.util.Map.Entry<?,?> e : rawBody.entrySet()) {
                    Object k = e.getKey(); if (k != null) body.put(String.valueOf(k), e.getValue());
                }
            }
            if (body != null && body.containsKey("choices")) {
                return ResponseEntity.ok(body);
            } else if (body != null && body.containsKey("error")) {
                return ResponseEntity.status(500).body(java.util.Map.of("error", String.valueOf(body.get("error"))));
            } else {
                return ResponseEntity.status(500).body(java.util.Map.of("error", "Unexpected LLM response: " + String.valueOf(body)));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "LLM communication failed: " + e.getMessage()));
        }
    }

    @GetMapping("/system")
    public ResponseEntity<?> getSystemPrompt() {
        return ResponseEntity.ok(java.util.Map.of("system", CURRENT_SYSTEM_PROMPT));
    }

    @PostMapping("/system")
    public ResponseEntity<?> setSystemPrompt(@RequestBody Map<String,String> body) {
        String s = body.getOrDefault("system", "").trim();
        if (s.isEmpty()) return ResponseEntity.badRequest().body("system prompt required");
        CURRENT_SYSTEM_PROMPT = s;
        return ResponseEntity.ok(java.util.Map.of("system", CURRENT_SYSTEM_PROMPT));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithLLM(@RequestBody Map<String, Object> payload) {
        // Enhanced chat with optional location context
        String prompt = (String) payload.getOrDefault("prompt", "");
        String origin = (String) payload.getOrDefault("origin", "");
        String destination = (String) payload.getOrDefault("destination", "");
        
        // If origin and destination are provided, use enhanced search
        if ((origin != null && !origin.isBlank()) || (destination != null && !destination.isBlank())) {
            Map<String, Object> enhancedPayload = new HashMap<>();
            enhancedPayload.put("prompt", prompt);
            enhancedPayload.put("origin", origin);
            enhancedPayload.put("destination", destination);
            return searchFreeAndAsk(enhancedPayload);
        }
        
        // Otherwise use regular chat
        return getRouteSuggestion(payload);
    }

    /**
     * Free augmentation endpoint: uses Wikipedia opensearch to fetch short descriptions for
     * origin and destination (if provided) and includes those snippets in the prompt sent
     * to the LLM. This lets us augment the LLM with free web knowledge without paid APIs.
     * Request body: { "prompt": "...", "origin": "City A", "destination": "City B" }
     */
    @Autowired
    private SearchFreeService searchFreeService;

    @PostMapping("/search-free")
    public ResponseEntity<?> searchFreeAndAsk(@RequestBody Map<String, Object> payload) {
        String prompt = (String) payload.getOrDefault("prompt", "");
        String origin = (String) payload.getOrDefault("origin", "");
        String destination = (String) payload.getOrDefault("destination", "");

        java.util.List<Map<String,String>> searchResults = new java.util.ArrayList<>();
        try {
            if (origin != null && !origin.isBlank()) {
                Map<String,String> r = searchFreeService.getComprehensivePlaceInfo(origin);
                if (r != null) searchResults.add(r);
            }
            if (destination != null && !destination.isBlank()) {
                Map<String,String> r = searchFreeService.getComprehensivePlaceInfo(destination);
                if (r != null) searchResults.add(r);
            }
        } catch (Exception e) {
            System.err.println("search-free service failed: " + e.getMessage());
        }

        // Build enhanced augmented prompt with comprehensive real-time data
        StringBuilder aug = new StringBuilder();
        if (!searchResults.isEmpty()) {
            aug.append("🌍 COMPREHENSIVE TRAVEL ANALYSIS CONTEXT:\n");
            aug.append("Current time: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            for (Map<String,String> s : searchResults) {
                String placeName = s.getOrDefault("display_name", s.getOrDefault("query","Unknown Location"));
                aug.append("📍 ").append(placeName).append(":\n");
                
                // Basic location info
                if (s.containsKey("lat") && s.containsKey("lon")) {
                    aug.append("   Coordinates: ").append(s.get("lat")).append(", ").append(s.get("lon")).append("\n");
                }
                
                // Wikidata description if available
                if (s.containsKey("wikidata_description")) {
                    aug.append("   Description: ").append(s.get("wikidata_description")).append("\n");
                }
                
                // Travel tips
                if (s.containsKey("travel_tip")) {
                    aug.append("   💡 Travel Tip: ").append(s.get("travel_tip")).append("\n");
                }
                
                // Enhanced real-time data
                if (s.containsKey("day_of_week")) {
                    aug.append("   📅 Day: ").append(s.get("day_of_week")).append(" (").append(s.get("time_of_day")).append(")\n");
                }
                if (s.containsKey("season")) {
                    aug.append("   🌿 Season: ").append(s.get("season")).append("\n");
                }
                if (s.containsKey("traffic_pattern")) {
                    aug.append("   🚦 Traffic: ").append(s.get("traffic_pattern")).append("\n");
                }
                if (s.containsKey("weather_considerations")) {
                    aug.append("   🌤️ Weather: ").append(s.get("weather_considerations")).append("\n");
                }
                if (s.containsKey("local_events")) {
                    aug.append("   📅 Events: ").append(s.get("local_events")).append("\n");
                }
                if (s.containsKey("accessibility")) {
                    aug.append("   ♿ Accessibility: ").append(s.get("accessibility")).append("\n");
                }
                if (s.containsKey("parking_info")) {
                    aug.append("   🅿️ Parking: ").append(s.get("parking_info")).append("\n");
                }
                if (s.containsKey("public_transport")) {
                    aug.append("   🚌 Public Transport: ").append(s.get("public_transport")).append("\n");
                }
                
                aug.append("\n");
            }
            aug.append("📋 DETAILED ANALYSIS REQUEST:\n");
        }
        aug.append(prompt);

        Map<String,Object> call = new HashMap<>();
        call.put("prompt", aug.toString());
        if (payload.containsKey("system")) call.put("system", payload.get("system"));
        return getRouteSuggestion(call);
    }

    
}
