package com.votingsystem.voting_system.strategy;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;

/**
 * Strategy interface for vote validation.
 * This defines the contract for different validation strategies.
 */
public interface VoteValidationStrategy {
    
    /**
     * Validates if a user can vote based on the specific strategy rules.
     * 
     * @param user The user attempting to vote
     * @param event The event for which the vote is being cast
     * @return ValidationResult containing success status and error message if any
     */
    ValidationResult validateVote(User user, Event event);
    
    /**
     * Gets the strategy name for identification purposes.
     * 
     * @return String representation of the strategy name
     */
    String getStrategyName();
    
    /**
     * Result class for validation operations.
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }
}
