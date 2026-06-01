package com.votingsystem.voting_system.controller;

import com.votingsystem.voting_system.entity.*;
import com.votingsystem.voting_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/support")
public class SupportController {
    
    @Autowired
    private TicketService ticketService;
    
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalTickets", ticketService.getAllTickets().size());
        model.addAttribute("openTickets", ticketService.getOpenTickets().size());
        model.addAttribute("resolvedTickets", ticketService.getResolvedTickets().size());
        model.addAttribute("escalatedTickets", ticketService.getEscalatedTickets().size());
        return "support/dashboard";
    }
    
    @GetMapping("/tickets")
    public String tickets(Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets());
        return "support/tickets";
    }
    
    @GetMapping("/tickets/{id}")
    public String ticketDetails(@PathVariable Long id, Model model, Authentication authentication) {
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            return "redirect:/support/tickets";
        }
        
        // Check if user is authenticated and redirect based on role
        if (authentication != null && authentication.getPrincipal() != null) {
            User user = (User) authentication.getPrincipal();
            
            // If the ticket belongs to this user, redirect them to their role-specific ticket page
            if (ticket.getUser().getId().equals(user.getId())) {
                switch (user.getRole()) {
                    case VOTER:
                        return "redirect:/voter/tickets/" + id;
                    case NOMINEE:
                        return "redirect:/nominee/tickets/" + id;
                    case ADMIN:
                        // Admins can stay on support page
                        break;
                    case SUPPORT:
                        // Support staff can stay on support page
                        break;
                }
            }
        }
        
        model.addAttribute("ticket", ticket);
        return "support/ticket-details";
    }
    
    @PostMapping("/tickets/{id}/resolve")
    public String resolveTicket(@PathVariable Long id, @RequestParam String response, 
                              RedirectAttributes redirectAttributes) {
        try {
            ticketService.resolveTicket(id, response);
            redirectAttributes.addFlashAttribute("success", "Ticket resolved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/support/tickets/" + id;
    }
    
    @PostMapping("/tickets/{id}/escalate")
    public String escalateTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.escalateTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket escalated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/support/tickets/" + id;
    }
    
    @GetMapping("/tickets/{id}/update")
    public String updateTicketForm(@PathVariable Long id, Model model, Authentication authentication, 
                                  RedirectAttributes redirectAttributes) {
        System.out.println("=== SUPPORT TICKET UPDATE FORM ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please log in to update tickets");
            return "redirect:/support/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
            
            // Check if user is support staff or admin
            if (user.getRole() != User.Role.SUPPORT && user.getRole() != User.Role.ADMIN) {
                System.out.println("User is not support staff or admin - access denied");
                redirectAttributes.addFlashAttribute("error", "Access denied. Support staff or admin privileges required.");
                return "redirect:/support/tickets";
            }
            
            Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? "Yes" : "No"));
            
            // Check if ticket exists
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/support/tickets";
            }
            
            System.out.println("All checks passed - rendering update form");
            model.addAttribute("ticket", ticket);
            return "support/ticket-update";
            
        } catch (Exception e) {
            System.err.println("Error in support ticket update form: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading ticket update form: " + e.getMessage());
            return "redirect:/support/tickets";
        }
    }
    
    @PostMapping("/tickets/{id}/update")
    public String updateTicket(@PathVariable Long id, @RequestParam String subject, 
                              @RequestParam String description, @RequestParam String response,
                              Authentication authentication, RedirectAttributes redirectAttributes) {
        System.out.println("=== SUPPORT TICKET UPDATE ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Subject: " + subject);
        System.out.println("Description: " + description);
        System.out.println("Response: " + response);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please log in to update tickets");
            return "redirect:/support/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
            
            // Check if user is support staff or admin
            if (user.getRole() != User.Role.SUPPORT && user.getRole() != User.Role.ADMIN) {
                System.out.println("User is not support staff or admin - access denied");
                redirectAttributes.addFlashAttribute("error", "Access denied. Support staff or admin privileges required.");
                return "redirect:/support/tickets";
            }
            
            Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? "Yes" : "No"));
            
            // Check if ticket exists
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/support/tickets";
            }
            
            // Update ticket fields
            ticket.setSubject(subject);
            ticket.setDescription(description);
            if (response != null && !response.trim().isEmpty()) {
                ticket.setResponse(response);
            }
            
            System.out.println("All checks passed - updating ticket");
            ticketService.updateTicket(ticket);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully");
            
        } catch (Exception e) {
            System.err.println("Error in support ticket update: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update ticket: " + e.getMessage());
        }
        
        return "redirect:/support/tickets/" + id;
    }
    
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        System.out.println("=== SUPPORT TICKET DELETE ENDPOINT CALLED ===");
        System.out.println("Ticket ID: " + id);
        System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
        
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("Authentication failed - redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please log in to delete tickets");
            return "redirect:/support/tickets";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
            
            // Check if user is support staff or admin
            if (user.getRole() != User.Role.SUPPORT && user.getRole() != User.Role.ADMIN) {
                System.out.println("User is not support staff or admin - access denied");
                redirectAttributes.addFlashAttribute("error", "Access denied. Support staff or admin privileges required.");
                return "redirect:/support/tickets";
            }
            
            Ticket ticket = ticketService.getTicketById(id);
            System.out.println("Ticket found: " + (ticket != null ? "Yes" : "No"));
            
            // Check if ticket exists
            if (ticket == null) {
                System.out.println("Ticket not found for ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/support/tickets";
            }
            
            System.out.println("All checks passed - deleting ticket");
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully");
            
        } catch (Exception e) {
            System.err.println("Error in support ticket delete: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        
        return "redirect:/support/tickets";
    }
    
    @GetMapping("/tickets/open")
    public String openTickets(Model model) {
        model.addAttribute("tickets", ticketService.getOpenTickets());
        model.addAttribute("status", "Open");
        return "support/tickets";
    }
    
    @GetMapping("/tickets/resolved")
    public String resolvedTickets(Model model) {
        model.addAttribute("tickets", ticketService.getResolvedTickets());
        model.addAttribute("status", "Resolved");
        return "support/tickets";
    }
    
    @GetMapping("/tickets/escalated")
    public String escalatedTickets(Model model) {
        model.addAttribute("tickets", ticketService.getEscalatedTickets());
        model.addAttribute("status", "Escalated");
        return "support/tickets";
    }
}
