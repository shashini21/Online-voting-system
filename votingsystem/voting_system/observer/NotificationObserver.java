package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observer that handles real-time notifications when votes are cast.
 * This observer can be extended to send WebSocket updates, email notifications, etc.
 */
@Component("notificationObserver")
public class NotificationObserver implements VoteObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationObserver.class);
    
    @Override
    public void onVoteCast(Vote vote, Event event, User voter) {
        logger.info("Notification triggered: Vote cast by {} for event: {}", 
                   voter.getUsername(), event.getTitle());
        
        try {
            // Send real-time notification
            sendRealTimeNotification(vote, event, voter);
            
            // Log the vote activity
            logVoteActivity(vote, event, voter);
            
        } catch (Exception e) {
            logger.error("Error sending notification for vote cast: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteUpdated(Vote oldVote, Vote newVote, Event event, User voter) {
        logger.info("Notification triggered: Vote updated by {} for event: {}", 
                   voter.getUsername(), event.getTitle());
        
        try {
            // Send update notification
            sendUpdateNotification(oldVote, newVote, event, voter);
            
        } catch (Exception e) {
            logger.error("Error sending notification for vote update: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteDeleted(Vote vote, Event event, User voter) {
        logger.info("Notification triggered: Vote deleted by {} for event: {}", 
                   voter.getUsername(), event.getTitle());
        
        try {
            // Send deletion notification
            sendDeletionNotification(vote, event, voter);
            
        } catch (Exception e) {
            logger.error("Error sending notification for vote deletion: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getObserverName() {
        return "Notification Observer";
    }
    
    /**
     * Send real-time notification about a vote cast.
     * 
     * @param vote The vote that was cast
     * @param event The event the vote was cast for
     * @param voter The user who cast the vote
     */
    private void sendRealTimeNotification(Vote vote, Event event, User voter) {
        // This could be implemented to:
        // 1. Send WebSocket messages to connected clients
        // 2. Send push notifications to mobile apps
        // 3. Update real-time dashboards
        // 4. Send email notifications to admins
        
        logger.debug("Sending real-time notification for vote cast in event: {}", event.getTitle());
        
        // Example: WebSocket notification
        // webSocketService.sendToAll("/topic/votes", new VoteNotification(vote, event, voter));
    }
    
    /**
     * Log vote activity for audit purposes.
     * 
     * @param vote The vote that was cast
     * @param event The event the vote was cast for
     * @param voter The user who cast the vote
     */
    private void logVoteActivity(Vote vote, Event event, User voter) {
        logger.info("Vote Activity Log - User: {}, Event: {}, Nominee: {}, Timestamp: {}", 
                   voter.getUsername(), 
                   event.getTitle(), 
                   vote.getNominee() != null ? vote.getNominee().getNomineeName() : "Unknown",
                   vote.getTimestamp());
    }
    
    /**
     * Send notification about a vote update.
     * 
     * @param oldVote The previous vote data
     * @param newVote The updated vote data
     * @param event The event the vote belongs to
     * @param voter The user who updated the vote
     */
    private void sendUpdateNotification(Vote oldVote, Vote newVote, Event event, User voter) {
        logger.debug("Sending update notification for vote modification in event: {}", event.getTitle());
        
        // Implementation for vote update notifications
    }
    
    /**
     * Send notification about a vote deletion.
     * 
     * @param vote The vote that was deleted
     * @param event The event the vote belonged to
     * @param voter The user who deleted the vote
     */
    private void sendDeletionNotification(Vote vote, Event event, User voter) {
        logger.debug("Sending deletion notification for vote removal in event: {}", event.getTitle());
        
        // Implementation for vote deletion notifications
    }
}
