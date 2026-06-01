package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public static class RegistrationForm {
        private String username;
        private String password;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @GetMapping({"/register"})
    public String showRegister(Model model) {
        model.addAttribute("voterForm", new RegistrationForm());
        model.addAttribute("nomineeForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register/voter")
    public String registerVoter(@ModelAttribute("voterForm") RegistrationForm form, Model model) {
        // Enhanced validation
        StringBuilder validationErrors = new StringBuilder();
        
        if (isBlank(form.getUsername())) {
            validationErrors.append("Username is required. ");
        }
        if (isBlank(form.getEmail())) {
            validationErrors.append("Email address is required. ");
        }
        if (isBlank(form.getPassword())) {
            validationErrors.append("Password is required. ");
        }

        if (validationErrors.length() > 0) {
            model.addAttribute("error", validationErrors.toString().trim());
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }

        // Validate formats
        if (!form.getUsername().matches("^[A-Za-z0-9_.-]{3,30}$")) {
            model.addAttribute("error", "Username must be 3-30 characters long and contain only letters, numbers, dots, underscores, and hyphens.");
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }

        if (!form.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }

        if (!form.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            model.addAttribute("error", "Password must be at least 8 characters long and contain at least one letter and one number.");
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }

        if (userService.existsByUsername(form.getUsername())) {
            model.addAttribute("error", "Username '" + form.getUsername() + "' is already taken. Please choose a different username.");
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }
        
        if (userService.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email address '" + form.getEmail() + "' is already registered. Please use a different email or try logging in.");
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setEmail(form.getEmail());
        user.setRole(User.Role.VOTER);
        
        try {
            userService.createUser(user);
            model.addAttribute("success", "Voter account created successfully! Please log in to continue.");
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Failed to create voter account: " + ex.getMessage());
            model.addAttribute("voterForm", form);
            model.addAttribute("nomineeForm", new RegistrationForm());
            return "register";
        }
        return "redirect:/login?registered=voter";
    }

    @PostMapping("/register/nominee")
    public String registerNominee(@ModelAttribute("nomineeForm") RegistrationForm form, Model model) {
        // Enhanced validation
        StringBuilder validationErrors = new StringBuilder();
        
        if (isBlank(form.getUsername())) {
            validationErrors.append("Username is required. ");
        }
        if (isBlank(form.getEmail())) {
            validationErrors.append("Email address is required. ");
        }
        if (isBlank(form.getPassword())) {
            validationErrors.append("Password is required. ");
        }

        if (validationErrors.length() > 0) {
            model.addAttribute("error", validationErrors.toString().trim());
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }

        // Validate formats
        if (!form.getUsername().matches("^[A-Za-z0-9_.-]{3,30}$")) {
            model.addAttribute("error", "Username must be 3-30 characters long and contain only letters, numbers, dots, underscores, and hyphens.");
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }

        if (!form.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }

        if (!form.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            model.addAttribute("error", "Password must be at least 8 characters long and contain at least one letter and one number.");
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }

        if (userService.existsByUsername(form.getUsername())) {
            model.addAttribute("error", "Username '" + form.getUsername() + "' is already taken. Please choose a different username.");
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }
        
        if (userService.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email address '" + form.getEmail() + "' is already registered. Please use a different email or try logging in.");
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setEmail(form.getEmail());
        user.setRole(User.Role.NOMINEE);
        
        try {
            userService.createUser(user);
            model.addAttribute("success", "Nominee account created successfully! You are now logged in.");
            
            // Auto-login nominee and redirect to nominee dashboard
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/nominee/dashboard";
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Failed to create nominee account: " + ex.getMessage());
            model.addAttribute("nomineeForm", form);
            model.addAttribute("voterForm", new RegistrationForm());
            return "register";
        }
    }

    @GetMapping("/register/admin")
    public String showAdminRegister(Model model) {
        model.addAttribute("adminForm", new RegistrationForm());
        return "register-admin";
    }

    @PostMapping("/register/admin")
    public String registerAdmin(@ModelAttribute("adminForm") RegistrationForm form, Model model) {
        // Enhanced validation with specific error messages
        StringBuilder validationErrors = new StringBuilder();
        
        // Check username
        if (isBlank(form.getUsername())) {
            validationErrors.append("Username is required. ");
        }

        // Check email
        if (isBlank(form.getEmail())) {
            validationErrors.append("Email address is required. ");
        }

        // Check password
        if (isBlank(form.getPassword())) {
            validationErrors.append("Password is required. ");
        }

        if (validationErrors.length() > 0) {
            model.addAttribute("error", validationErrors.toString().trim());
            model.addAttribute("adminForm", form); // Preserve form data
            return "register-admin";
        }

        // Validate username format
        if (form.getUsername() != null && !form.getUsername().matches("^[A-Za-z0-9_.-]{3,30}$")) {
            model.addAttribute("error", "Username must be 3-30 characters long and contain only letters, numbers, dots, underscores, and hyphens.");
            model.addAttribute("adminForm", form);
            return "register-admin";
        }

        // Validate email format
        if (form.getEmail() != null && !form.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            model.addAttribute("adminForm", form);
            return "register-admin";
        }

        // Validate password strength
        if (form.getPassword() != null && !form.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            model.addAttribute("error", "Password must be at least 8 characters long and contain at least one letter and one number.");
            model.addAttribute("adminForm", form);
            return "register-admin";
        }

        // Check for existing username
        if (userService.existsByUsername(form.getUsername())) {
            model.addAttribute("error", "Username '" + form.getUsername() + "' is already taken. Please choose a different username.");
            model.addAttribute("adminForm", form);
            return "register-admin";
        }

        // Check for existing email
        if (userService.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email address '" + form.getEmail() + "' is already registered. Please use a different email address or try logging in.");
            model.addAttribute("adminForm", form);
            return "register-admin";
        }

        // Create new admin user
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setEmail(form.getEmail());
        user.setRole(User.Role.ADMIN);
        
        try {
            userService.createUser(user);
            model.addAttribute("success", "Admin account created successfully! You are now logged in.");
            
            // Auto-login admin and redirect to admin dashboard
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/admin/dashboard";
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Failed to create admin account: " + ex.getMessage());
            model.addAttribute("adminForm", form);
            return "register-admin";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}


