package com.votingsystem.voting_system.strategy;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Nominee;
import com.votingsystem.voting_system.repository.VoteRepository;
import com.votingsystem.voting_system.repository.NomineeRepository;
import com.votingsystem.voting_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Enhanced VoteService that demonstrates the Strategy Pattern usage.
 * This service uses different validation strategies based on user roles.
 */
@Service
public class EnhancedVoteService {
    
    @Autowired
    private VoteValidationContext voteValidationContext;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NomineeRepository nomineeRepository;
    
    /**
     * Casts a vote using the appropriate validation strategy based on user role.
     * 
     * @param userId The ID of the user casting the vote
     * @param nomineeId The ID of the nominee being voted for
     * @param eventId The ID of the event
     * @return The created Vote object
     * @throws RuntimeException if validation fails
     */
    public Vote castVote(Long userId, Long nomineeId, Long eventId) {
        // Fetch entities
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Event event = userService.getUserById(eventId)
                .map(u -> new Event()) // This would normally come from EventService
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));
        
        // Use strategy pattern for validation
        VoteValidationStrategy.ValidationResult validationResult = 
            voteValidationContext.validateVote(user, event);
        
        if (!validationResult.isValid()) {
            throw new RuntimeException("Vote validation failed: " + validationResult.getErrorMessage());
        }
        
        // Check if user has already voted for this event (additional business logic)
        if (hasUserVotedForEvent(userId, eventId)) {
            throw new RuntimeException("User has already voted for this event");
        }
        
        // Create and save vote
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setEvent(event);
        vote.setNominee(nominee);
        vote.setTimestamp(LocalDateTime.now());
        vote.setEncryptedVote(encryptVote(nomineeId.toString()));
        
        return voteRepository.save(vote);
    }
    
    /**
     * Casts a vote using a specific validation strategy.
     * 
     * @param userId The ID of the user casting the vote
     * @param nomineeId The ID of the nominee being voted for
     * @param eventId The ID of the event
     * @param strategyName The name of the validation strategy to use
     * @return The created Vote object
     * @throws RuntimeException if validation fails
     */
    public Vote castVoteWithStrategy(Long userId, Long nomineeId, Long eventId, String strategyName) {
        // Fetch entities
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Event event = userService.getUserById(eventId)
                .map(u -> new Event()) // This would normally come from EventService
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        // Use specific strategy for validation
        VoteValidationStrategy.ValidationResult validationResult = 
            voteValidationContext.validateVote(user, event, strategyName);
        
        if (!validationResult.isValid()) {
            throw new RuntimeException("Vote validation failed: " + validationResult.getErrorMessage());
        }
        
        // Additional business logic...
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));
        
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setEvent(event);
        vote.setNominee(nominee);
        vote.setTimestamp(LocalDateTime.now());
        vote.setEncryptedVote(encryptVote(nomineeId.toString()));
        
        return voteRepository.save(vote);
    }
    
    /**
     * Gets all available validation strategies.
     * 
     * @return Map of strategy names to strategy implementations
     */
    public java.util.Map<String, VoteValidationStrategy> getAvailableStrategies() {
        return voteValidationContext.getAvailableStrategies();
    }
    
    /**
     * Checks if a user has already voted for a specific event.
     * 
     * @param userId The user ID
     * @param eventId The event ID
     * @return true if user has voted, false otherwise
     */
    private boolean hasUserVotedForEvent(Long userId, Long eventId) {
        return voteRepository.countByUserIdAndEventId(userId, eventId) > 0;
    }
    
    /**
     * Simple encryption method for vote data.
     * 
     * @param voteData The vote data to encrypt
     * @return Encrypted vote data
     */
    private String encryptVote(String voteData) {
        // Simple encryption - in production, use proper encryption
        return "ENCRYPTED_" + voteData + "_" + System.currentTimeMillis();
    }
}
