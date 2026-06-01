package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.*;
import com.votingsystem.voting_system.service.*;
import com.votingsystem.voting_system.repository.NomineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ResultService resultService;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private VoteService voteService;

    @Autowired
    private NomineeRepository nomineeRepository;
    
    @Autowired
    private ExportService exportService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalEvents", eventService.getAllEvents().size());
        model.addAttribute("activeEvents", eventService.getActiveEvents().size());
        model.addAttribute("openTickets", ticketService.getOpenTickets().size());
        return "admin/dashboard";
    }
    
    // User Management
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "admin/user-form";
    }
    
    @PostMapping("/users")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Validate user data
            String validationError = validateUser(user, true);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("error", validationError);
                model.addAttribute("user", user);
                model.addAttribute("roles", User.Role.values());
                return "admin/user-form";
            }
            
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            return "admin/user-form";
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id).orElse(new User()));
        model.addAttribute("roles", User.Role.values());
        return "admin/user-form";
    }
    
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Validate user data
            String validationError = validateUser(user, false);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("error", validationError);
                model.addAttribute("user", user);
                model.addAttribute("roles", User.Role.values());
                return "admin/user-form";
            }
            
            user.setId(id);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            return "admin/user-form";
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    // Event Management
    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/events";
    }
    
    @GetMapping("/events/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new Event());
        return "admin/event-form";
    }
    
    @PostMapping("/events")
    public String saveEvent(@ModelAttribute Event event, Model model, RedirectAttributes redirectAttributes) {
        try {
            eventService.createEvent(event);
            redirectAttributes.addFlashAttribute("success", "Event created successfully");
            return "redirect:/admin/events";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("event", event);
            return "admin/event-form";
        }
    }
    
    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "admin/event-form";
    }
    
    @PostMapping("/events/{id}")
    public String updateEvent(@PathVariable Long id, @ModelAttribute Event event, Model model, RedirectAttributes redirectAttributes) {
        try {
            event.setId(id);
            eventService.updateEvent(event);
            redirectAttributes.addFlashAttribute("success", "Event updated successfully");
            return "redirect:/admin/events";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("event", event);
            return "admin/event-form";
        }
    }
    
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("success", "Event deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events";
    }
    
    // Results Management
    @GetMapping("/results")
    public String results(Model model) {
        try {
            System.out.println("=== ADMIN RESULTS ENDPOINT CALLED ===");
            var results = resultService.getAllResults();
            var events = eventService.getAllEvents();
            
            System.out.println("Results loaded: " + (results != null ? results.size() : "null"));
            System.out.println("Events loaded: " + (events != null ? events.size() : "null"));

            // Debug logging
            System.out.println("=== RESULTS DEBUG ===");
            System.out.println("Total events: " + (events != null ? events.size() : "null"));
            System.out.println("Total results: " + (results != null ? results.size() : "null"));
        
            if (events != null) {
                for (Event e : events) {
                    if (e != null) {
                        System.out.println("Event: " + e.getTitle() + 
                                         " | Voting Open: " + e.isVotingOpen() + 
                                         " | Nomination Open: " + e.isNominationOpen() + 
                                         " | Voting Closed: " + e.isVotingClosed() +
                                         " | Date: " + e.getDate() +
                                         " | Nomination Deadline: " + e.getNominationDeadline() +
                                         " | Voting Deadline: " + e.getVotingDeadline());
                    }
                }
            }
        
            // Debug pending results logic
            long pendingCount = 0;
            if (events != null) {
                pendingCount = events.stream()
                    .filter(e -> e != null && !e.isVotingOpen())
                    .count();
            }
            System.out.println("Events with pending results: " + pendingCount);
            
            // Debug published results
            System.out.println("Published results count: " + (results != null ? results.size() : 0));
            if (results != null) {
                for (Result r : results) {
                    if (r != null && r.getEvent() != null) {
                        System.out.println("Result for event: " + r.getEvent().getTitle() + 
                                         " | Total votes: " + r.getTotalVotes() + 
                                         " | Generated at: " + r.getGeneratedAt());
                    }
                }
            }

            // Build counts for nominees and votes per event for the Pending Results table
            java.util.Map<Long, Long> voteCountsByEvent = new java.util.HashMap<>();
            java.util.Map<Long, Long> nomineeCountsByEvent = new java.util.HashMap<>();
            java.util.Map<Long, java.util.Map<String, Long>> nomineeVotesByEvent = new java.util.HashMap<>();
            
            if (events != null) {
                for (Event e : events) {
                    if (e != null && e.getId() != null) {
                        try {
                            long voteCount = voteService.getVoteCountForEvent(e.getId());
                            long nomineeCount = nomineeRepository.countByEventId(e.getId());
                            java.util.Map<String, Long> nomineeVotes = resultService.getVoteCountsByNominee(e.getId());
                            
                            voteCountsByEvent.put(e.getId(), voteCount);
                            nomineeCountsByEvent.put(e.getId(), nomineeCount);
                            nomineeVotesByEvent.put(e.getId(), nomineeVotes != null ? nomineeVotes : new java.util.HashMap<>());
                            
                            System.out.println("Event " + e.getTitle() + " (ID: " + e.getId() + "): " + 
                                             voteCount + " votes, " + nomineeCount + " nominees, " + 
                                             (nomineeVotes != null ? nomineeVotes.size() : 0) + " nominee vote entries");
                        } catch (Exception ex) {
                            System.err.println("Error processing event " + e.getTitle() + ": " + ex.getMessage());
                            ex.printStackTrace();
                            // Set default values to prevent null pointer exceptions
                            voteCountsByEvent.put(e.getId(), 0L);
                            nomineeCountsByEvent.put(e.getId(), 0L);
                            nomineeVotesByEvent.put(e.getId(), new java.util.HashMap<>());
                        }
                    }
                }
            }

            model.addAttribute("results", results);
            model.addAttribute("events", events);
            model.addAttribute("voteCountsByEvent", voteCountsByEvent);
            model.addAttribute("nomineeCountsByEvent", nomineeCountsByEvent);
            model.addAttribute("nomineeVotesByEvent", nomineeVotesByEvent);
            
            System.out.println("=== MODEL ATTRIBUTES SET ===");
            System.out.println("Returning admin/results template");
            return "admin/results";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN RESULTS ENDPOINT ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading results: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/results/event/{eventId}")
    public String resultsByEvent(@PathVariable Long eventId, Model model) {
        try {
            System.out.println("=== RESULTS BY EVENT ENDPOINT CALLED ===");
            System.out.println("Event ID: " + eventId);
            
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                System.err.println("Event not found for ID: " + eventId);
                return "redirect:/admin/results";
            }
            
            System.out.println("Event found: " + event.getTitle());
            
            // Get nominee vote counts with null safety
            var nomineeVoteCounts = resultService.getNomineeVoteCounts(eventId);
            var voteCountsByCategory = resultService.getVoteCountsByCategoryAndNominee(eventId);
            var voteCountsByNominee = resultService.getVoteCountsByNominee(eventId);
            var totalVotes = voteCountsByNominee != null ? 
                voteCountsByNominee.values().stream().mapToLong(Long::longValue).sum() : 0L;
            
            // Debug logging
            System.out.println("Nominee Vote Counts: " + (nomineeVoteCounts != null ? nomineeVoteCounts.size() : "null"));
            System.out.println("Vote Counts by Category: " + (voteCountsByCategory != null ? voteCountsByCategory.size() : "null"));
            System.out.println("Total Votes: " + totalVotes);
            
            model.addAttribute("event", event);
            model.addAttribute("voteCountsByCategory", voteCountsByCategory != null ? voteCountsByCategory : java.util.Collections.emptyMap());
            model.addAttribute("nomineeVoteCounts", nomineeVoteCounts != null ? nomineeVoteCounts : java.util.Collections.emptyList());
            model.addAttribute("totalVotes", totalVotes);
            
            System.out.println("=== MODEL ATTRIBUTES SET ===");
            return "admin/results-detail";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN RESULTS BY EVENT ENDPOINT ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading event details: " + e.getMessage());
            return "redirect:/admin/results";
        }
    }
    
    @PostMapping("/results/publish/{eventId}")
    public String publishResults(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== PUBLISH RESULTS ENDPOINT CALLED ===");
            System.out.println("Event ID: " + eventId);
            
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                redirectAttributes.addFlashAttribute("error", "Event not found");
                return "redirect:/admin/results";
            }
            
            if (!event.isVotingClosed()) {
                redirectAttributes.addFlashAttribute("error", "Cannot publish results while voting is still open");
                return "redirect:/admin/results";
            }
            
            // Check if results exist for this event
            Result existingResult = resultService.getLatestResultByEvent(eventId);
            if (existingResult == null) {
                redirectAttributes.addFlashAttribute("error", "No results found for this event. Please generate results first.");
                return "redirect:/admin/results";
            }
            
            // Check if results are already published
            if (existingResult.getPublished()) {
                redirectAttributes.addFlashAttribute("error", "Results for this event are already published");
                return "redirect:/admin/results";
            }
            
            // Publish the results
            resultService.publishResults(eventId);
            redirectAttributes.addFlashAttribute("success", "Results published successfully! They are now visible to voters.");
            
            System.out.println("Results published successfully for event: " + event.getTitle());
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN PUBLISH RESULTS ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to publish results: " + e.getMessage());
        }
        return "redirect:/admin/results";
    }
    
    @PostMapping("/results/generate/{eventId}")
    public String generateResults(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            resultService.generateResults(eventId);
            redirectAttributes.addFlashAttribute("success", "Results generated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/results";
    }
    
    @GetMapping("/results/export/pdf/{resultId}")
    public ResponseEntity<ByteArrayResource> exportResultsPdf(@PathVariable Long resultId) {
        try {
            byte[] pdfBytes = exportService.exportResultsToPdf(resultId);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voting-results-" + resultId + ".txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/results/export/excel/{resultId}")
    public ResponseEntity<ByteArrayResource> exportResultsExcel(@PathVariable Long resultId) {
        try {
            byte[] excelBytes = exportService.exportResultsToExcel(resultId);
            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voting-results-" + resultId + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelBytes.length)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/results/delete/{resultId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteResult(@PathVariable Long resultId, RedirectAttributes redirectAttributes) {
        try {
            resultService.deleteResult(resultId);
            redirectAttributes.addFlashAttribute("success", "Published result deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete result: " + e.getMessage());
        }
        return "redirect:/admin/results";
    }
    
    @GetMapping("/tickets")
    public String tickets(Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets());
        return "admin/tickets";
    }
    
    @GetMapping("/tickets/{id}")
    public String ticketDetails(@PathVariable Long id, Model model, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        System.out.println("=== ADMIN TICKET DETAILS ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please log in to view ticket details");
            return "redirect:/admin/tickets";
        }
        
        try {
            com.votingsystem.voting_system.entity.User user = (com.votingsystem.voting_system.entity.User) authentication.getPrincipal();
            System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
            
            // Check if user is admin
            if (user.getRole() != com.votingsystem.voting_system.entity.User.Role.ADMIN) {
                System.out.println("User is not admin - access denied");
                redirectAttributes.addFlashAttribute("error", "Access denied. Admin privileges required.");
                return "redirect:/admin/tickets";
            }
            
            com.votingsystem.voting_system.entity.Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? "Yes" : "No"));
            
            // Check if ticket exists
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/admin/tickets";
            }
            
            System.out.println("All checks passed - rendering admin ticket details");
            model.addAttribute("ticket", ticket);
            return "admin/ticket-details";
            
        } catch (Exception e) {
            System.err.println("Error in admin ticket details: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading ticket details: " + e.getMessage());
            return "redirect:/admin/tickets";
        }
    }
    
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        return "redirect:/admin/tickets";
    }
    
    @GetMapping("/results/winners/{eventId}")
    public String viewWinners(@PathVariable Long eventId, Model model) {
        try {
            System.out.println("=== ADMIN VIEW WINNERS ENDPOINT CALLED ===");
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                System.err.println("Event not found for ID: " + eventId);
                return "redirect:/admin/results";
            }
            
            System.out.println("Event found: " + event.getTitle());
            
            // Get winner data
            var nomineeVoteCounts = resultService.getNomineeVoteCounts(eventId);
            var voteCountsByCategory = resultService.getVoteCountsByCategoryAndNominee(eventId);
            var voteCountsByNominee = resultService.getVoteCountsByNominee(eventId);
            var totalVotes = voteCountsByNominee != null ? 
                voteCountsByNominee.values().stream().mapToLong(Long::longValue).sum() : 0L;
            
            // Debug logging
            System.out.println("Nominee Vote Counts: " + (nomineeVoteCounts != null ? nomineeVoteCounts.size() : "null"));
            System.out.println("Vote Counts by Category: " + (voteCountsByCategory != null ? voteCountsByCategory.size() : "null"));
            System.out.println("Total Votes: " + totalVotes);
            
            model.addAttribute("event", event);
            model.addAttribute("voteCountsByCategory", voteCountsByCategory != null ? voteCountsByCategory : java.util.Collections.emptyMap());
            model.addAttribute("nomineeVoteCounts", nomineeVoteCounts != null ? nomineeVoteCounts : java.util.Collections.emptyList());
            model.addAttribute("totalVotes", totalVotes);
            
            System.out.println("=== MODEL ATTRIBUTES SET ===");
            return "admin/winners";
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN VIEW WINNERS ENDPOINT ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading winner details: " + e.getMessage());
            return "redirect:/admin/results";
        }
    }
    
    @GetMapping("/debug/voting/{eventId}")
    public String debugVoting(@PathVariable Long eventId, Model model) {
        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                model.addAttribute("error", "Event not found");
                return "error";
            }
            
            String currentTime = java.time.LocalDateTime.now().toString();
            String nominationDeadline = event.getNominationDeadline().toString();
            String votingDeadline = event.getVotingDeadline().toString();
            
            model.addAttribute("event", event);
            model.addAttribute("currentTime", currentTime);
            model.addAttribute("nominationDeadline", nominationDeadline);
            model.addAttribute("votingDeadline", votingDeadline);
            model.addAttribute("isNominationOpen", event.isNominationOpen());
            model.addAttribute("isVotingOpen", event.isVotingOpen());
            model.addAttribute("isVotingClosed", event.isVotingClosed());
            
            return "admin/debug-voting";
            
        } catch (Exception e) {
            model.addAttribute("error", "Debug error: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Validates user data for both create and update operations
     * @param user The user object to validate
     * @param isCreate True if creating a new user, false if updating
     * @return Error message if validation fails, null if validation passes
     */
    private String validateUser(User user, boolean isCreate) {
        StringBuilder errors = new StringBuilder();
        
        // Validate Username
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            errors.append("Username is required. ");
        } else {
            String username = user.getUsername().trim();
            if (username.length() < 3) {
                errors.append("Username must be at least 3 characters long. ");
            }
            if (username.length() > 30) {
                errors.append("Username cannot exceed 30 characters. ");
            }
            if (!username.matches("^[A-Za-z0-9_.-]+$")) {
                errors.append("Username can only contain letters, numbers, dots, hyphens, and underscores. ");
            }
        }
        
        // Validate Email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.append("Email is required. ");
        } else {
            String email = user.getEmail().trim();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                errors.append("Please enter a valid email address. ");
            }
            if (email.length() > 100) {
                errors.append("Email cannot exceed 100 characters. ");
            }
        }
        
        // Validate Password
        if (isCreate) {
            // For new users, password is required
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                errors.append("Password is required. ");
            } else {
                String password = user.getPassword();
                if (password.length() < 8) {
                    errors.append("Password must be at least 8 characters long. ");
                }
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    errors.append("Password must contain at least one letter and one number. ");
                }
            }
        } else {
            // For updates, if password is provided, validate it
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String password = user.getPassword();
                if (password.length() < 8) {
                    errors.append("Password must be at least 8 characters long. ");
                }
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    errors.append("Password must contain at least one letter and one number. ");
                }
            }
        }
        
        // Validate Role
        if (user.getRole() == null) {
            errors.append("Role is required. ");
        }
        
        // Check for duplicate username (only for new users or if username changed)
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            try {
                var existingUser = userService.getUserByUsername(user.getUsername().trim());
                if (existingUser.isPresent() && (isCreate || !existingUser.get().getId().equals(user.getId()))) {
                    errors.append("Username already exists. ");
                }
            } catch (Exception e) {
                // Ignore service exceptions during validation
            }
        }
        
        // Check for duplicate email (only for new users or if email changed)
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            try {
                var existingUser = userService.getUserByEmail(user.getEmail().trim());
                if (existingUser.isPresent() && (isCreate || !existingUser.get().getId().equals(user.getId()))) {
                    errors.append("Email already exists. ");
                }
            } catch (Exception e) {
                // Ignore service exceptions during validation
            }
        }
        
        return errors.length() > 0 ? errors.toString().trim() : null;
    }
}
