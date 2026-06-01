package com.votingsystem.voting_system.config;

import com.votingsystem.voting_system.entity.User;
import com.votingsystem.voting_system.entity.Event;
import com.votingsystem.voting_system.entity.Nominee;
import com.votingsystem.voting_system.service.UserService;
import com.votingsystem.voting_system.service.EventService;
import com.votingsystem.voting_system.repository.NomineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private NomineeRepository nomineeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@votingsystem.com");
            admin.setRole(User.Role.ADMIN);
            admin.setCreatedAt(java.time.LocalDateTime.now());
            admin.setUpdatedAt(java.time.LocalDateTime.now());
            userService.createUser(admin);
        }
        
        // Create sample voter if not exists
        if (!userService.existsByUsername("voter1")) {
            User voter = new User();
            voter.setUsername("voter1");
            voter.setPassword(passwordEncoder.encode("voter123"));
            voter.setEmail("voter1@votingsystem.com");
            voter.setRole(User.Role.VOTER);
            voter.setCreatedAt(java.time.LocalDateTime.now());
            voter.setUpdatedAt(java.time.LocalDateTime.now());
            userService.createUser(voter);
        }
        
        // Create sample support user if not exists
        if (!userService.existsByUsername("support1")) {
            User support = new User();
            support.setUsername("support1");
            support.setPassword(passwordEncoder.encode("support123"));
            support.setEmail("support1@votingsystem.com");
            support.setRole(User.Role.SUPPORT);
            support.setCreatedAt(java.time.LocalDateTime.now());
            support.setUpdatedAt(java.time.LocalDateTime.now());
            userService.createUser(support);
        }
        
        // Create sample event if none exists
        if (eventService.getAllEvents().isEmpty()) {
            Event event = new Event();
            event.setTitle("Annual Awards 2024");
            event.setDate(LocalDateTime.now().plusDays(30));
            event.setVenue("Grand Convention Center");
            event.setDescription("Annual awards ceremony for outstanding achievements");
            event.setNominationDeadline(LocalDateTime.now().plusDays(15));
            event.setVotingDeadline(LocalDateTime.now().plusDays(25));
            eventService.createEvent(event);
            
            // Add sample nominees
            Nominee nominee1 = new Nominee();
            nominee1.setEvent(event);
            nominee1.setCategory("Best Employee");
            nominee1.setNomineeName("John Doe");
            nomineeRepository.save(nominee1);
            
            Nominee nominee2 = new Nominee();
            nominee2.setEvent(event);
            nominee2.setCategory("Best Employee");
            nominee2.setNomineeName("Jane Smith");
            nomineeRepository.save(nominee2);
            
            Nominee nominee3 = new Nominee();
            nominee3.setEvent(event);
            nominee3.setCategory("Best Team");
            nominee3.setNomineeName("Development Team");
            nomineeRepository.save(nominee3);
        }
    }
}
