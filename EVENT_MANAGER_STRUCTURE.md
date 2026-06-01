# Event Manager Code Structure

## Overview
The Event Manager is a comprehensive module in the voting system that handles all event-related operations including creation, management, and lifecycle tracking of voting events.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Templates (Thymeleaf)                                      │
│  ├── admin/events.html          (Event listing)            │
│  ├── admin/event-form.html      (Create/Edit form)          │
│  └── admin/results.html         (Event results)             │
│                                                             │
│  Controllers                                                │
│  └── AdminController.java       (Event management endpoints)│
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     BUSINESS LAYER                          │
├─────────────────────────────────────────────────────────────┤
│  Services                                                    │
│  ├── EventService.java         (Core business logic)        │
│  ├── VoteService.java          (Vote management)           │
│  ├── ResultService.java        (Results processing)        │
│  └── ExportService.java        (Data export)               │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│  Repositories                                                │
│  ├── EventRepository.java      (Data access)               │
│  ├── VoteRepository.java       (Vote data)                  │
│  ├── NomineeRepository.java    (Nominee data)               │
│  └── ResultRepository.java     (Results data)              │
│                                                             │
│  Entities                                                    │
│  ├── Event.java               (Event entity)               │
│  ├── Vote.java                (Vote entity)                │
│  ├── Nominee.java             (Nominee entity)             │
│  └── Result.java              (Result entity)               │
└─────────────────────────────────────────────────────────────┘
```

## Detailed Component Structure

### 1. Entity Layer (`Event.java`)
```
Event Entity
├── Basic Properties
│   ├── id (Long) - Primary Key
│   ├── title (String) - Event title
│   ├── date (LocalDateTime) - Event date
│   ├── venue (String) - Event location
│   ├── description (String) - Event description
│   └── bannerUrl (String) - Banner image URL
│
├── Timeline Properties
│   ├── nominationDeadline (LocalDateTime)
│   └── votingDeadline (LocalDateTime)
│
├── Relationships
│   ├── nominees (List<Nominee>) - One-to-Many
│   ├── votes (List<Vote>) - One-to-Many
│   └── results (List<Result>) - One-to-Many
│
└── Business Methods
    ├── isVotingOpen() - Check if voting is active
    ├── isNominationOpen() - Check if nominations are open
    └── isVotingClosed() - Check if voting is closed
```

### 2. Repository Layer (`EventRepository.java`)
```
EventRepository Interface
├── Inherits from JpaRepository<Event, Long>
│
├── Custom Query Methods
│   ├── findActiveEvents(LocalDateTime now)
│   ├── findCompletedEvents(LocalDateTime now)
│   ├── findEventsWithOpenNomination(LocalDateTime now)
│   ├── findEventsWithOpenVoting(LocalDateTime now)
│   ├── findAllOrderByDate()
│   └── countActiveEvents(LocalDateTime now)
│
└── JPA Query Annotations
    ├── @Query - Custom JPQL queries
    └── @Param - Parameter binding
```

### 3. Service Layer (`EventService.java`)
```
EventService Class
├── Dependencies
│   └── @Autowired EventRepository
│
├── CRUD Operations
│   ├── getAllEvents() - Get all events
│   ├── getEventById(Long id) - Get specific event
│   ├── createEvent(Event event) - Create new event
│   ├── updateEvent(Event event) - Update existing event
│   └── deleteEvent(Long id) - Delete event
│
├── Business Logic Methods
│   ├── getActiveEvents() - Get currently active events
│   ├── getCompletedEvents() - Get finished events
│   ├── getEventsWithOpenNomination() - Events accepting nominations
│   ├── getEventsWithOpenVoting() - Events accepting votes
│   └── countActiveEvents() - Count active events
│
├── Status Check Methods
│   ├── isEventVotingOpen(Long eventId)
│   └── isEventNominationOpen(Long eventId)
│
└── Validation Logic
    ├── Title validation (3-100 characters)
    ├── Venue validation (3-120 characters)
    ├── Date validation (proper order)
    ├── Description validation (max 1000 chars)
    └── Banner URL validation (HTTP/HTTPS format)
```

### 4. Controller Layer (`AdminController.java`)
```
AdminController Class
├── Request Mapping
│   └── @RequestMapping("/admin")
│
├── Dependencies
│   ├── @Autowired EventService
│   ├── @Autowired UserService
│   ├── @Autowired ResultService
│   ├── @Autowired TicketService
│   ├── @Autowired VoteService
│   └── @Autowired ExportService
│
├── Event Management Endpoints
│   ├── GET /admin/events - List all events
│   ├── GET /admin/events/new - Show create form
│   ├── POST /admin/events - Create new event
│   ├── GET /admin/events/{id}/edit - Show edit form
│   ├── POST /admin/events/{id} - Update event
│   └── POST /admin/events/{id}/delete - Delete event
│
├── Results Management Endpoints
│   ├── GET /admin/results - Show results dashboard
│   ├── GET /admin/results/event/{eventId} - Event-specific results
│   ├── POST /admin/results/publish/{eventId} - Publish results
│   ├── POST /admin/results/generate/{eventId} - Generate results
│   └── GET /admin/results/export/{format}/{resultId} - Export results
│
└── Error Handling
    ├── Try-catch blocks for all operations
    ├── RedirectAttributes for success/error messages
    └── Model attributes for form data
```

### 5. View Layer (`event-form.html`)
```
Event Form Template
├── Form Structure
│   ├── Event Information Card
│   │   ├── Title field (required, 3-100 chars)
│   │   ├── Venue field (required, 3-120 chars)
│   │   ├── Description field (optional, max 1000 chars)
│   │   ├── Banner URL field (optional)
│   │   ├── Event Date field (required)
│   │   ├── Nomination Deadline field (required)
│   │   └── Voting Deadline field (required)
│   │
│   └── Event Guidelines Card
│       ├── Banner preview
│       ├── Field descriptions
│       └── Validation hints
│
├── JavaScript Features
│   ├── File upload functionality
│   ├── Banner preview update
│   ├── Client-side validation
│   ├── Form submission handling
│   └── Error display
│
└── Styling
    ├── Bootstrap classes
    ├── Custom CSS for validation
    ├── Responsive design
    └── Error state styling
```

## Data Flow

```
1. User Request
   ↓
2. AdminController receives request
   ↓
3. Controller calls EventService methods
   ↓
4. Service validates data and calls Repository
   ↓
5. Repository executes database operations
   ↓
6. Data flows back through Service to Controller
   ↓
7. Controller adds data to Model
   ↓
8. Thymeleaf template renders response
   ↓
9. HTML sent to user
```

## Key Features

### Event Lifecycle Management
- **Creation**: Admin creates event with all required details
- **Nomination Phase**: Users can submit nominations
- **Voting Phase**: Users can cast votes
- **Results Phase**: Results are generated and published

### Validation Layers
- **Client-side**: JavaScript validation for immediate feedback
- **Server-side**: Java validation for data integrity
- **Database**: JPA constraints for final validation

### File Management
- **Upload**: Banner images/videos can be uploaded
- **Storage**: Files stored in uploads directory
- **Preview**: Real-time preview of uploaded content

### Error Handling
- **Form Validation**: Field-specific error messages
- **Exception Handling**: Try-catch blocks with user-friendly messages
- **Redirect Attributes**: Success/error flash messages

## Dependencies

### External Libraries
- **Spring Boot**: Framework foundation
- **Spring Data JPA**: Database operations
- **Thymeleaf**: Template engine
- **Lombok**: Code generation
- **Bootstrap**: UI framework

### Internal Dependencies
- **UserService**: User management
- **VoteService**: Vote processing
- **ResultService**: Results calculation
- **ExportService**: Data export functionality

## Security Considerations
- **Admin-only Access**: All event management requires admin role
- **Input Validation**: Comprehensive validation prevents malicious input
- **File Upload Security**: File type and size restrictions
- **SQL Injection Prevention**: JPA parameterized queries

This structure provides a robust, scalable foundation for event management in the voting system with clear separation of concerns and comprehensive error handling.
