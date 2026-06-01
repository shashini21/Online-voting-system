package com.votingsystem.voting_system.observer;

import java.util.List;

/**
 * Subject interface for the Observer pattern.
 * Classes implementing this interface can register, remove, and notify observers.
 */
public interface VoteSubject {
    
    /**
     * Register an observer to receive vote event notifications.
     * 
     * @param observer The observer to register
     */
    void registerObserver(VoteObserver observer);
    
    /**
     * Remove an observer from receiving notifications.
     * 
     * @param observer The observer to remove
     */
    void removeObserver(VoteObserver observer);
    
    /**
     * Notify all registered observers about a vote event.
     * 
     * @param eventType The type of vote event
     * @param vote The vote involved in the event
     * @param event The event the vote belongs to
     * @param voter The user involved in the vote event
     */
    void notifyObservers(VoteEventType eventType, com.votingsystem.voting_system.entity.Vote vote, 
                       com.votingsystem.voting_system.entity.Event event, 
                       com.votingsystem.voting_system.entity.User voter);
    
    /**
     * Get all registered observers.
     * 
     * @return List of registered observers
     */
    List<VoteObserver> getObservers();
    
    /**
     * Get the number of registered observers.
     * 
     * @return Number of observers
     */
    int getObserverCount();
}
