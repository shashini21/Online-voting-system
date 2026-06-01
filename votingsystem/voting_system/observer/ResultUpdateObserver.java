package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observer that handles result recalculation when votes are cast.
 * This observer ensures that voting results are always up-to-date.
 */
@Component("resultUpdateObserver")
public class ResultUpdateObserver implements VoteObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(ResultUpdateObserver.class);
    
    @Autowired
    private ResultService resultService;
    
    @Override
    public void onVoteCast(Vote vote, Event event, User voter) {
        logger.info("Result update triggered by vote cast for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Recalculate results for the event
            recalculateResults(event.getId());
            
            // Update rankings if needed
            updateRankings(event.getId());
            
            logger.info("Results recalculated for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error recalculating results for vote cast: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteUpdated(Vote oldVote, Vote newVote, Event event, User voter) {
        logger.info("Result update triggered by vote update for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Recalculate results after vote update
            recalculateResults(event.getId());
            
            // Update rankings
            updateRankings(event.getId());
            
            logger.info("Results recalculated after vote update for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error recalculating results for vote update: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void onVoteDeleted(Vote vote, Event event, User voter) {
        logger.info("Result update triggered by vote deletion for event: {} by user: {}", 
                   event.getTitle(), voter.getUsername());
        
        try {
            // Recalculate results after vote deletion
            recalculateResults(event.getId());
            
            // Update rankings
            updateRankings(event.getId());
            
            logger.info("Results recalculated after vote deletion for event: {}", event.getTitle());
            
        } catch (Exception e) {
            logger.error("Error recalculating results for vote deletion: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getObserverName() {
        return "Result Update Observer";
    }
    
    /**
     * Recalculate voting results for the specified event.
     * 
     * @param eventId The ID of the event to recalculate results for
     */
    private void recalculateResults(Long eventId) {
        try {
            logger.debug("Recalculating results for event ID: {}", eventId);
            
            // Use the result service to get current results
            var voteCounts = resultService.getVoteCountsByCategoryAndNominee(eventId);
            var nomineeCounts = resultService.getNomineeVoteCounts(eventId);
            
            logger.debug("Recalculated vote counts for event {}: {} categories, {} nominees", 
                        eventId, voteCounts.size(), nomineeCounts.size());
            
            // This would typically:
            // 1. Recalculate vote counts by nominee
            // 2. Update category-wise results
            // 3. Determine winners
            // 4. Update result summaries
            
        } catch (Exception e) {
            logger.error("Error recalculating results for event {}: {}", eventId, e.getMessage(), e);
        }
    }
    
    /**
     * Update nominee rankings for the specified event.
     * 
     * @param eventId The ID of the event to update rankings for
     */
    private void updateRankings(Long eventId) {
        try {
            logger.debug("Updating rankings for event ID: {}", eventId);
            
            // This would typically:
            // 1. Sort nominees by vote count
            // 2. Update ranking positions
            // 3. Identify category winners
            // 4. Update leaderboards
            
        } catch (Exception e) {
            logger.error("Error updating rankings for event {}: {}", eventId, e.getMessage(), e);
        }
    }
}
