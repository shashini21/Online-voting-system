package com.votingsystem.voting_system.strategy;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Demo controller to showcase the Strategy Pattern implementation.
 * This controller demonstrates how different validation strategies are used.
 */
@Controller
@RequestMapping("/strategy-demo")
public class StrategyPatternDemoController {
    
    @Autowired
    private VoteValidationContext voteValidationContext;
    
    // Note: EnhancedVoteService is available for future use
    // @Autowired
    // private EnhancedVoteService enhancedVoteService;
    
    /**
     * Shows the strategy pattern demo page with available strategies.
     */
    @GetMapping("/")
    public String strategyDemo(Model model) {
        model.addAttribute("strategies", voteValidationContext.getAvailableStrategies());
        return "strategy-demo/index";
    }
    
    /**
     * Demonstrates strategy selection based on user role.
     */
    @GetMapping("/demo-role-based")
    public String demoRoleBased(Model model) {
        // Create sample users for demonstration
        User adminUser = createSampleUser(User.Role.ADMIN, "admin_user");
        User voterUser = createSampleUser(User.Role.VOTER, "voter_user");
        User nomineeUser = createSampleUser(User.Role.NOMINEE, "nominee_user");
        
        Event sampleEvent = createSampleEvent();
        
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("voterUser", voterUser);
        model.addAttribute("nomineeUser", nomineeUser);
        model.addAttribute("sampleEvent", sampleEvent);
        
        return "strategy-demo/role-based";
    }
    
    /**
     * Demonstrates manual strategy selection.
     */
    @GetMapping("/demo-manual-selection")
    public String demoManualSelection(Model model) {
        User testUser = createSampleUser(User.Role.VOTER, "test_user");
        Event sampleEvent = createSampleEvent();
        
        model.addAttribute("testUser", testUser);
        model.addAttribute("sampleEvent", sampleEvent);
        model.addAttribute("availableStrategies", voteValidationContext.getAvailableStrategies());
        
        return "strategy-demo/manual-selection";
    }
    
    /**
     * API endpoint to test validation strategies.
     */
    @PostMapping("/api/validate")
    @ResponseBody
    public StrategyValidationResponse validateVote(
            @RequestParam String userRole,
            @RequestParam(required = false) String strategyName) {
        
        User user = createSampleUser(User.Role.valueOf(userRole.toUpperCase()), "test_user");
        Event event = createSampleEvent();
        
        VoteValidationStrategy.ValidationResult result;
        
        if (strategyName != null && !strategyName.isEmpty()) {
            result = voteValidationContext.validateVote(user, event, strategyName);
        } else {
            result = voteValidationContext.validateVote(user, event);
        }
        
        return new StrategyValidationResponse(
            result.isValid(),
            result.getErrorMessage(),
            strategyName != null ? strategyName : "Auto-selected"
        );
    }
    
    /**
     * API endpoint to get all available strategies.
     */
    @GetMapping("/api/strategies")
    @ResponseBody
    public java.util.Map<String, String> getAvailableStrategies() {
        return voteValidationContext.getAvailableStrategies()
            .entrySet()
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                entry -> entry.getValue().getStrategyName()
            ));
    }
    
    /**
     * Creates a sample user for demonstration purposes.
     */
    private User createSampleUser(User.Role role, String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }
    
    /**
     * Creates a sample event for demonstration purposes.
     */
    private Event createSampleEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Sample Voting Event");
        event.setNominationDeadline(java.time.LocalDateTime.now().minusDays(1));
        event.setVotingDeadline(java.time.LocalDateTime.now().plusDays(1));
        event.setDate(java.time.LocalDateTime.now().plusDays(2));
        return event;
    }
    
    /**
     * Response class for strategy validation API.
     */
    public static class StrategyValidationResponse {
        private final boolean valid;
        private final String errorMessage;
        private final String strategyUsed;
        
        public StrategyValidationResponse(boolean valid, String errorMessage, String strategyUsed) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.strategyUsed = strategyUsed;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public String getStrategyUsed() {
            return strategyUsed;
        }
    }
}
