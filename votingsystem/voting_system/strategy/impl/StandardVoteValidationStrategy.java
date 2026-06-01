package com.votingsystem.voting_system.strategy.impl;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.strategy.VoteValidationStrategy;
import org.springframework.stereotype.Component;

/**
 * Strategy for validating standard user votes.
 * Implements normal voting rules and restrictions.
 */
@Component("standardVoteValidationStrategy")
public class StandardVoteValidationStrategy implements VoteValidationStrategy {
    
    @Override
    public ValidationResult validateVote(User user, Event event) {
        // Standard validation rules for regular voters
        
        if (user == null) {
            return ValidationResult.failure("User cannot be null");
        }
        
        if (event == null) {
            return ValidationResult.failure("Event cannot be null");
        }
        
        // Check if user is a voter
        if (user.getRole() != User.Role.VOTER) {
            return ValidationResult.failure("Only voters can cast votes using this strategy");
        }
        
        // Check if voting is open
        if (!event.isVotingOpen()) {
            return ValidationResult.failure("Voting is not open for this event");
        }
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            return ValidationResult.failure("User account is disabled");
        }
        
        // Check if user account is not expired
        if (!user.isAccountNonExpired()) {
            return ValidationResult.failure("User account has expired");
        }
        
        // Check if user account is not locked
        if (!user.isAccountNonLocked()) {
            return ValidationResult.failure("User account is locked");
        }
        
        // Check if credentials are not expired
        if (!user.isCredentialsNonExpired()) {
            return ValidationResult.failure("User credentials have expired");
        }
        
        return ValidationResult.success();
    }
    
    @Override
    public String getStrategyName() {
        return "Standard Vote Validation Strategy";
    }
}
