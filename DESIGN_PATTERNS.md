# Design Patterns Used in Voting System

This document outlines all the design patterns implemented in the Voting System application.

## Overview

The voting system is built using Spring Boot framework and implements multiple design patterns to ensure maintainability, scalability, and code reusability.

## 1. Layered Architecture Pattern (N-Tier Architecture)

### Description
The application follows a clear separation of concerns across multiple layers.

### Implementation
```
┌─────────────────────────────────────┐
│           Controller Layer          │ ← AdminController, VoterController, etc.
├─────────────────────────────────────┤
│           Service Layer             │ ← UserService, EventService, VoteService
├─────────────────────────────────────┤
│          Repository Layer           │ ← UserRepository, EventRepository, etc.
├─────────────────────────────────────┤
│           Entity Layer              │ ← User, Event, Vote, Nominee, Result
└─────────────────────────────────────┘
```

### Benefits
- Clear separation of concerns
- Easy to maintain and test
- Scalable architecture

---

## 2. Repository Pattern

### Description
Encapsulates data access logic and provides a uniform interface for data operations.

### Implementation
```java
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserId(Long userId);
    List<Vote> findByEventId(Long eventId);
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.nominee.id = :nomineeId")
    long countByNomineeId(@Param("nomineeId") Long nomineeId);
}
```

### Files Using This Pattern
- `VoteRepository.java`
- `UserRepository.java`
- `EventRepository.java`
- `NomineeRepository.java`
- `ResultRepository.java`
- `TicketRepository.java`

### Benefits
- Abstracts data access logic
- Provides consistent data access interface
- Easy to mock for testing

---

## 3. Service Layer Pattern

### Description
Encapsulates business logic and provides a clean interface for controllers.

### Implementation
```java
@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private EventService eventService;
    
    public Vote castVote(Long userId, Long nomineeId, Long eventId) {
        // Business logic for casting votes
        if (!eventService.isEventVotingOpen(eventId)) {
            throw new RuntimeException("Voting is not open for this event");
        }
        // ... additional business logic
        return voteRepository.save(vote);
    }
}
```

### Files Using This Pattern
- `VoteService.java`
- `UserService.java`
- `EventService.java`
- `ResultService.java`
- `TicketService.java`
- `ExportService.java`

### Benefits
- Centralizes business logic
- Promotes code reusability
- Makes testing easier

---

## 4. Dependency Injection Pattern

### Description
Objects receive their dependencies from external sources rather than creating them internally.

### Implementation
```java
@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
}
```

### Benefits
- Loose coupling between classes
- Easy to test and mock dependencies
- Flexible configuration

---

## 5. Data Transfer Object (DTO) Pattern

### Description
Objects that carry data between different layers without business logic.

### Implementation
```java
// In ResultService.java
public static class NomineeVoteCount {
    private String nomineeName;
    private String category;
    private Long voteCount;
    
    // Constructors, getters, and setters
}
```

### Benefits
- Reduces network calls
- Encapsulates data transfer
- Improves performance

---

## 6. Template Method Pattern

### Description
Defines the skeleton of an algorithm in a method, deferring some steps to subclasses.

### Implementation
```java
@Service
public class ExportService {
    public byte[] exportResultsToPdf(Long resultId) throws IOException {
        // Template method structure
        Result result = getResult(resultId);
        Event event = result.getEvent();
        List<NomineeVoteCount> nomineeVoteCounts = getNomineeVoteCounts(event.getId());
        
        // PDF-specific formatting
        StringBuilder content = new StringBuilder();
        content.append("VOTING RESULTS REPORT\n");
        // ... PDF formatting logic
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    public byte[] exportResultsToExcel(Long resultId) throws IOException {
        // Same template structure
        Result result = getResult(resultId);
        Event event = result.getEvent();
        List<NomineeVoteCount> nomineeVoteCounts = getNomineeVoteCounts(event.getId());
        
        // Excel-specific formatting
        StringBuilder content = new StringBuilder();
        content.append("Event,").append(event.getTitle()).append("\n");
        // ... Excel formatting logic
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }
}
```

### Benefits
- Code reusability
- Consistent algorithm structure
- Easy to maintain

---

## 7. Strategy Pattern

### Description
Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

### Implementation
```java
// Role-based access control
public enum Role {
    ADMIN, VOTER, SUPPORT, NOMINEE
}

// Different strategies for different user types
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/nominee/**").hasRole("NOMINEE")
            .requestMatchers("/support/**").hasRole("SUPPORT")
            // ... different access strategies
        );
        return http.build();
    }
}
```

### Benefits
- Flexible algorithm selection
- Easy to add new strategies
- Eliminates conditional statements

---

## 8. Observer Pattern (Implicit)

### Description
Defines a one-to-many dependency between objects so that when one object changes state, all dependents are notified.

### Implementation
```java
@Entity
@Table(name = "votes")
public class Vote {
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        // Update logic
    }
}
```

### Benefits
- Loose coupling between objects
- Automatic state synchronization
- Extensible event handling

---

## 9. Factory Pattern (Implicit)

### Description
Creates objects without specifying their exact class.

### Implementation
```java
@SpringBootApplication
public class VotingSystemApplication {
    public static void main(String[] args) {
        // Spring Boot acts as a factory
        SpringApplication.run(VotingSystemApplication.class, args);
    }
}

// Bean creation through Spring
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Benefits
- Centralized object creation
- Easy to modify object creation logic
- Reduces coupling

---

## 10. Builder Pattern (Implicit)

### Description
Constructs complex objects step by step.

### Implementation
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    // ... other fields
}
```

### Benefits
- Flexible object construction
- Readable code
- Immutable objects

---

## 11. Adapter Pattern

### Description
Allows incompatible interfaces to work together.

### Implementation
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Adapter: converts custom User to Spring Security UserDetails
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user; // User implements UserDetails interface
    }
}
```

### Benefits
- Integration with third-party libraries
- Maintains existing interfaces
- Flexible adaptation

---

## 12. MVC Pattern (Model-View-Controller)

### Description
Separates application into three interconnected components.

### Implementation
```
┌─────────────────────────────────────┐
│            Controllers              │ ← AdminController, VoterController
├─────────────────────────────────────┤
│            Services                 │ ← Business Logic
├─────────────────────────────────────┤
│             Models                  │ ← JPA Entities
└─────────────────────────────────────┘
```

### Files Structure
- **Models**: `User.java`, `Event.java`, `Vote.java`, `Nominee.java`, `Result.java`
- **Views**: Thymeleaf templates in `templates/` directory
- **Controllers**: `AdminController.java`, `VoterController.java`, `NomineeController.java`

### Benefits
- Clear separation of concerns
- Easy to maintain
- Scalable architecture

---

## 13. Configuration Pattern

### Description
Centralizes configuration management and system setup.

### Implementation
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Security configuration
        return http.build();
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resource configuration
    }
}
```

### Benefits
- Centralized configuration
- Easy to modify system behavior
- Environment-specific settings

---

## 14. Validation Pattern

### Description
Ensures data integrity and business rules compliance.

### Implementation
```java
@Service
public class EventService {
    public Event createEvent(Event event) {
        // Validation logic
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Event title is required");
        }
        if (event.getTitle().trim().length() < 3) {
            throw new RuntimeException("Event title must be at least 3 characters long");
        }
        // ... more validation
        return eventRepository.save(event);
    }
}
```

### Benefits
- Data integrity
- Early error detection
- Consistent validation rules

---

## 15. Decorator Pattern

### Description
Adds new functionality to objects without altering their structure.

### Implementation
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    // ... other fields with JPA annotations
}
```

### Benefits
- Flexible object enhancement
- Maintains original interface
- Composable functionality

---

## Additional Patterns and Practices

### 16. Singleton Pattern (Implicit)
Spring beans are singletons by default, ensuring single instance per application context.

### 17. Proxy Pattern (Implicit)
Spring AOP uses proxy pattern for cross-cutting concerns like security and transactions.

### 18. Command Pattern (Implicit)
Spring MVC controllers handle HTTP requests as commands.

### 19. Facade Pattern
Service layer acts as a facade, providing simplified interface to complex subsystems.

### 20. Specification Pattern
JPA query methods implement specification pattern for flexible data queries.

---

## Benefits of Using Design Patterns

1. **Maintainability**: Code is easier to understand and modify
2. **Scalability**: System can grow without major restructuring
3. **Testability**: Components can be tested in isolation
4. **Reusability**: Common solutions can be applied across the application
5. **Flexibility**: System can adapt to changing requirements
6. **Best Practices**: Follows industry-standard approaches

---

## Conclusion

The Voting System demonstrates excellent software engineering practices by implementing multiple design patterns that work together to create a robust, maintainable, and scalable application. These patterns ensure the codebase follows Spring Boot best practices and can easily accommodate future enhancements and modifications.

## Files Referenced

### Controllers
- `AdminController.java`
- `VoterController.java`
- `NomineeController.java`
- `SupportController.java`
- `RegistrationController.java`
- `MainController.java`

### Services
- `VoteService.java`
- `UserService.java`
- `EventService.java`
- `ResultService.java`
- `TicketService.java`
- `ExportService.java`

### Repositories
- `VoteRepository.java`
- `UserRepository.java`
- `EventRepository.java`
- `NomineeRepository.java`
- `ResultRepository.java`
- `TicketRepository.java`

### Entities
- `User.java`
- `Event.java`
- `Vote.java`
- `Nominee.java`
- `Result.java`
- `Ticket.java`

### Configuration
- `SecurityConfig.java`
- `WebConfig.java`
- `CustomUserDetailsService.java`
- `CustomAuthSuccessHandler.java`
