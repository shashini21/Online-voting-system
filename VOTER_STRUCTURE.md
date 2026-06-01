# Voter Structure Documentation

## Overview
The Voter module is a comprehensive component in the voting system that handles all voter-related operations including authentication, voting, profile management, and support ticket creation. It provides a user-friendly interface for voters to participate in the voting process.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Templates (Thymeleaf)                                      │
│  ├── voter/dashboard.html          (Main dashboard)        │
│  ├── voter/events.html             (Event listing)          │
│  ├── voter/event-details.html      (Event details & voting) │
│  ├── voter/event-results.html      (Event results)          │
│  ├── voter/winners.html            (Winners display)        │
│  ├── voter/my-votes.html           (Vote history)           │
│  ├── voter/profile.html            (Profile management)     │
│  ├── voter/tickets.html            (Support tickets)        │
│  ├── voter/ticket-form.html        (Create ticket)          │
│  └── voter/ticket-edit.html        (Edit ticket)            │
│                                                             │
│  Controllers                                                │
│  ├── VoterController.java          (Voter endpoints)        │
│  └── RegistrationController.java   (Voter registration)    │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     BUSINESS LAYER                          │
├─────────────────────────────────────────────────────────────┤
│  Services                                                    │
│  ├── VoteService.java          (Vote processing)           │
│  ├── EventService.java         (Event management)          │
│  ├── UserService.java          (User management)           │
│  ├── TicketService.java        (Support tickets)           │
│  └── ResultService.java        (Results processing)        │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│  Repositories                                                │
│  ├── VoteRepository.java        (Vote data access)          │
│  ├── EventRepository.java       (Event data access)         │
│  ├── UserRepository.java        (User data access)          │
│  ├── NomineeRepository.java     (Nominee data access)       │
│  └── TicketRepository.java      (Ticket data access)        │
│                                                             │
│  Entities                                                    │
│  ├── Vote.java                (Vote entity)                │
│  ├── User.java                (User entity)                │
│  ├── Event.java               (Event entity)               │
│  ├── Nominee.java             (Nominee entity)             │
│  └── Ticket.java              (Ticket entity)              │
└─────────────────────────────────────────────────────────────┘
```

## Detailed Component Structure

### 1. Entity Layer

#### Vote Entity (`Vote.java`)
```
Vote Entity
├── Basic Properties
│   ├── id (Long) - Primary Key
│   ├── timestamp (LocalDateTime) - Vote timestamp
│   └── encryptedVote (String) - Encrypted vote data
│
├── Relationships
│   ├── user (User) - Many-to-One (Voter)
│   ├── nominee (Nominee) - Many-to-One (Voted for)
│   └── event (Event) - Many-to-One (Event context)
│
└── Lifecycle Methods
    └── @PrePersist - Auto-set timestamp
```

#### User Entity (`User.java`)
```
User Entity (Voter)
├── Authentication Properties
│   ├── id (Long) - Primary Key
│   ├── username (String) - Unique username
│   ├── password (String) - Encrypted password
│   ├── email (String) - Unique email
│   └── role (Role) - User role (VOTER)
│
├── Profile Properties
│   ├── firstName (String) - First name
│   ├── lastName (String) - Last name
│   ├── phone (String) - Phone number
│   └── address (String) - Address
│
├── Timestamps
│   ├── createdAt (LocalDateTime)
│   └── updatedAt (LocalDateTime)
│
├── Relationships
│   ├── votes (List<Vote>) - One-to-Many
│   ├── tickets (List<Ticket>) - One-to-Many
│   └── nominees (List<Nominee>) - One-to-Many
│
└── Security Implementation
    └── implements UserDetails - Spring Security
```

#### Nominee Entity (`Nominee.java`)
```
Nominee Entity
├── Basic Properties
│   ├── id (Long) - Primary Key
│   ├── category (String) - Award category
│   ├── nomineeName (String) - Nominee name
│   ├── description (String) - Nominee description
│   ├── mediaUrl (String) - Media file URL
│   └── mediaType (String) - Media type (image/video)
│
├── Relationships
│   ├── event (Event) - Many-to-One
│   ├── user (User) - Many-to-One (Creator)
│   └── votes (List<Vote>) - One-to-Many
│
└── Business Methods
    └── getVoteCount() - Calculate vote count
```

### 2. Repository Layer

#### VoteRepository
```
VoteRepository Interface
├── Inherits from JpaRepository<Vote, Long>
│
├── Custom Query Methods
│   ├── findByUserId(Long userId) - Get user's votes
│   ├── findByEventId(Long eventId) - Get event votes
│   ├── countByUserIdAndEventId(Long userId, Long eventId) - Check if voted
│   ├── countByNomineeId(Long nomineeId) - Count nominee votes
│   ├── countByEventId(Long eventId) - Count event votes
│   ├── findByUserIdAndNomineeId(Long userId, Long nomineeId) - Check specific vote
│   └── getVoteCountByCategoryForEvent(Long eventId) - Category-wise counts
│
└── JPA Query Annotations
    ├── @Query - Custom JPQL queries
    └── @Param - Parameter binding
```

### 3. Service Layer

#### VoteService (`VoteService.java`)
```
VoteService Class
├── Dependencies
│   ├── @Autowired VoteRepository
│   ├── @Autowired EventService
│   ├── @Autowired UserService
│   └── @Autowired NomineeRepository
│
├── Core Voting Operations
│   ├── castVote(Long userId, Long nomineeId, Long eventId) - Cast vote
│   ├── hasUserVotedForEvent(Long userId, Long eventId) - Check voting status
│   ├── hasUserVotedForNominee(Long userId, Long nomineeId) - Check specific vote
│   └── getUserVotes(Long userId) - Get user's vote history
│
├── Vote Retrieval Methods
│   ├── getEventVotes(Long eventId) - Get all event votes
│   ├── getVoteCountForNominee(Long nomineeId) - Count nominee votes
│   ├── getVoteCountForEvent(Long eventId) - Count event votes
│   └── getVoteCountByCategoryForEvent(Long eventId) - Category counts
│
├── Security & Validation
│   ├── Voting window validation
│   ├── Duplicate vote prevention
│   ├── User authentication checks
│   └── Event/nominee existence validation
│
└── Encryption Methods
    ├── encryptVote(String voteData) - Encrypt vote data
    └── decryptVote(String encryptedVote) - Decrypt vote data
```

### 4. Controller Layer

#### VoterController (`VoterController.java`)
```
VoterController Class
├── Request Mapping
│   └── @RequestMapping("/voter")
│
├── Dependencies
│   ├── @Autowired EventService
│   ├── @Autowired VoteService
│   ├── @Autowired NomineeRepository
│   ├── @Autowired TicketService
│   ├── @Autowired ResultService
│   └── @Autowired UserService
│
├── Dashboard & Navigation
│   ├── GET /voter/dashboard - Main dashboard
│   ├── GET /voter/test - Test endpoint
│   └── GET /voter/profile - User profile
│
├── Event Management
│   ├── GET /voter/events - List active events
│   ├── GET /voter/events/{id} - Event details
│   ├── GET /voter/events/{id}/results - Event results
│   └── GET /voter/events/{id}/winners - Event winners
│
├── Voting Operations
│   ├── GET /voter/events/{eventId}/vote/{nomineeId} - Cast vote
│   └── GET /voter/my-votes - Vote history
│
├── Profile Management
│   ├── GET /voter/profile - View profile
│   └── POST /voter/profile - Update profile
│
├── Support System
│   ├── GET /voter/tickets - List tickets
│   ├── GET /voter/tickets/new - Create ticket form
│   ├── POST /voter/tickets - Create ticket
│   ├── GET /voter/tickets/{id}/edit - Edit ticket form
│   └── POST /voter/tickets/{id}/update - Update ticket
│
└── Error Handling
    ├── Try-catch blocks for all operations
    ├── RedirectAttributes for success/error messages
    └── Authentication checks
```

### 5. View Layer

#### Dashboard Template (`dashboard.html`)
```
Voter Dashboard Template
├── Hero Section
│   ├── Welcome message
│   ├── User statistics
│   └── Quick actions
│
├── Statistics Cards
│   ├── Active Events Count
│   ├── Published Results Count
│   ├── Total Votes Count
│   └── Support Tickets Count
│
├── Active Events List
│   ├── Event cards with status
│   ├── Voting status indicators
│   └── Quick action buttons
│
├── Recent Activity
│   ├── Recent votes
│   ├── Recent tickets
│   └── System notifications
│
└── Navigation
    ├── Quick links
    ├── Profile access
    └── Support ticket creation
```

#### Event Details Template (`event-details.html`)
```
Event Details Template
├── Event Information Card
│   ├── Event banner (image/video)
│   ├── Basic event details
│   ├── Timeline information
│   └── Status indicators
│
├── Voting Status Card
│   ├── Voting eligibility check
│   ├── Vote status (voted/not voted)
│   └── Voting instructions
│
├── Nominees Section
│   ├── Category-wise grouping
│   ├── Nominee cards
│   ├── Vote buttons
│   └── Media previews
│
├── Action Buttons
│   ├── Vote for nominees
│   ├── View results
│   └── Back to events
│
└── Responsive Design
    ├── Mobile-friendly layout
    ├── Touch-friendly buttons
    └── Optimized media display
```

## Data Flow

```
1. User Authentication
   ↓
2. VoterController receives request
   ↓
3. Controller validates authentication
   ↓
4. Controller calls appropriate Service methods
   ↓
5. Service validates business rules
   ↓
6. Service calls Repository methods
   ↓
7. Repository executes database operations
   ↓
8. Data flows back through Service to Controller
   ↓
9. Controller adds data to Model
   ↓
10. Thymeleaf template renders response
    ↓
11. HTML sent to user
```

## Key Features

### Authentication & Authorization
- **Spring Security Integration**: Role-based access control
- **Session Management**: Secure user sessions
- **Authentication Checks**: All endpoints protected
- **User Context**: Current user available in all operations

### Voting System
- **Vote Casting**: Secure vote submission
- **Duplicate Prevention**: One vote per user per event
- **Voting Windows**: Time-based voting restrictions
- **Vote Encryption**: Basic vote data encryption
- **Vote History**: Complete voting history tracking

### Profile Management
- **Profile Viewing**: Complete user profile display
- **Profile Updates**: Comprehensive profile editing
- **Validation**: Client and server-side validation
- **Data Integrity**: Unique constraint enforcement

### Support System
- **Ticket Creation**: User support ticket submission
- **Ticket Management**: View and edit own tickets
- **Status Tracking**: Ticket status monitoring
- **Validation**: Comprehensive ticket validation

### Event Interaction
- **Event Browsing**: View available events
- **Event Details**: Comprehensive event information
- **Nominee Viewing**: Category-wise nominee display
- **Results Access**: View voting results and winners

## Security Features

### Vote Security
- **Encryption**: Basic vote data encryption
- **Timestamp Tracking**: Vote timestamp recording
- **Duplicate Prevention**: Database-level constraints
- **Voting Windows**: Time-based restrictions

### User Security
- **Password Encryption**: Secure password storage
- **Session Management**: Secure session handling
- **Input Validation**: Comprehensive input sanitization
- **SQL Injection Prevention**: Parameterized queries

### Access Control
- **Role-Based Access**: VOTER role restrictions
- **Authentication Required**: All endpoints protected
- **Ownership Validation**: Users can only access own data
- **Resource Protection**: Sensitive data protection

## Error Handling

### Client-Side Validation
- **Form Validation**: JavaScript validation
- **Real-time Feedback**: Immediate validation feedback
- **User-Friendly Messages**: Clear error messages
- **Field Highlighting**: Visual error indicators

### Server-Side Validation
- **Business Rule Validation**: Comprehensive validation
- **Exception Handling**: Try-catch blocks
- **Error Messages**: User-friendly error messages
- **Logging**: Detailed error logging

### Database Constraints
- **Unique Constraints**: Prevent duplicate data
- **Foreign Key Constraints**: Maintain referential integrity
- **Data Type Validation**: Database-level validation
- **Transaction Management**: ACID compliance

## Performance Considerations

### Database Optimization
- **Lazy Loading**: Optimized relationship loading
- **Indexed Queries**: Efficient database queries
- **Connection Pooling**: Database connection management
- **Query Optimization**: Efficient data retrieval

### Caching Strategy
- **Session Caching**: User session data
- **Event Caching**: Frequently accessed event data
- **Result Caching**: Computed result caching
- **Static Resource Caching**: CSS/JS caching

### Frontend Optimization
- **Lazy Loading**: On-demand content loading
- **Image Optimization**: Compressed media files
- **CSS/JS Minification**: Optimized resource delivery
- **Responsive Design**: Mobile-optimized layouts

## Dependencies

### External Libraries
- **Spring Boot**: Framework foundation
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **Thymeleaf**: Template engine
- **Bootstrap**: UI framework
- **Lombok**: Code generation

### Internal Dependencies
- **EventService**: Event management
- **UserService**: User management
- **TicketService**: Support ticket management
- **ResultService**: Results processing
- **NomineeRepository**: Nominee data access

This structure provides a comprehensive, secure, and user-friendly voting experience with proper separation of concerns, robust error handling, and extensive validation at all levels.

