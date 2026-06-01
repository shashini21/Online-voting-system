package com.votingsystem.voting_system.observer;

import com.votingsystem.voting_system.entity.Vote;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.User;

/**
 * Observer interface for vote-related events.
 * Implementations of this interface will be notified when vote events occur.
 */
public interface VoteObserver {
    
    /**
     * Called when a new vote is cast.
     * 
     * @param vote The vote that was cast
     * @param event The event the vote was cast for
     * @param voter The user who cast the vote
     */
    void onVoteCast(Vote vote, Event event, User voter);
    
    /**
     * Called when a vote is updated.
     * 
     * @param oldVote The previous vote data
     * @param newVote The updated vote data
     * @param event The event the vote belongs to
     * @param voter The user who updated the vote
     */
    void onVoteUpdated(Vote oldVote, Vote newVote, Event event, User voter);
    
    /**
     * Called when a vote is deleted.
     * 
     * @param vote The vote that was deleted
     * @param event The event the vote belonged to
     * @param voter The user who deleted the vote
     */
    void onVoteDeleted(Vote vote, Event event, User voter);
    
    /**
     * Get the observer name for identification and logging.
     * 
     * @return The name of this observer
     */
    String getObserverName();
}
