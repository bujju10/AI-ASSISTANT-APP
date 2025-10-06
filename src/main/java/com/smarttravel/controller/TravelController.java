package com.smarttravel.controller;

import com.smarttravel.model.Route;
import com.smarttravel.service.RouteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TravelController {

    private final RouteService routeService;

    public TravelController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Route> routes = routeService.getAllRoutes();
        model.addAttribute("routes", routes);
        model.addAttribute("newRoute", new Route());
        return "index";
    }

    @PostMapping("/addRoute")
    public String addRoute(@ModelAttribute Route route) {
        routeService.saveRoute(route);
        return "redirect:/";
    }
}
