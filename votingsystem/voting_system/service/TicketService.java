package com.votingsystem.voting_system.service;

import com.votingsystem.voting_system.entity.Ticket;
import com.votingsystem.voting_system.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllOrderByCreatedAtDesc();
    }
    
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }
    
    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus(Ticket.Status.OPEN);
        return ticketRepository.save(ticket);
    }
    
    public Ticket updateTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
    
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
    
    public List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Ticket> getTicketsByStatus(Ticket.Status status) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Ticket resolveTicket(Long ticketId, String response) {
        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }
        
        ticket.setStatus(Ticket.Status.RESOLVED);
        ticket.setResponse(response);
        ticket.setResolvedAt(LocalDateTime.now());
        
        return ticketRepository.save(ticket);
    }
    
    public Ticket escalateTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }
        
        ticket.setStatus(Ticket.Status.ESCALATED);
        return ticketRepository.save(ticket);
    }
    
    public long countTicketsByStatus(Ticket.Status status) {
        return ticketRepository.countByStatus(status);
    }
    
    public long countTicketsByUser(Long userId) {
        return ticketRepository.countByUserId(userId);
    }
    
    public List<Ticket> getOpenTickets() {
        return getTicketsByStatus(Ticket.Status.OPEN);
    }
    
    public List<Ticket> getResolvedTickets() {
        return getTicketsByStatus(Ticket.Status.RESOLVED);
    }
    
    public List<Ticket> getEscalatedTickets() {
        return getTicketsByStatus(Ticket.Status.ESCALATED);
    }
}
