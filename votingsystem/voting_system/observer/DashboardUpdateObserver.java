package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.service.ResultService;
import com.votingsystem.voting_system.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observer that updates dashboard statistics when votes are cast.
 * This observer automatically refreshes vote counts and statistics.
 */
@Component("dashboardUpdateObserver")
public class DashboardUpdateObserver implements VoteObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardUpdateObserver.class);
    
    @Autowired
    private ResultService resultService;
    
    @Autowired
    private EventService eventService;
    
    @Override
    public void onVoteCast(Vote vote, Event event, User voter) {
        logger.info("Dashboard update triggered by vote cast for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Update vote counts for the event
            updateVoteCounts(event.getId());
            
            // Update event statistics
            updateEventStatistics(event.getId());
            
            // Log dashboard update
            logger.info("Dashboard statistics updated for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error updating dashboard for vote cast: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteUpdated(Vote oldVote, Vote newVote, Event event, User voter) {
        logger.info("Dashboard update triggered by vote update for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Recalculate vote counts
            updateVoteCounts(event.getId());
            
            // Update event statistics
            updateEventStatistics(event.getId());
            
            logger.info("Dashboard statistics updated after vote modification for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error updating dashboard for vote update: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteDeleted(Vote vote, Event event, User voter) {
        logger.info("Dashboard update triggered by vote deletion for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Update vote counts after deletion
            updateVoteCounts(event.getId());
            
            // Update event statistics
            updateEventStatistics(event.getId());
            
            logger.info("Dashboard statistics updated after vote deletion for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error updating dashboard for vote deletion: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getObserverName() {
        return "Dashboard Update Observer";
    }
    
    /**
     * Update vote counts for the specified event.
     * 
     * @param eventId The ID of the event to update
     */
    private void updateVoteCounts(Long eventId) {
        try {
            // This would typically trigger cache invalidation or real-time updates
            // For now, we'll just log the action
            logger.debug("Updating vote counts for event ID: {}", eventId);
            
            // Use the result service to get current vote counts
            var voteCounts = resultService.getVoteCountsByCategoryAndNominee(eventId);
            logger.debug("Current vote counts for event {}: {}", eventId, voteCounts.size());
            
            // In a real implementation, this might:
            // 1. Invalidate cached vote counts
            // 2. Send WebSocket updates to connected clients
            // 3. Update real-time statistics
            // 4. Trigger dashboard refresh notifications
            
        } catch (Exception e) {
            logger.error("Error updating vote counts for event {}: {}", eventId, e.getMessage(), e);
        }
    }
    
    /**
     * Update event statistics for the specified event.
     * 
     * @param eventId The ID of the event to update
     */
    private void updateEventStatistics(Long eventId) {
        try {
            logger.debug("Updating event statistics for event ID: {}", eventId);
            
            // Use the event service to get event details
            var event = eventService.getEventById(eventId);
            if (event != null) {
                logger.debug("Updating statistics for event: {}", event.getTitle());
            }
            
            // This could trigger:
            // 1. Recalculation of participation rates
            // 2. Update of nominee rankings
            // 3. Refresh of result summaries
            // 4. Notification to admin dashboards
            
        } catch (Exception e) {
            logger.error("Error updating event statistics for event {}: {}", eventId, e.getMessage(), e);
        }
    }
}
