package com.votingsystem.voting_system.observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Configuration class for the Observer Pattern implementation.
 * This class registers all vote observers with the vote subject.
 */
@Configuration
public class ObserverConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ObserverConfig.class);
    
    @Autowired
    private VoteSubjectImpl voteSubject;
    
    @Autowired
    private List<VoteObserver> voteObservers;
    
    /**
     * Initialize the observer pattern by registering all observers.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    public void initializeObservers() {
        logger.info("Initializing Observer Pattern with {} observers", voteObservers.size());
        
        for (VoteObserver observer : voteObservers) {
            voteSubject.registerObserver(observer);
            logger.info("Registered observer: {}", observer.getObserverName());
        }
        
        logger.info("Observer Pattern initialization complete. Total observers: {}", 
                   voteSubject.getObserverCount());
    }
    
    /**
     * Bean definition for VoteSubjectImpl.
     * This ensures the subject is properly managed by Spring.
     * 
     * @return VoteSubjectImpl instance
     */
    @Bean
    public VoteSubjectImpl voteSubject() {
        return new VoteSubjectImpl();
    }
    
    /**
     * Get the vote subject for external use.
     * 
     * @return The vote subject instance
     */
    public VoteSubject getVoteSubject() {
        return voteSubject;
    }
}
