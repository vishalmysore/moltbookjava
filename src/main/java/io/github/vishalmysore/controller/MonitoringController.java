package io.github.vishalmysore.controller;

import io.github.vishalmysore.service.ActivityTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoringController {

    @Autowired
    private ActivityTrackingService activityTrackingService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("activities", activityTrackingService.getRecentActivities());
        model.addAttribute("stats", activityTrackingService.getStats());
        return "dashboard";
    }
}
