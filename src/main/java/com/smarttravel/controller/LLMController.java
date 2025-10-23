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
    // Default system prompt that guides the assistant behavior. Can be overridden by sending
    // a `system` field in the request payload.
    private static final String DEFAULT_SYSTEM_PROMPT =
        "You are a helpful travel planning assistant. "
        + "When given route coordinates and context, you should: "
        + "1) Propose the top 3 route options with concise pros/cons for each; "
        + "2) Recommend the best mode of transportation (considering time, cost, safety, and CO2); "
        + "3) Return a short human-readable recommendation and (optionally) a JSON block under 'structured' "
        + "with keys: best_route_index, recommended_mode, estimated_time_minutes, estimated_cost, and an explanation. "
        + "Keep answers concise unless the user asks for more details.";
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
        // Accepts { "prompt": "..." }
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
                Map<String,String> r = searchFreeService.enrichPlace(origin);
                if (r != null) searchResults.add(r);
            }
            if (destination != null && !destination.isBlank()) {
                Map<String,String> r = searchFreeService.enrichPlace(destination);
                if (r != null) searchResults.add(r);
            }
        } catch (Exception e) {
            System.err.println("search-free service failed: " + e.getMessage());
        }

        // Build augmented prompt
        StringBuilder aug = new StringBuilder();
        if (!searchResults.isEmpty()) {
            aug.append("Context from local free sources:\n");
            for (Map<String,String> s : searchResults) {
                aug.append("- ").append(s.getOrDefault("display_name", s.getOrDefault("query","?"))).append(": ")
                   .append(s.getOrDefault("wikidata_description", s.getOrDefault("description","(no description)")))
                   .append(" (lat:").append(s.getOrDefault("lat","?")).append(", lon:").append(s.getOrDefault("lon","?")).append(")\n");
            }
            aug.append("\n");
        }
        aug.append(prompt);

        Map<String,Object> call = new HashMap<>();
        call.put("prompt", aug.toString());
        if (payload.containsKey("system")) call.put("system", payload.get("system"));
        return getRouteSuggestion(call);
    }

    
}
