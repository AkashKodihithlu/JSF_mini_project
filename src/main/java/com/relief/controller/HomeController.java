package com.relief.controller;

import com.relief.dao.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserDAO userDAO;

    public HomeController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projectName", "Disaster Relief Coordination System");
        try {
            model.addAttribute("userCount", userDAO.findAll().size());
            model.addAttribute("dbStatus", "Connected");
        } catch (Exception e) {
            model.addAttribute("userCount", 0);
            model.addAttribute("dbStatus", "Disconnected: " + e.getMessage());
        }
        return "index";
    }
}
