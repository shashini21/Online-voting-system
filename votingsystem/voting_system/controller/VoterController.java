package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.*;
import com.votingsystem.voting_system.service.*;
import com.votingsystem.voting_system.repository.NomineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/voter")
public class VoterController {
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private VoteService voteService;
    
    @Autowired
    private NomineeRepository nomineeRepository;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private ResultService resultService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/test")
    public String test() {
        System.out.println("=== VOTER TEST ENDPOINT CALLED ===");
        return "voter/test";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        System.out.println("=== VOTER DASHBOARD ENDPOINT CALLED ===");
        
        try {
            User user = null;
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
                System.out.println("User authenticated: " + user.getUsername());
            } else {
                System.out.println("No authentication found, using null user");
            }
            
            // Fetch real event data
            List<Event> activeEvents = eventService.getActiveEvents();
            List<Event> allEvents = eventService.getAllEvents();
            
            // Debug: Print all events and their status
            System.out.println("=== EVENT DEBUG INFO ===");
            System.out.println("Current time: " + java.time.LocalDateTime.now());
            if (allEvents != null) {
                System.out.println("Total events in database: " + allEvents.size());
                for (Event event : allEvents) {
                    System.out.println("Event: " + event.getTitle());
                    System.out.println("  - Date: " + event.getDate());
                    System.out.println("  - Nomination Deadline: " + event.getNominationDeadline());
                    System.out.println("  - Voting Deadline: " + event.getVotingDeadline());
                    System.out.println("  - Is Active: " + (event.getVotingDeadline().isAfter(java.time.LocalDateTime.now())));
                    System.out.println("  - Is Nomination Open: " + event.isNominationOpen());
                    System.out.println("  - Is Voting Open: " + event.isVotingOpen());
                    System.out.println("  - Is Voting Closed: " + event.isVotingClosed());
                }
            } else {
                System.out.println("No events found in database");
            }
            System.out.println("Active events count: " + (activeEvents != null ? activeEvents.size() : 0));
            System.out.println("========================");
            
            // Calculate statistics
            int activeEventsCount = activeEvents != null ? activeEvents.size() : 0;
            int totalVotesCount = 0;
            int supportTicketsCount = 0;
            int publishedResultsCount = 0;
            
            // Calculate total votes across all events
            if (allEvents != null) {
                for (Event event : allEvents) {
                    try {
                        totalVotesCount += voteService.getVoteCountForEvent(event.getId());
                    } catch (Exception e) {
                        System.err.println("Error getting vote count for event " + event.getId() + ": " + e.getMessage());
                    }
                }
                
                // Count events with published results
                for (Event event : allEvents) {
                    if (event.isVotingClosed()) {
                        publishedResultsCount++;
                    }
                }
            }
            
            // Get support tickets count for the user
            if (user != null && user.getId() != null) {
                try {
                    supportTicketsCount = ticketService.getTicketsByUser(user.getId()).size();
                } catch (Exception e) {
                    System.err.println("Error getting support tickets count: " + e.getMessage());
                    supportTicketsCount = 0;
                }
            }
            
            System.out.println("Active Events: " + activeEventsCount);
            System.out.println("Total Events: " + (allEvents != null ? allEvents.size() : 0));
            System.out.println("Total Votes: " + totalVotesCount);
            System.out.println("Published Results: " + publishedResultsCount);
            System.out.println("Support Tickets: " + supportTicketsCount);
            
            // Add real data to model
            model.addAttribute("user", user);
            model.addAttribute("activeEventsCount", activeEventsCount);
            model.addAttribute("publishedResultsCount", publishedResultsCount);
            model.addAttribute("totalVotesCount", totalVotesCount);
            model.addAttribute("supportTicketsCount", supportTicketsCount);
            model.addAttribute("activeEvents", activeEvents != null ? activeEvents : new ArrayList<>());
            model.addAttribute("allEvents", allEvents != null ? allEvents : new ArrayList<>());
            
            System.out.println("=== RETURNING VOTER DASHBOARD TEMPLATE ===");
            return "voter/dashboard";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN VOTER DASHBOARD ===");
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            
            // Fallback to empty data
            model.addAttribute("user", null);
            model.addAttribute("activeEventsCount", 0);
            model.addAttribute("publishedResultsCount", 0);
            model.addAttribute("totalVotesCount", 0);
            model.addAttribute("supportTicketsCount", 0);
            model.addAttribute("activeEvents", new ArrayList<>());
            model.addAttribute("allEvents", new ArrayList<>());
            
            return "voter/dashboard";
        }
    }
    
    @GetMapping("/events")
    public String events(Model model) {
        try {
            System.out.println("=== VOTER EVENTS ENDPOINT CALLED ===");
            
            List<Event> activeEvents = eventService.getActiveEvents();
            System.out.println("Found " + (activeEvents != null ? activeEvents.size() : 0) + " active events");
            
            if (activeEvents == null) {
                activeEvents = new ArrayList<>();
            }
            
            model.addAttribute("events", activeEvents);
            
            System.out.println("=== MODEL ATTRIBUTES SET ===");
            return "voter/events";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN VOTER EVENTS ENDPOINT ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading events: " + e.getMessage());
            model.addAttribute("events", new ArrayList<>());
            return "voter/events";
        }
    }
    
    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            System.out.println("=== VOTER EVENT DETAILS ENDPOINT CALLED ===");
            System.out.println("Event ID: " + id);
            
            if (id == null) {
                System.err.println("Event ID is null");
                return "redirect:/voter/events";
            }
            
            Event event = eventService.getEventById(id);
            if (event == null) {
                System.err.println("Event not found for ID: " + id);
                return "redirect:/voter/events";
            }
            
            System.out.println("Event found: " + event.getTitle());
            
            User user = null;
            boolean hasVoted = false;
            
            if (authentication != null && authentication.getPrincipal() != null) {
                user = (User) authentication.getPrincipal();
                if (user != null && user.getId() != null) {
                    hasVoted = voteService.hasUserVotedForEvent(user.getId(), id);
                    System.out.println("User " + user.getUsername() + " has voted: " + hasVoted);
            
            // Debug voting timeline
            System.out.println("=== VOTING DEBUG INFO ===");
            System.out.println("Event: " + event.getTitle());
            System.out.println("Current time: " + java.time.LocalDateTime.now());
            System.out.println("Nomination deadline: " + event.getNominationDeadline());
            System.out.println("Voting deadline: " + event.getVotingDeadline());
            System.out.println("Is nomination open: " + event.isNominationOpen());
            System.out.println("Is voting open: " + event.isVotingOpen());
            System.out.println("Is voting closed: " + event.isVotingClosed());
            System.out.println("=========================");
                }
            }
            
            List<Nominee> nominees = null;
            List<String> categories = null;
            
            try {
                nominees = nomineeRepository.findByEventIdOrderByCategoryAndName(id);
                categories = nomineeRepository.findDistinctCategoriesByEventId(id);
                System.out.println("Found " + (nominees != null ? nominees.size() : 0) + " nominees and " + (categories != null ? categories.size() : 0) + " categories");
            } catch (Exception e) {
                System.err.println("Error fetching nominees/categories: " + e.getMessage());
                e.printStackTrace();
                nominees = new ArrayList<>();
                categories = new ArrayList<>();
            }
            
            model.addAttribute("event", event);
            model.addAttribute("hasVoted", hasVoted);
            model.addAttribute("nominees", nominees);
            model.addAttribute("categories", categories);
            
            System.out.println("=== MODEL ATTRIBUTES SET ===");
            return "voter/event-details";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN VOTER EVENT DETAILS ENDPOINT ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading event details: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/events/{id}/results")
    public String eventResults(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            return "redirect:/voter/events";
        }
        
        // Get published result if available
        Result result = resultService.getLatestResultByEvent(id);
        
        // Get live vote counts by category (always available)
        var voteCountsByCategory = resultService.getVoteCountsByCategoryAndNominee(id);
        var nomineeVoteCounts = resultService.getNomineeVoteCounts(id);
        var totalVotes = voteService.getVoteCountForEvent(id);
        
        model.addAttribute("event", event);
        model.addAttribute("result", result);
        model.addAttribute("byCategory", voteCountsByCategory);
        model.addAttribute("nomineeVoteCounts", nomineeVoteCounts);
        model.addAttribute("totalVotes", totalVotes);
        
        return "voter/event-results";
    }
    
    @GetMapping("/events/{id}/winners")
    public String eventWinners(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            return "redirect:/voter/events";
        }
        
        // Get published result if available
        Result result = resultService.getLatestResultByEvent(id);
        
        // Get vote counts and rankings
        var voteCountsByCategory = resultService.getVoteCountsByCategoryAndNominee(id);
        var nomineeVoteCounts = resultService.getNomineeVoteCounts(id);
        var totalVotes = voteService.getVoteCountForEvent(id);
        
        model.addAttribute("event", event);
        model.addAttribute("result", result);
        model.addAttribute("byCategory", voteCountsByCategory);
        model.addAttribute("nomineeVoteCounts", nomineeVoteCounts);
        model.addAttribute("totalVotes", totalVotes);
        
        return "voter/event-results";
    }
    
    @GetMapping("/events/{eventId}/vote/{nomineeId}")
    public String vote(@PathVariable Long eventId, @PathVariable Long nomineeId, 
                      Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to vote");
            return "redirect:/voter/events/" + eventId;
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            voteService.castVote(user.getId(), nomineeId, eventId);
            redirectAttributes.addFlashAttribute("success", "Vote cast successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/voter/events/" + eventId;
    }
    
    @GetMapping("/winners")
    public String winners(Model model) {
        // Get all events with results
        List<Event> allEvents = eventService.getAllEvents();
        List<Event> eventsWithResults = new ArrayList<>();
        
        // Filter events that have voting closed and results available
        for (Event event : allEvents) {
            if (event.isVotingClosed()) {
                long voteCount = voteService.getVoteCountForEvent(event.getId());
                if (voteCount > 0) {
                    eventsWithResults.add(event);
                }
            }
        }
        
        // Get overall statistics
        long totalVotesAcrossAllEvents = 0;
        int totalNomineesAcrossAllEvents = 0;
        
        for (Event event : eventsWithResults) {
            totalVotesAcrossAllEvents += voteService.getVoteCountForEvent(event.getId());
            var nomineeVoteCounts = resultService.getNomineeVoteCounts(event.getId());
            if (nomineeVoteCounts != null) {
                totalNomineesAcrossAllEvents += nomineeVoteCounts.size();
            }
        }
        
        model.addAttribute("events", eventsWithResults);
        model.addAttribute("totalEvents", eventsWithResults.size());
        model.addAttribute("totalVotes", totalVotesAcrossAllEvents);
        model.addAttribute("totalNominees", totalNomineesAcrossAllEvents);
        
        return "voter/winners";
    }
    
    @GetMapping("/my-votes")
    public String myVotes(Model model, Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                model.addAttribute("error", "Please log in to view your votes");
                model.addAttribute("votes", new ArrayList<>());
                return "voter/my-votes";
            }
            
            User user = (User) authentication.getPrincipal();
            List<Vote> userVotes = voteService.getUserVotes(user.getId());
            model.addAttribute("votes", userVotes);
            return "voter/my-votes";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while loading your votes: " + e.getMessage());
            model.addAttribute("votes", new ArrayList<>());
            return "voter/my-votes";
        }
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            model.addAttribute("error", "Please log in to view your profile");
            return "voter/profile";
        }
        
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        return "voter/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user, 
                              @RequestParam(required = false) String password,
                              Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to update your profile");
            return "redirect:/voter/profile";
        }
        
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            // Enhanced validation with detailed error messages
            StringBuilder validationErrors = new StringBuilder();
            
            // Validate First Name
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                validationErrors.append("First name is required. ");
            } else {
                String firstName = user.getFirstName().trim();
                if (!firstName.matches("^[A-Za-z\\s]+$")) {
                    validationErrors.append("First name must contain only letters and spaces. ");
                }
                if (firstName.length() < 2) {
                    validationErrors.append("First name must be at least 2 characters long. ");
                }
                if (firstName.length() > 50) {
                    validationErrors.append("First name cannot exceed 50 characters. ");
                }
            }
            
            // Validate Last Name
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                validationErrors.append("Last name is required. ");
            } else {
                String lastName = user.getLastName().trim();
                if (!lastName.matches("^[A-Za-z\\s]+$")) {
                    validationErrors.append("Last name must contain only letters and spaces. ");
                }
                if (lastName.length() < 2) {
                    validationErrors.append("Last name must be at least 2 characters long. ");
                }
                if (lastName.length() > 50) {
                    validationErrors.append("Last name cannot exceed 50 characters. ");
                }
            }
            
            // Validate Email
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                validationErrors.append("Email is required. ");
            } else {
                String email = user.getEmail().trim();
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    validationErrors.append("Please enter a valid email address. ");
                }
                if (email.length() > 100) {
                    validationErrors.append("Email cannot exceed 100 characters. ");
                }
            }
            
            // Validate Username
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                validationErrors.append("Username is required. ");
            } else {
                String username = user.getUsername().trim();
                if (username.length() < 3) {
                    validationErrors.append("Username must be at least 3 characters long. ");
                }
                if (username.length() > 30) {
                    validationErrors.append("Username cannot exceed 30 characters. ");
                }
                if (!username.matches("^[A-Za-z0-9_.-]+$")) {
                    validationErrors.append("Username can only contain letters, numbers, dots, hyphens, and underscores. ");
                }
            }
            
            // Validate Phone Number
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                String phone = user.getPhone().trim();
                if (!phone.matches("^[0-9]{10}$")) {
                    validationErrors.append("Phone number must contain exactly 10 digits. ");
                }
            }
            
            // Validate Address
            if (user.getAddress() != null && !user.getAddress().trim().isEmpty()) {
                String address = user.getAddress().trim();
                if (address.length() > 500) {
                    validationErrors.append("Address cannot exceed 500 characters. ");
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
                return "redirect:/voter/profile";
            }
            
            // Check if email is already taken by another user
            Optional<User> existingUserByEmail = userService.getUserByEmail(user.getEmail().trim());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Email is already taken by another user");
                return "redirect:/voter/profile";
            }
            
            // Check if username is already taken by another user
            Optional<User> existingUserByUsername = userService.getUserByUsername(user.getUsername().trim());
            if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Username is already taken by another user");
                return "redirect:/voter/profile";
            }
            
            // Update the current user's profile with new information
            currentUser.setFirstName(user.getFirstName().trim());
            currentUser.setLastName(user.getLastName().trim());
            currentUser.setEmail(user.getEmail().trim());
            currentUser.setUsername(user.getUsername().trim());
            currentUser.setPhone(user.getPhone() != null ? user.getPhone().trim() : null);
            currentUser.setAddress(user.getAddress() != null ? user.getAddress().trim() : null);
            
            // Update password if provided
            if (password != null && !password.trim().isEmpty()) {
                currentUser.setPassword(password.trim());
            }
            
            // Save the updated user
            userService.updateUser(currentUser);
            
            // Set appropriate success message
            String successMessage = "Profile updated successfully!";
            if (password != null && !password.trim().isEmpty()) {
                successMessage += " Password has been changed.";
            }
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/voter/profile";
    }
    
    @GetMapping("/tickets")
    public String tickets(Model model, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            model.addAttribute("error", "Please log in to view your tickets");
            model.addAttribute("tickets", new ArrayList<>());
            return "voter/tickets";
        }
        
        User user = (User) authentication.getPrincipal();
        model.addAttribute("tickets", ticketService.getTicketsByUser(user.getId()));
        return "voter/tickets";
    }
    
    @GetMapping("/tickets/new")
    public String newTicket(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "voter/ticket-form";
    }
    
    @PostMapping("/tickets")
    public String createTicket(@ModelAttribute Ticket ticket, Authentication authentication, 
                             RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to create a ticket");
            return "redirect:/voter/tickets/new";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            
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
                return "redirect:/voter/tickets/new";
            }

            // Validate subject length
            if (ticket.getSubject().length() < 5 || ticket.getSubject().length() > 100) {
                redirectAttributes.addFlashAttribute("error", "Subject must be between 5 and 100 characters long.");
                redirectAttributes.addFlashAttribute("ticket", ticket);
                return "redirect:/voter/tickets/new";
            }

            // Validate description length
            if (ticket.getDescription().length() < 20 || ticket.getDescription().length() > 2000) {
                redirectAttributes.addFlashAttribute("error", "Description must be between 20 and 2000 characters long.");
                redirectAttributes.addFlashAttribute("ticket", ticket);
                return "redirect:/voter/tickets/new";
            }

            ticket.setUser(user);
            ticketService.createTicket(ticket);
            redirectAttributes.addFlashAttribute("success", "Support ticket created successfully! We'll respond within 24 hours.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
            redirectAttributes.addFlashAttribute("ticket", ticket);
            return "redirect:/voter/tickets/new";
        }
        return "redirect:/voter/tickets";
    }
    
    @GetMapping("/tickets/{id}")
    public String ticketDetails(@PathVariable Long id, Model model, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to view ticket details");
            return "redirect:/voter/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket ticket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/voter/tickets";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only view your own tickets");
                return "redirect:/voter/tickets";
            }
            
            model.addAttribute("ticket", ticket);
            return "voter/ticket-details";
            
        } catch (Exception e) {
            System.err.println("Error in voter ticket details: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading ticket details");
            return "redirect:/voter/tickets";
        }
    }
    
    @GetMapping("/tickets/{id}/edit")
    public String editTicket(@PathVariable Long id, Model model, Authentication authentication, 
                           RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to edit tickets");
            return "redirect:/voter/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket ticket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/voter/tickets";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own tickets");
                return "redirect:/voter/tickets";
            }
            
            // Only allow editing of OPEN tickets
            if (ticket.getStatus() != Ticket.Status.OPEN) {
                redirectAttributes.addFlashAttribute("error", "Only open tickets can be edited");
                return "redirect:/voter/tickets";
            }
            
            model.addAttribute("ticket", ticket);
            return "voter/ticket-edit";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/voter/tickets";
        }
    }
    
    @PostMapping("/tickets/{id}/update")
    public String updateTicket(@PathVariable Long id, @ModelAttribute Ticket updatedTicket, 
                             Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to update tickets");
            return "redirect:/voter/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket existingTicket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (existingTicket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/voter/tickets";
            }
            
            if (!existingTicket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own tickets");
                return "redirect:/voter/tickets";
            }
            
            // Only allow editing of OPEN tickets
            if (existingTicket.getStatus() != Ticket.Status.OPEN) {
                redirectAttributes.addFlashAttribute("error", "Only open tickets can be edited");
                return "redirect:/voter/tickets";
            }
            
            // Update the ticket fields
            existingTicket.setSubject(updatedTicket.getSubject());
            existingTicket.setDescription(updatedTicket.getDescription());
            
            ticketService.updateTicket(existingTicket);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update ticket: " + e.getMessage());
        }
        return "redirect:/voter/tickets";
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
    
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to delete tickets");
            return "redirect:/voter/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket ticket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/voter/tickets";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only delete your own tickets");
                return "redirect:/voter/tickets";
            }
            
            // Allow deletion of OPEN and RESOLVED tickets
            if (ticket.getStatus() != Ticket.Status.OPEN && ticket.getStatus() != Ticket.Status.RESOLVED) {
                redirectAttributes.addFlashAttribute("error", "You can only delete open or resolved tickets");
                return "redirect:/voter/tickets";
            }
            
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        
        return "redirect:/voter/tickets";
    }
    
    // GET endpoint for testing purposes (redirects to POST)
    @GetMapping("/tickets/{id}/delete")
    public String deleteTicketGet(@PathVariable Long id, Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        System.out.println("=== VOTER TICKET DELETE GET ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to delete tickets");
            return "redirect:/voter/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Ticket ticket = ticketService.getTicketById(id);
            
            // Check if ticket exists and belongs to the current user
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/voter/tickets";
            }
            
            if (!ticket.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only delete your own tickets");
                return "redirect:/voter/tickets";
            }
            
            // Allow deletion of OPEN and RESOLVED tickets
            if (ticket.getStatus() != Ticket.Status.OPEN && ticket.getStatus() != Ticket.Status.RESOLVED) {
                redirectAttributes.addFlashAttribute("error", "You can only delete open or resolved tickets");
                return "redirect:/voter/tickets";
            }
            
            System.out.println("All checks passed - deleting ticket via GET");
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully");
            
        } catch (Exception e) {
            System.err.println("Error in voter ticket delete (GET): " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        
        return "redirect:/voter/tickets";
    }
}
