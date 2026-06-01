package com.votingsystem.voting_system.strategy.impl;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.strategy.VoteValidationStrategy;
import org.springframework.stereotype.Component;

/**
 * Strategy for validating admin votes.
 * Admins have special voting privileges and different validation rules.
 */
@Component("adminVoteValidationStrategy")
public class AdminVoteValidationStrategy implements VoteValidationStrategy {
    
    @Override
    public ValidationResult validateVote(User user, Event event) {
        // Admins can vote even if they are not registered as voters
        if (user.getRole() != User.Role.ADMIN) {
            return ValidationResult.failure("User must be an admin to use this validation strategy");
        }
        
        // Admin-specific validation rules
        if (event == null) {
            return ValidationResult.failure("Event cannot be null");
        }
        
        if (!event.isVotingOpen()) {
            return ValidationResult.failure("Voting is not open for this event");
        }
        
        // Admins can vote multiple times if needed (for testing purposes)
        // This could be configured based on system settings
        
        return ValidationResult.success();
    }
    
    @Override
    public String getStrategyName() {
        return "Admin Vote Validation Strategy";
    }
}
