package com.votingsystem.voting_system.strategy;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Context class for the Strategy Pattern.
 * This class manages the selection and execution of different vote validation strategies.
 */
@Component
public class VoteValidationContext {
    
    private final Map<String, VoteValidationStrategy> strategies;
    
    /**
     * Constructor that injects all available strategies.
     * Spring will automatically inject all beans implementing VoteValidationStrategy.
     */
    @Autowired
    public VoteValidationContext(Map<String, VoteValidationStrategy> strategies) {
        this.strategies = strategies;
    }
    
    /**
     * Validates a vote using the appropriate strategy based on user role.
     * 
     * @param user The user attempting to vote
     * @param event The event for which the vote is being cast
     * @return ValidationResult containing success status and error message if any
     */
    public VoteValidationStrategy.ValidationResult validateVote(User user, Event event) {
        VoteValidationStrategy strategy = getStrategyForUser(user);
        
        if (strategy == null) {
            return VoteValidationStrategy.ValidationResult.failure(
                "No validation strategy found for user role: " + user.getRole()
            );
        }
        
        return strategy.validateVote(user, event);
    }
    
    /**
     * Validates a vote using a specific strategy by name.
     * 
     * @param user The user attempting to vote
     * @param event The event for which the vote is being cast
     * @param strategyName The name of the strategy to use
     * @return ValidationResult containing success status and error message if any
     */
    public VoteValidationStrategy.ValidationResult validateVote(User user, Event event, String strategyName) {
        VoteValidationStrategy strategy = strategies.get(strategyName);
        
        if (strategy == null) {
            return VoteValidationStrategy.ValidationResult.failure(
                "Strategy not found: " + strategyName
            );
        }
        
        return strategy.validateVote(user, event);
    }
    
    /**
     * Gets the appropriate strategy for a given user based on their role.
     * 
     * @param user The user for whom to select a strategy
     * @return The appropriate VoteValidationStrategy or null if none found
     */
    private VoteValidationStrategy getStrategyForUser(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return strategies.get("adminVoteValidationStrategy");
            case VOTER:
                return strategies.get("standardVoteValidationStrategy");
            case NOMINEE:
                return strategies.get("nomineeVoteValidationStrategy");
            case SUPPORT:
                // Support users might use standard validation or have their own
                return strategies.get("standardVoteValidationStrategy");
            default:
                return null;
        }
    }
    
    /**
     * Gets all available strategies.
     * 
     * @return Map of strategy names to strategy implementations
     */
    public Map<String, VoteValidationStrategy> getAvailableStrategies() {
        return strategies;
    }
    
    /**
     * Gets a specific strategy by name.
     * 
     * @param strategyName The name of the strategy
     * @return The VoteValidationStrategy or null if not found
     */
    public VoteValidationStrategy getStrategy(String strategyName) {
        return strategies.get(strategyName);
    }
}
