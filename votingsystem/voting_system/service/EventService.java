package com.votingsystem.voting_system.service;

import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    public List<Event> getAllEvents() {
        return eventRepository.findAllOrderByDate();
    }
    
    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }
    
    public Event createEvent(Event event) {
        // Validate required fields
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Event title is required");
        }
        if (event.getTitle().trim().length() < 3) {
            throw new RuntimeException("Event title must be at least 3 characters long");
        }
        if (event.getTitle().trim().length() > 100) {
            throw new RuntimeException("Event title cannot exceed 100 characters");
        }
        
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new RuntimeException("Venue is required");
        }
        if (event.getVenue().trim().length() < 3) {
            throw new RuntimeException("Venue must be at least 3 characters long");
        }
        if (event.getVenue().trim().length() > 120) {
            throw new RuntimeException("Venue cannot exceed 120 characters");
        }
        
        if (event.getDate() == null) {
            throw new RuntimeException("Event date is required");
        }
        
        if (event.getNominationDeadline() == null) {
            throw new RuntimeException("Nomination deadline is required");
        }
        
        if (event.getVotingDeadline() == null) {
            throw new RuntimeException("Voting deadline is required");
        }
        
        // Validate dates
        if (event.getNominationDeadline().isAfter(event.getVotingDeadline())) {
            throw new RuntimeException("Nomination deadline cannot be after voting deadline");
        }
        
        if (event.getVotingDeadline().isAfter(event.getDate())) {
            throw new RuntimeException("Voting deadline cannot be after event date");
        }
        
        if (event.getNominationDeadline().isAfter(event.getDate())) {
            throw new RuntimeException("Nomination deadline cannot be after event date");
        }
        
        if (event.getVotingDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Voting deadline cannot be in the past");
        }
        
        if (event.getNominationDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Nomination deadline cannot be in the past");
        }
        
        // Validate description length if provided
        if (event.getDescription() != null && event.getDescription().length() > 1000) {
            throw new RuntimeException("Description cannot exceed 1000 characters");
        }
        
        // Validate banner URL format if provided
        if (event.getBannerUrl() != null && !event.getBannerUrl().trim().isEmpty()) {
            String url = event.getBannerUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("/")) {
                throw new RuntimeException("Banner URL must be a valid HTTP/HTTPS URL or start with '/'");
            }
        }
        
        return eventRepository.save(event);
    }
    
    public Event updateEvent(Event event) {
        // Validate required fields
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Event title is required");
        }
        if (event.getTitle().trim().length() < 3) {
            throw new RuntimeException("Event title must be at least 3 characters long");
        }
        if (event.getTitle().trim().length() > 100) {
            throw new RuntimeException("Event title cannot exceed 100 characters");
        }
        
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new RuntimeException("Venue is required");
        }
        if (event.getVenue().trim().length() < 3) {
            throw new RuntimeException("Venue must be at least 3 characters long");
        }
        if (event.getVenue().trim().length() > 120) {
            throw new RuntimeException("Venue cannot exceed 120 characters");
        }
        
        if (event.getDate() == null) {
            throw new RuntimeException("Event date is required");
        }
        
        if (event.getNominationDeadline() == null) {
            throw new RuntimeException("Nomination deadline is required");
        }
        
        if (event.getVotingDeadline() == null) {
            throw new RuntimeException("Voting deadline is required");
        }
        
        // Validate dates
        if (event.getNominationDeadline().isAfter(event.getVotingDeadline())) {
            throw new RuntimeException("Nomination deadline cannot be after voting deadline");
        }
        
        if (event.getVotingDeadline().isAfter(event.getDate())) {
            throw new RuntimeException("Voting deadline cannot be after event date");
        }
        
        if (event.getNominationDeadline().isAfter(event.getDate())) {
            throw new RuntimeException("Nomination deadline cannot be after event date");
        }
        
        // Validate description length if provided
        if (event.getDescription() != null && event.getDescription().length() > 1000) {
            throw new RuntimeException("Description cannot exceed 1000 characters");
        }
        
        // Validate banner URL format if provided
        if (event.getBannerUrl() != null && !event.getBannerUrl().trim().isEmpty()) {
            String url = event.getBannerUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("/")) {
                throw new RuntimeException("Banner URL must be a valid HTTP/HTTPS URL or start with '/'");
            }
        }
        
        return eventRepository.save(event);
    }
    
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
    
    public List<Event> getActiveEvents() {
        return eventRepository.findActiveEvents(LocalDateTime.now());
    }
    
    public List<Event> getCompletedEvents() {
        return eventRepository.findCompletedEvents(LocalDateTime.now());
    }
    
    public List<Event> getEventsWithOpenNomination() {
        return eventRepository.findEventsWithOpenNomination(LocalDateTime.now());
    }
    
    public List<Event> getEventsWithOpenVoting() {
        return eventRepository.findEventsWithOpenVoting(LocalDateTime.now());
    }
    
    public long countActiveEvents() {
        return eventRepository.countActiveEvents(LocalDateTime.now());
    }
    
    public boolean isEventVotingOpen(Long eventId) {
        Event event = getEventById(eventId);
        return event != null && event.isVotingOpen();
    }
    
    public boolean isEventNominationOpen(Long eventId) {
        Event event = getEventById(eventId);
        return event != null && event.isNominationOpen();
    }
}
