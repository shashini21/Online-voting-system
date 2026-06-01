package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.Nominee;
import com.votingsystem.voting_system.entity.Ticket;
import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.repository.NomineeRepository;
import com.votingsystem.voting_system.service.EventService;
import com.votingsystem.voting_system.service.TicketService;
import com.votingsystem.voting_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Optional;

@Controller
@RequestMapping("/nominee")
public class NomineeController {

    @Autowired
    private EventService eventService;

    @Autowired
    private NomineeRepository nomineeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    private final Path uploadRoot = Paths.get("uploads");

    @GetMapping("/test")
    public String testDashboard(Model model) {
        System.out.println("=== TEST DASHBOARD ACCESSED ===");
        model.addAttribute("message", "Test dashboard is working!");
        return "nominee/test";
    }

    @GetMapping({"/dashboard", "/{username}/dashboard"})
    public String dashboard(@PathVariable(required = false) String username, Model model, Authentication authentication) {
        try {
            System.out.println("=== NOMINEE DASHBOARD ACCESSED ===");
            System.out.println("Username: " + username);
            System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
            System.out.println("User Role: " + (authentication != null ? authentication.getAuthorities() : "null"));
            
            // Load events data
            var activeEvents = eventService.getActiveEvents();
            var allEvents = eventService.getAllEvents();
            var closedEvents = allEvents.stream()
                .filter(event -> event.isVotingClosed())
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Active Events: " + (activeEvents != null ? activeEvents.size() : 0));
            System.out.println("All Events: " + (allEvents != null ? allEvents.size() : 0));
            
            model.addAttribute("activeEvents", activeEvents);
            model.addAttribute("allEvents", allEvents);
            model.addAttribute("closedEvents", closedEvents);
            
            // Add Quick Stats data for the dashboard
            System.out.println("Adding statistics to model...");
            model.addAttribute("activeEventsCount", activeEvents != null ? activeEvents.size() : 0);
            model.addAttribute("publishedResultsCount", closedEvents != null ? closedEvents.size() : 0);
            
            System.out.println("Active Events Count: " + (activeEvents != null ? activeEvents.size() : 0));
            System.out.println("Published Results Count: " + (closedEvents != null ? closedEvents.size() : 0));
            
            // Get user-specific statistics
            if (authentication != null && authentication.getPrincipal() != null) {
                User currentUser = (User) authentication.getPrincipal();
                System.out.println("Current user: " + currentUser.getUsername());
                
                // Add user to model for template access
                model.addAttribute("user", currentUser);
                
                // Support tickets count for current user
                var userTickets = ticketService.getTicketsByUser(currentUser.getId());
                model.addAttribute("supportTicketsCount", userTickets != null ? userTickets.size() : 0);
                System.out.println("Support Tickets Count: " + (userTickets != null ? userTickets.size() : 0));
                
                // Total votes count - get votes for all events
                int totalVotes = 0;
                if (allEvents != null) {
                    System.out.println("Processing " + allEvents.size() + " events for vote counting...");
                    for (Event event : allEvents) {
                        if (event.getNominees() != null) {
                            int eventVotes = event.getNominees().stream()
                                .mapToInt(nominee -> nominee.getVoteCount())
                                .sum();
                            totalVotes += eventVotes;
                            System.out.println("Event '" + event.getTitle() + "' has " + eventVotes + " votes");
                        }
                    }
                }
                model.addAttribute("totalVotesCount", totalVotes);
                System.out.println("Total Votes Count: " + totalVotes);
            } else {
                model.addAttribute("supportTicketsCount", 0);
                model.addAttribute("totalVotesCount", 0);
                System.out.println("No authentication - setting counts to 0");
            }
            
            // Determine the owner (username from URL or authenticated user)
            String owner = username != null ? username : (authentication != null ? authentication.getName() : null);
            System.out.println("Owner: " + owner);
            
            // Load nominations for the owner
            if (owner != null) {
                try {
                    // Try the custom query first
                    var nominations = nomineeRepository.findByUsernameCustom(owner);
                    System.out.println("Nominations found with custom query: " + (nominations != null ? nominations.size() : 0));
                    
                    if (nominations == null || nominations.isEmpty()) {
                        // Try the original method as fallback
                        nominations = nomineeRepository.findByUserUsernameOrderByEventDateDescCategoryAscNomineeNameAsc(owner);
                        System.out.println("Nominations found with original method: " + (nominations != null ? nominations.size() : 0));
                    }
                    
                    if (nominations != null && !nominations.isEmpty()) {
                        System.out.println("First nomination: " + nominations.get(0).getNomineeName());
                        System.out.println("Nomination event: " + nominations.get(0).getEvent().getTitle());
                    }
                    
                    model.addAttribute("nominations", nominations);
                } catch (Exception e) {
                    System.err.println("Error loading nominations: " + e.getMessage());
                    e.printStackTrace();
                    model.addAttribute("nominations", new java.util.ArrayList<>());
                }
            } else {
                System.out.println("No owner found - setting empty nominations");
                model.addAttribute("nominations", new java.util.ArrayList<>());
            }
            
            if (username != null) {
                model.addAttribute("panelOwner", username);
            }
            
            System.out.println("=== RETURNING TO TEMPLATE ===");
            return "nominee/dashboard";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN DASHBOARD ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            // Return error page
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/submit")
    public String showSubmissionForm(@RequestParam(value = "eventId", required = false) Long eventId, Model model) {
        var activeEvents = eventService.getActiveEvents();
        model.addAttribute("activeEvents", activeEvents);
        if (eventId != null) {
            Event selected = eventService.getEventById(eventId);
            model.addAttribute("event", selected);
        }
        model.addAttribute("nominee", new Nominee());
        return "nominee/submit";
    }

    @PostMapping("/submit")
    public String submitNomination(@RequestParam("eventId") Long eventId,
                                   @ModelAttribute Nominee nominee,
                                   @RequestParam(value = "media", required = false) MultipartFile media,
                                   Authentication authentication,
                                   Model model) throws IOException {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return "redirect:/nominee/dashboard";
        }
        nominee.setEvent(event);
        if (authentication != null && authentication.isAuthenticated()) {
            com.votingsystem.voting_system.entity.User current = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            nominee.setUser(current);
        }

        if (media != null && !media.isEmpty()) {
            Files.createDirectories(uploadRoot);
            String ext = org.springframework.util.StringUtils.getFilenameExtension(media.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path target = uploadRoot.resolve(filename);
            Files.copy(media.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            nominee.setMediaUrl("/uploads/" + filename);
            String contentType = media.getContentType();
            nominee.setMediaType(contentType != null && contentType.startsWith("video") ? "video" : "image");
        }

        nomineeRepository.save(nominee);
        String owner = authentication != null ? authentication.getName() : null;
        return owner != null ? "redirect:/nominee/" + owner + "/dashboard" : "redirect:/nominee/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editNomination(@PathVariable Long id, Model model) {
        Nominee nominee = nomineeRepository.findById(id).orElse(null);
        if (nominee == null) { return "redirect:/nominee/dashboard"; }
        model.addAttribute("event", nominee.getEvent());
        model.addAttribute("activeEvents", eventService.getActiveEvents());
        model.addAttribute("nominee", nominee);
        return "nominee/submit";
    }

    @PostMapping("/update/{id}")
    public String updateNomination(@PathVariable Long id,
                                  @RequestParam("eventId") Long eventId,
                                  @ModelAttribute Nominee nominee,
                                  @RequestParam(value = "media", required = false) MultipartFile media,
                                  Authentication authentication,
                                  Model model) throws IOException {
        Nominee existingNominee = nomineeRepository.findById(id).orElse(null);
        if (existingNominee == null) {
            return "redirect:/nominee/dashboard";
        }

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return "redirect:/nominee/dashboard";
        }

        // Update the existing nominee with new data
        existingNominee.setEvent(event);
        existingNominee.setCategory(nominee.getCategory());
        existingNominee.setNomineeName(nominee.getNomineeName());
        existingNominee.setDescription(nominee.getDescription());

        // Handle media update
        if (media != null && !media.isEmpty()) {
            Files.createDirectories(uploadRoot);
            String ext = org.springframework.util.StringUtils.getFilenameExtension(media.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path target = uploadRoot.resolve(filename);
            Files.copy(media.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            existingNominee.setMediaUrl("/uploads/" + filename);
            String contentType = media.getContentType();
            existingNominee.setMediaType(contentType != null && contentType.startsWith("video") ? "video" : "image");
        }

        nomineeRepository.save(existingNominee);
        String owner = authentication != null ? authentication.getName() : null;
        return owner != null ? "redirect:/nominee/" + owner + "/dashboard" : "redirect:/nominee/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteNomination(@PathVariable Long id) {
        nomineeRepository.deleteById(id);
        return "redirect:/nominee/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("user", authentication.getPrincipal());
        return "nominee/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            com.votingsystem.voting_system.entity.User current = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            
            // Enhanced validation with detailed error messages
            StringBuilder validationErrors = new StringBuilder();
            
            // Validate Username
            if (username == null || username.trim().isEmpty()) {
                validationErrors.append("Username is required. ");
            } else {
                String trimmedUsername = username.trim();
                if (trimmedUsername.length() < 3) {
                    validationErrors.append("Username must be at least 3 characters long. ");
                }
                if (trimmedUsername.length() > 30) {
                    validationErrors.append("Username cannot exceed 30 characters. ");
                }
                if (!trimmedUsername.matches("^[A-Za-z0-9_.-]+$")) {
                    validationErrors.append("Username can only contain letters, numbers, dots, hyphens, and underscores. ");
                }
            }
            
            // Validate Email
            if (email == null || email.trim().isEmpty()) {
                validationErrors.append("Email is required. ");
            } else {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    validationErrors.append("Please enter a valid email address. ");
                }
                if (trimmedEmail.length() > 100) {
                    validationErrors.append("Email cannot exceed 100 characters. ");
                }
            }
            
            // Validate Password (only if provided)
            if (password != null && !password.trim().isEmpty()) {
                String trimmedPassword = password.trim();
                if (trimmedPassword.length() < 8) {
                    validationErrors.append("Password must be at least 8 characters long. ");
                }
                if (trimmedPassword.length() > 100) {
                    validationErrors.append("Password cannot exceed 100 characters. ");
                }
                if (!trimmedPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$")) {
                    validationErrors.append("Password must contain at least one uppercase letter, one lowercase letter, and one number. ");
                }
            }
            
            // Check for validation errors
            if (validationErrors.length() > 0) {
                redirectAttributes.addFlashAttribute("error", validationErrors.toString().trim());
                return "redirect:/nominee/profile";
            }
            
            // Check if email is already taken by another user
            if (email != null && !email.trim().isEmpty()) {
                Optional<com.votingsystem.voting_system.entity.User> existingUserByEmail = userService.getUserByEmail(email.trim());
                if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(current.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Email is already taken by another user");
                    return "redirect:/nominee/profile";
                }
            }
            
            // Check if username is already taken by another user
            if (username != null && !username.trim().isEmpty()) {
                Optional<com.votingsystem.voting_system.entity.User> existingUserByUsername = userService.getUserByUsername(username.trim());
                if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(current.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Username is already taken by another user");
                    return "redirect:/nominee/profile";
                }
            }
            
            // Update user profile
            current.setUsername(username != null ? username.trim() : "");
            current.setEmail(email != null ? email.trim() : "");
            if (password != null && !password.trim().isEmpty()) {
                current.setPassword(password.trim());
            }
            
            // Use userService to persist changes
            userService.updateUser(current);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/nominee/profile";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + ex.getMessage());
            return "redirect:/nominee/profile";
        }
    }

    @GetMapping("/tickets")
    public String tickets(Model model, Authentication authentication) {
        com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
        model.addAttribute("tickets", ticketService.getTicketsByUser(user.getId()));
        return "nominee/tickets";
    }

    @GetMapping("/tickets/new")
    public String newTicket(Model model) {
        model.addAttribute("ticket", new com.votingsystem.voting_system.entity.Ticket());
        return "nominee/ticket-form";
    }

    @PostMapping("/tickets")
    public String createTicket(@ModelAttribute com.votingsystem.voting_system.entity.Ticket ticket, Authentication authentication, 
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            
            // Enhanced validation
            StringBuilder validationErrors = new StringBuilder();
            
            // Check subject
            if (ticket.getSubject() == null || ticket.getSubject().trim().isEmpty()) {
                validationErrors.append("Subject is required. ");
            }

            // Check description
            if (ticket.getDescription() == null || ticket.getDescription().trim().isEmpty()) {
                validationErrors.append("Description is required. ");
            }

            if (validationErrors.length() > 0) {
                redirectAttributes.addFlashAttribute("error", validationErrors.toString().trim());
                redirectAttributes.addFlashAttribute("ticket", ticket);
                return "redirect:/nominee/tickets/new";
            }

            // Validate subject length
            if (ticket.getSubject().length() < 5 || ticket.getSubject().length() > 100) {
                redirectAttributes.addFlashAttribute("error", "Subject must be between 5 and 100 characters long.");
                redirectAttributes.addFlashAttribute("ticket", ticket);
                return "redirect:/nominee/tickets/new";
            }

            // Validate description length
            if (ticket.getDescription().length() < 20 || ticket.getDescription().length() > 2000) {
                redirectAttributes.addFlashAttribute("error", "Description must be between 20 and 2000 characters long.");
                redirectAttributes.addFlashAttribute("ticket", ticket);
                return "redirect:/nominee/tickets/new";
            }

            ticket.setUser(user);
            ticketService.createTicket(ticket);
            redirectAttributes.addFlashAttribute("success", "Support ticket created successfully! We'll respond within 24 hours.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
            redirectAttributes.addFlashAttribute("ticket", ticket);
            return "redirect:/nominee/tickets/new";
        }
        return "redirect:/nominee/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String ticketDetails(@PathVariable Long id, Model model, Authentication authentication, 
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        System.out.println("=== NOMINEE TICKET DETAILS ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please log in to view ticket details");
            return "redirect:/nominee/tickets";
        }
        
        try {
            com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
            
            com.votingsystem.voting_system.entity.Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? "Yes" : "No"));
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/nominee/tickets";
            }
            
            System.out.println("Ticket user ID: " + ticket.getUser().getId());
            System.out.println("Current user ID: " + user.getId());
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                System.out.println("User mismatch - ticket belongs to different user");
                redirectAttributes.addFlashAttribute("error", "You can only view your own tickets");
                return "redirect:/nominee/tickets";
            }
            
            System.out.println("All checks passed - rendering ticket details");
            model.addAttribute("ticket", ticket);
            return "nominee/ticket-details";
            
        } catch (Exception e) {
            System.err.println("Error in nominee ticket details: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading ticket details: " + e.getMessage());
            return "redirect:/nominee/tickets";
        }
    }

    @GetMapping("/tickets/{id}/edit")
    public String editTicket(@PathVariable Long id, Model model, Authentication authentication, 
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            com.votingsystem.voting_system.entity.Ticket ticket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/nominee/tickets";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own tickets");
                return "redirect:/nominee/tickets";
            }
            
            // Only allow editing of OPEN tickets
            if (ticket.getStatus() != com.votingsystem.voting_system.entity.Ticket.Status.OPEN) {
                redirectAttributes.addFlashAttribute("error", "Only open tickets can be edited");
                return "redirect:/nominee/tickets";
            }
            
            model.addAttribute("ticket", ticket);
            return "nominee/ticket-edit";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/nominee/tickets";
        }
    }

    @PostMapping("/tickets/{id}/update")
    public String updateTicket(@PathVariable Long id, @ModelAttribute com.votingsystem.voting_system.entity.Ticket updatedTicket, 
                             Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            com.votingsystem.voting_system.entity.Ticket existingTicket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (existingTicket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/nominee/tickets";
            }
            
            if (!existingTicket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own tickets");
                return "redirect:/nominee/tickets";
            }
            
            // Only allow editing of OPEN tickets
            if (existingTicket.getStatus() != com.votingsystem.voting_system.entity.Ticket.Status.OPEN) {
                redirectAttributes.addFlashAttribute("error", "Only open tickets can be edited");
                return "redirect:/nominee/tickets";
            }
            
            // Update the ticket fields
            existingTicket.setSubject(updatedTicket.getSubject());
            existingTicket.setDescription(updatedTicket.getDescription());
            
            ticketService.updateTicket(existingTicket);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update ticket: " + e.getMessage());
        }
        return "redirect:/nominee/tickets";
    }
    
    @GetMapping("/tickets/{id}/debug")
    @ResponseBody
    public String debugTicket(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "Not authenticated";
        }
        
        User user = (User) authentication.getPrincipal();
        Ticket ticket = ticketService.getTicketById(id);
        
        if (ticket == null) {
            return "Ticket not found";
        }
        
        return String.format("Ticket ID: %d, User ID: %d, Current User ID: %d, Status: %s, Owner: %s", 
            ticket.getId(), 
            ticket.getUser().getId(), 
            user.getId(), 
            ticket.getStatus(), 
            ticket.getUser().getUsername());
    }
    
    @GetMapping("/tickets/test-create")
    @ResponseBody
    public String testCreateTicket(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "Not authenticated";
        }
        
        User user = (User) authentication.getPrincipal();
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setSubject("Test Ticket for Deletion");
        ticket.setDescription("This is a test ticket to verify deletion functionality");
        ticket.setStatus(Ticket.Status.OPEN);
        
        Ticket savedTicket = ticketService.createTicket(ticket);
        return "Test ticket created with ID: " + savedTicket.getId() + ". Try deleting it now!";
    }
    
    @GetMapping("/tickets/{id}/simple-delete")
    @ResponseBody
    public String simpleDeleteTicket(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "Not authenticated";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket ticket = ticketService.getTicketById(id);
            
            if (ticket == null) {
                return "Ticket not found";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                return "You can only delete your own tickets";
            }
            
            ticketService.deleteTicket(id);
            return "Ticket " + id + " deleted successfully!";
            
        } catch (Exception e) {
            return "Error deleting ticket: " + e.getMessage();
        }
    }
    
    @GetMapping("/debug/stats")
    @ResponseBody
    public String debugStats(Authentication authentication) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== NOMINEE DASHBOARD STATS DEBUG ===\n");
        
        try {
            // Test active events
            var activeEvents = eventService.getActiveEvents();
            debug.append("Active Events: ").append(activeEvents != null ? activeEvents.size() : 0).append("\n");
            if (activeEvents != null && !activeEvents.isEmpty()) {
                for (Event event : activeEvents) {
                    debug.append("  - ").append(event.getTitle()).append(" (ID: ").append(event.getId()).append(")\n");
                }
            }
            
            // Test all events
            var allEvents = eventService.getAllEvents();
            debug.append("All Events: ").append(allEvents != null ? allEvents.size() : 0).append("\n");
            if (allEvents != null && !allEvents.isEmpty()) {
                for (Event event : allEvents) {
                    debug.append("  - ").append(event.getTitle()).append(" (ID: ").append(event.getId())
                         .append(", Voting Closed: ").append(event.isVotingClosed()).append(")\n");
                }
            }
            
            // Test closed events
            var closedEvents = allEvents != null ? allEvents.stream()
                .filter(event -> event.isVotingClosed())
                .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
            debug.append("Closed Events: ").append(closedEvents.size()).append("\n");
            
            // Test user tickets
            if (authentication != null && authentication.getPrincipal() != null) {
                User currentUser = (User) authentication.getPrincipal();
                debug.append("Current User: ").append(currentUser.getUsername()).append("\n");
                
                var userTickets = ticketService.getTicketsByUser(currentUser.getId());
                debug.append("User Tickets: ").append(userTickets != null ? userTickets.size() : 0).append("\n");
                
                // Test vote counting
                int totalVotes = 0;
                if (allEvents != null) {
                    for (Event event : allEvents) {
                        if (event.getNominees() != null) {
                            int eventVotes = event.getNominees().stream()
                                .mapToInt(nominee -> nominee.getVoteCount())
                                .sum();
                            totalVotes += eventVotes;
                            debug.append("Event '").append(event.getTitle()).append("' has ").append(eventVotes).append(" votes\n");
                        }
                    }
                }
                debug.append("Total Votes: ").append(totalVotes).append("\n");
            } else {
                debug.append("No authentication found\n");
            }
            
        } catch (Exception e) {
            debug.append("ERROR: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }
        
        return debug.toString();
    }
    
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        System.out.println("Delete ticket endpoint called for ID: " + id);
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - user not logged in");
            redirectAttributes.addFlashAttribute("error", "Please log in to delete tickets");
            return "redirect:/nominee/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            System.out.println("User attempting to delete ticket: " + user.getUsername());
            
            Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? ticket.getId() : "null"));
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/nominee/tickets";
            }
            
            System.out.println("Ticket user ID: " + ticket.getUser().getId() + ", Current user ID: " + user.getId());
            if (!ticket.getUser().getId().equals(user.getId())) {
                System.out.println("User ownership check failed");
                redirectAttributes.addFlashAttribute("error", "You can only delete your own tickets");
                return "redirect:/nominee/tickets";
            }
            
            // Allow deletion of OPEN and RESOLVED tickets
            System.out.println("Ticket status: " + ticket.getStatus());
            if (ticket.getStatus() != Ticket.Status.OPEN && ticket.getStatus() != Ticket.Status.RESOLVED) {
                redirectAttributes.addFlashAttribute("error", "You can only delete open or resolved tickets");
                return "redirect:/nominee/tickets";
            }
            
            System.out.println("Deleting ticket ID: " + id);
            ticketService.deleteTicket(id);
            System.out.println("Ticket deleted successfully");
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully");
            
        } catch (Exception e) {
            System.out.println("Exception occurred while deleting ticket: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        
        return "redirect:/nominee/tickets";
    }
}


