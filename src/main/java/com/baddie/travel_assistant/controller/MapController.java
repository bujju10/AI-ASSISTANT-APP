package com.baddie.travel_assistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @GetMapping("/")
    public String home() {
        return "index"; // loads index.html
    }

    @GetMapping("/map")
    public String mapPage() {
        return "map"; // loads map.html
    }
}
