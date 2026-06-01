package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.service.UserService;
import com.votingsystem.voting_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TestController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private EventService eventService;

    @GetMapping("/test/database")
    public String testDatabase(Model model) {
        try {
            // Test READ operations
            List<User> allUsers = userService.getAllUsers();
            long voterCount = userService.countByRole(User.Role.VOTER);
            long adminCount = userService.countByRole(User.Role.ADMIN);
            long nomineeCount = userService.countByRole(User.Role.NOMINEE);
            
            // Test Event operations
            List<Event> allEvents = eventService.getAllEvents();
            
            model.addAttribute("allUsers", allUsers);
            model.addAttribute("voterCount", voterCount);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("nomineeCount", nomineeCount);
            model.addAttribute("allEvents", allEvents);
            model.addAttribute("eventCount", allEvents.size());
            model.addAttribute("status", "Database connection successful! Events: " + allEvents.size());
            
        } catch (Exception e) {
            model.addAttribute("status", "Database error: " + e.getMessage());
            model.addAttribute("error", e);
        }
        
        return "test/database";
    }

    @PostMapping("/test/create-user")
    public String createTestUser(@RequestParam String username, 
                                @RequestParam String email, 
                                @RequestParam String password,
                                @RequestParam String role,
                                Model model) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(User.Role.valueOf(role.toUpperCase()));
            
            User savedUser = userService.createUser(user);
            model.addAttribute("success", "User created successfully with ID: " + savedUser.getId());
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
        }
        
        return "redirect:/test/database";
    }

    @PostMapping("/test/delete-user")
    public String deleteTestUser(@RequestParam Long userId, Model model) {
        try {
            userService.deleteUser(userId);
            model.addAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        
        return "redirect:/test/database";
    }
}
