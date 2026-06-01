package com.votingsystem.voting_system.service;

import com.votingsystem.voting_system.entity.*;
import com.votingsystem.voting_system.repository.VoteRepository;
import com.votingsystem.voting_system.repository.NomineeRepository;
import com.votingsystem.voting_system.service.EventService;
import com.votingsystem.voting_system.observer.VoteSubjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NomineeRepository nomineeRepository;
    
    @Autowired
    private VoteSubjectImpl voteSubject;
    
    public Vote castVote(Long userId, Long nomineeId, Long eventId) {
        // Check if voting is open for this event
        if (!eventService.isEventVotingOpen(eventId)) {
            throw new RuntimeException("Voting is not open for this event");
        }
        
        // Check if user has already voted for this event
        if (hasUserVotedForEvent(userId, eventId)) {
            throw new RuntimeException("User has already voted for this event");
        }
        
        // Fetch entities
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        // Create vote
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setEvent(event);
        vote.setNominee(nominee);
        vote.setTimestamp(LocalDateTime.now());
        vote.setEncryptedVote(encryptVote(nomineeId.toString()));
        
        // Save the vote
        Vote savedVote = voteRepository.save(vote);
        
        // Notify observers about the vote cast
        try {
            voteSubject.notifyVoteCast(savedVote, event, user);
            logger.info("Vote cast successfully and observers notified for event: {} by user: {}", 
                       event.getTitle(), user.getUsername());
        } catch (Exception e) {
            logger.error("Error notifying observers about vote cast: {}", e.getMessage(), e);
            // Don't fail the vote if observer notification fails
        }
        
        return savedVote;
    }
    
    public boolean hasUserVotedForEvent(Long userId, Long eventId) {
        return voteRepository.countByUserIdAndEventId(userId, eventId) > 0;
    }
    
    public List<Vote> getUserVotes(Long userId) {
        return voteRepository.findByUserId(userId);
    }
    
    public List<Vote> getEventVotes(Long eventId) {
        return voteRepository.findByEventId(eventId);
    }
    
    public long getVoteCountForNominee(Long nomineeId) {
        return voteRepository.countByNomineeId(nomineeId);
    }
    
    public long getVoteCountForEvent(Long eventId) {
        return voteRepository.countByEventId(eventId);
    }
    
    public List<Object[]> getVoteCountByCategoryForEvent(Long eventId) {
        return voteRepository.getVoteCountByCategoryForEvent(eventId);
    }
    
    public boolean hasUserVotedForNominee(Long userId, Long nomineeId) {
        return voteRepository.findByUserIdAndNomineeId(userId, nomineeId).isPresent();
    }
    
    private String encryptVote(String voteData) {
        // Simple encryption - in production, use proper encryption
        return "ENCRYPTED_" + voteData + "_" + System.currentTimeMillis();
    }
    
    public String decryptVote(String encryptedVote) {
        // Simple decryption - in production, use proper decryption
        if (encryptedVote.startsWith("ENCRYPTED_")) {
            String[] parts = encryptedVote.split("_");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return encryptedVote;
    }
    
    /**
     * Update an existing vote and notify observers.
     * 
     * @param voteId The ID of the vote to update
     * @param newNomineeId The new nominee ID
     * @return The updated vote
     */
    public Vote updateVote(Long voteId, Long newNomineeId) {
        Vote existingVote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found"));
        
        // Store old vote data for observer notification
        Vote oldVote = new Vote();
        oldVote.setId(existingVote.getId());
        oldVote.setUser(existingVote.getUser());
        oldVote.setEvent(existingVote.getEvent());
        oldVote.setNominee(existingVote.getNominee());
        oldVote.setTimestamp(existingVote.getTimestamp());
        oldVote.setEncryptedVote(existingVote.getEncryptedVote());
        
        // Update the vote
        Nominee newNominee = nomineeRepository.findById(newNomineeId)
                .orElseThrow(() -> new RuntimeException("New nominee not found"));
        
        existingVote.setNominee(newNominee);
        existingVote.setTimestamp(LocalDateTime.now());
        existingVote.setEncryptedVote(encryptVote(newNomineeId.toString()));
        
        // Save the updated vote
        Vote updatedVote = voteRepository.save(existingVote);
        
        // Notify observers about the vote update
        try {
            voteSubject.notifyVoteUpdated(oldVote, updatedVote, existingVote.getEvent(), existingVote.getUser());
            logger.info("Vote updated successfully and observers notified for vote ID: {}", voteId);
        } catch (Exception e) {
            logger.error("Error notifying observers about vote update: {}", e.getMessage(), e);
        }
        
        return updatedVote;
    }
    
    /**
     * Delete a vote and notify observers.
     * 
     * @param voteId The ID of the vote to delete
     */
    public void deleteVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found"));
        
        // Store vote data for observer notification
        Vote voteToDelete = new Vote();
        voteToDelete.setId(vote.getId());
        voteToDelete.setUser(vote.getUser());
        voteToDelete.setEvent(vote.getEvent());
        voteToDelete.setNominee(vote.getNominee());
        voteToDelete.setTimestamp(vote.getTimestamp());
        voteToDelete.setEncryptedVote(vote.getEncryptedVote());
        
        // Delete the vote
        voteRepository.deleteById(voteId);
        
        // Notify observers about the vote deletion
        try {
            voteSubject.notifyVoteDeleted(voteToDelete, vote.getEvent(), vote.getUser());
            logger.info("Vote deleted successfully and observers notified for vote ID: {}", voteId);
        } catch (Exception e) {
            logger.error("Error notifying observers about vote deletion: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get the vote subject for external access to observer functionality.
     * 
     * @return The vote subject instance
     */
    public VoteSubjectImpl getVoteSubject() {
        return voteSubject;
    }
}
