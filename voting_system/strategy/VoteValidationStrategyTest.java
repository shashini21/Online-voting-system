package com.votingsystem.voting_system.strategy;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.strategy.impl.AdminVoteValidationStrategy;
import com.votingsystem.voting_system.strategy.impl.StandardVoteValidationStrategy;
import com.votingsystem.voting_system.strategy.impl.NomineeVoteValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Strategy Pattern implementation.
 * Tests different vote validation strategies.
 */
@ExtendWith(MockitoExtension.class)
class VoteValidationStrategyTest {
    
    private AdminVoteValidationStrategy adminStrategy;
    private StandardVoteValidationStrategy standardStrategy;
    private NomineeVoteValidationStrategy nomineeStrategy;
    
    private User adminUser;
    private User voterUser;
    private User nomineeUser;
    private Event testEvent;
    
    @BeforeEach
    void setUp() {
        adminStrategy = new AdminVoteValidationStrategy();
        standardStrategy = new StandardVoteValidationStrategy();
        nomineeStrategy = new NomineeVoteValidationStrategy();
        
        // Create test users
        adminUser = new User();
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setUsername("admin");
        adminUser.setEnabled(true);
        adminUser.setAccountNonExpired(true);
        adminUser.setAccountNonLocked(true);
        adminUser.setCredentialsNonExpired(true);
        
        voterUser = new User();
        voterUser.setRole(User.Role.VOTER);
        voterUser.setUsername("voter");
        voterUser.setEnabled(true);
        voterUser.setAccountNonExpired(true);
        voterUser.setAccountNonLocked(true);
        voterUser.setCredentialsNonExpired(true);
        
        nomineeUser = new User();
        nomineeUser.setRole(User.Role.NOMINEE);
        nomineeUser.setUsername("nominee");
        nomineeUser.setEnabled(true);
        
        // Create test event with voting open
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setNominationDeadline(LocalDateTime.now().minusDays(1));
        testEvent.setVotingDeadline(LocalDateTime.now().plusDays(1));
        testEvent.setDate(LocalDateTime.now().plusDays(2));
    }
    
    @Test
    void testAdminVoteValidationStrategy_Success() {
        VoteValidationStrategy.ValidationResult result = adminStrategy.validateVote(adminUser, testEvent);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testAdminVoteValidationStrategy_WrongRole() {
        VoteValidationStrategy.ValidationResult result = adminStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User must be an admin to use this validation strategy", result.getErrorMessage());
    }
    
    @Test
    void testAdminVoteValidationStrategy_NullEvent() {
        VoteValidationStrategy.ValidationResult result = adminStrategy.validateVote(adminUser, null);
        
        assertFalse(result.isValid());
        assertEquals("Event cannot be null", result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_Success() {
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(voterUser, testEvent);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_WrongRole() {
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(adminUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("Only voters can cast votes using this strategy", result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_DisabledUser() {
        voterUser.setEnabled(false);
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User account is disabled", result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_ExpiredAccount() {
        voterUser.setAccountNonExpired(false);
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User account has expired", result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_LockedAccount() {
        voterUser.setAccountNonLocked(false);
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User account is locked", result.getErrorMessage());
    }
    
    @Test
    void testStandardVoteValidationStrategy_ExpiredCredentials() {
        voterUser.setCredentialsNonExpired(false);
        VoteValidationStrategy.ValidationResult result = standardStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User credentials have expired", result.getErrorMessage());
    }
    
    @Test
    void testNomineeVoteValidationStrategy_Success() {
        VoteValidationStrategy.ValidationResult result = nomineeStrategy.validateVote(nomineeUser, testEvent);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testNomineeVoteValidationStrategy_WrongRole() {
        VoteValidationStrategy.ValidationResult result = nomineeStrategy.validateVote(voterUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User must be a nominee to use this validation strategy", result.getErrorMessage());
    }
    
    @Test
    void testNomineeVoteValidationStrategy_DisabledUser() {
        nomineeUser.setEnabled(false);
        VoteValidationStrategy.ValidationResult result = nomineeStrategy.validateVote(nomineeUser, testEvent);
        
        assertFalse(result.isValid());
        assertEquals("User account is disabled", result.getErrorMessage());
    }
    
    @Test
    void testStrategyNames() {
        assertEquals("Admin Vote Validation Strategy", adminStrategy.getStrategyName());
        assertEquals("Standard Vote Validation Strategy", standardStrategy.getStrategyName());
        assertEquals("Nominee Vote Validation Strategy", nomineeStrategy.getStrategyName());
    }
    
    @Test
    void testValidationResultHelpers() {
        VoteValidationStrategy.ValidationResult success = VoteValidationStrategy.ValidationResult.success();
        VoteValidationStrategy.ValidationResult failure = VoteValidationStrategy.ValidationResult.failure("Test error");
        
        assertTrue(success.isValid());
        assertNull(success.getErrorMessage());
        
        assertFalse(failure.isValid());
        assertEquals("Test error", failure.getErrorMessage());
    }
}
