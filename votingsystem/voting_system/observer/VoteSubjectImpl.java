package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Concrete implementation of VoteSubject that manages observers and notifies them of vote events.
 * This class is thread-safe and uses CopyOnWriteArrayList for concurrent access.
 */
@Component
public class VoteSubjectImpl implements VoteSubject {
    
    private static final Logger logger = LoggerFactory.getLogger(VoteSubjectImpl.class);
    
    private final List<VoteObserver> observers = new CopyOnWriteArrayList<>();
    
    @Override
    public void registerObserver(VoteObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            logger.info("Registered observer: {}", observer.getObserverName());
        }
    }
    
    @Override
    public void removeObserver(VoteObserver observer) {
        if (observer != null && observers.remove(observer)) {
            logger.info("Removed observer: {}", observer.getObserverName());
        }
    }
    
    @Override
    public void notifyObservers(VoteEventType eventType, Vote vote, Event event, User voter) {
        if (observers.isEmpty()) {
            logger.debug("No observers registered for vote event: {}", eventType);
            return;
        }
        
        logger.info("Notifying {} observers about vote event: {} for event: {}", 
                   observers.size(), eventType, event != null ? event.getTitle() : "Unknown");
        
        for (VoteObserver observer : observers) {
            try {
                switch (eventType) {
                    case VOTE_CAST:
                        observer.onVoteCast(vote, event, voter);
                        break;
                    case VOTE_UPDATED:
                        // For updated votes, we need to handle this differently
                        // This would require storing the old vote data
                        observer.onVoteUpdated(null, vote, event, voter);
                        break;
                    case VOTE_DELETED:
                        observer.onVoteDeleted(vote, event, voter);
                        break;
                }
                logger.debug("Successfully notified observer: {}", observer.getObserverName());
            } catch (Exception e) {
                logger.error("Error notifying observer {}: {}", observer.getObserverName(), e.getMessage(), e);
            }
        }
    }
    
    @Override
    public List<VoteObserver> getObservers() {
        return new ArrayList<>(observers);
    }
    
    @Override
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Convenience method to notify observers about a vote cast event.
     * 
     * @param vote The vote that was cast
     * @param event The event the vote was cast for
     * @param voter The user who cast the vote
     */
    public void notifyVoteCast(Vote vote, Event event, User voter) {
        notifyObservers(VoteEventType.VOTE_CAST, vote, event, voter);
    }
    
    /**
     * Convenience method to notify observers about a vote update event.
     * 
     * @param oldVote The previous vote data
     * @param newVote The updated vote data
     * @param event The event the vote belongs to
     * @param voter The user who updated the vote
     */
    public void notifyVoteUpdated(Vote oldVote, Vote newVote, Event event, User voter) {
        notifyObservers(VoteEventType.VOTE_UPDATED, newVote, event, voter);
    }
    
    /**
     * Convenience method to notify observers about a vote deletion event.
     * 
     * @param vote The vote that was deleted
     * @param event The event the vote belonged to
     * @param voter The user who deleted the vote
     */
    public void notifyVoteDeleted(Vote vote, Event event, User voter) {
        notifyObservers(VoteEventType.VOTE_DELETED, vote, event, voter);
    }
}
