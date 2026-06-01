package com.votingsystem.voting_system.observer;

/**
 * Enumeration of vote event types for the Observer pattern.
 */
public enum VoteEventType {
    VOTE_CAST("Vote Cast"),
    VOTE_UPDATED("Vote Updated"),
    VOTE_DELETED("Vote Deleted");
    
    private final String description;
    
    VoteEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
