package com.votingsystem.voting_system.strategy.impl;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.strategy.VoteValidationStrategy;
import org.springframework.stereotype.Component;

/**
 * Strategy for validating nominee votes.
 * Nominees have special voting restrictions and rules.
 */
@Component("nomineeVoteValidationStrategy")
public class NomineeVoteValidationStrategy implements VoteValidationStrategy {
    
    @Override
    public ValidationResult validateVote(User user, Event event) {
        // Nominee-specific validation rules
        
        if (user == null) {
            return ValidationResult.failure("User cannot be null");
        }
        
        if (event == null) {
            return ValidationResult.failure("Event cannot be null");
        }
        
        // Check if user is a nominee
        if (user.getRole() != User.Role.NOMINEE) {
            return ValidationResult.failure("User must be a nominee to use this validation strategy");
        }
        
        // Check if voting is open
        if (!event.isVotingOpen()) {
            return ValidationResult.failure("Voting is not open for this event");
        }
        
        // Nominees might have different voting restrictions
        // For example, they might not be able to vote for themselves
        // This could be extended based on business requirements
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            return ValidationResult.failure("User account is disabled");
        }
        
        return ValidationResult.success();
    }
    
    @Override
    public String getStrategyName() {
        return "Nominee Vote Validation Strategy";
    }
}
