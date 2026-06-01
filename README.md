# Voting System

A complete web-based voting system built with Spring Boot, Thymeleaf, Bootstrap, and MS SQL Server.

## Features

### Role-Based Access Control
- **Admin**: Full system access, manage users, events, and results
- **Voter**: Participate in voting and create support tickets
- **Support**: Manage support tickets and help users

### Core Modules
1. **User Management** (Admin only)
   - Create, edit, delete users
   - Assign roles (ADMIN, VOTER, SUPPORT)
   - Update permissions and deadlines

2. **Event Management** (Admin)
   - Create and schedule award events
   - Set nomination and voting deadlines
   - Validate date conflicts

3. **Voting System** (Voter)
   - Select categories and nominees
   - Cast encrypted votes
   - Prevent double voting

4. **Result Management** (Admin)
   - Generate final results after voting ends
   - Export results as PDF/CSV
   - Restrict access during voting

5. **Customer Support** (Support Officer)
   - View and respond to user inquiries
   - Resolve or escalate tickets
   - Track ticket status

## Technology Stack

- **Backend**: Spring Boot 3.5.6
- **Frontend**: Thymeleaf + Bootstrap 5
- **Database**: MS SQL Server
- **Security**: Spring Security with role-based access
- **Build Tool**: Maven

## Database Schema

The system uses the following main tables:
- `users` - User accounts with roles
- `events` - Voting events with deadlines
- `nominees` - Nominees for each category
- `votes` - Encrypted vote records
- `results` - Generated voting results
- `tickets` - Support tickets

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+
- MS SQL Server
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd voting-system
   ```

2. **Configure Database**
   - Create a database named `VotingSystemDB` in MS SQL Server
   - Update `application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VotingSystemDB
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the Application**
   - Open browser and go to `http://localhost:8081`
   - Login with default credentials:
     - Admin: `admin` / `admin123`
     - Voter: `voter1` / `voter123`
     - Support: `support1` / `support123`

## Default Users

The system automatically creates sample users on startup:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | Full system access |
| voter1 | voter123 | VOTER | Can participate in voting |
| support1 | support123 | SUPPORT | Can manage support tickets |

## Project Structure

```
src/main/java/com/votingsystem/voting_system/
├── controller/          # REST controllers for each role
├── entity/             # JPA entities
├── repository/         # Data access layer
├── service/           # Business logic layer
└── config/            # Configuration classes

src/main/resources/
├── templates/          # Thymeleaf templates
│   ├── admin/         # Admin dashboard and forms
│   ├── voter/         # Voter dashboard and voting
│   └── support/       # Support dashboard and tickets
└── application.properties
```

## Security Features

- Role-based access control
- Encrypted password storage
- Session management
- CSRF protection
- Secure redirects after login

## API Endpoints

### Admin Endpoints
- `/admin/dashboard` - Admin dashboard
- `/admin/users` - User management
- `/admin/events` - Event management
- `/admin/results` - Results management

### Voter Endpoints
- `/voter/dashboard` - Voter dashboard
- `/voter/events` - View events
- `/voter/events/{id}` - Event details and voting
- `/voter/my-votes` - View personal votes

### Support Endpoints
- `/support/dashboard` - Support dashboard
- `/support/tickets` - Ticket management
- `/support/tickets/{id}` - Ticket details

## Development

### Adding New Features
1. Create entity classes in `entity/` package
2. Create repository interfaces in `repository/` package
3. Create service classes in `service/` package
4. Create controller classes in `controller/` package
5. Create Thymeleaf templates in `templates/` package

### Database Changes
- Update entity classes with new fields
- Add migration scripts if needed
- Update repository queries

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team or create an issue in the repository.
