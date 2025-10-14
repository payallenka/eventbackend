# Event Management Dashboard – Backend

## Overview
This is the Spring Boot backend for the Event Management Dashboard. It provides RESTful APIs for managing events, attendees, and tasks, and supports authentication and real-time updates.

## Features
- CRUD operations for events
- Attendee management and assignment
- Task management with status and deadlines
- JWT-based authentication
- Real-time updates via WebSockets
- PostgreSQL (Supabase) integration

## Setup Instructions

1. **Configure environment variables:**
   - Set the following in your environment or in a `.env` file:
     ```
     SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
     SPRING_DATASOURCE_USERNAME=<username>
     SPRING_DATASOURCE_PASSWORD=<password>
     SUPABASE_JWT_SECRET=<your-256-bit-secret>
     ```

2. **Build the project:**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **API will be available at:**
   - [http://localhost:8080](http://localhost:8080)

## API Endpoints

### Authentication
- `POST /api/auth/register` – Register a new user
- `POST /api/auth/login` – Login and receive JWT

### Events
- `GET /api/events` – List all events
- `POST /api/events` – Create event
- `PUT /api/events/{id}` – Update event
- `DELETE /api/events/{id}` – Delete event

### Attendees
- `GET /api/attendees` – List all attendees
- `POST /api/attendees` – Add attendee
- `DELETE /api/attendees/{id}` – Remove attendee

### Tasks
- `GET /api/events/{eventId}/tasks` – List tasks for an event
- `POST /api/events/{eventId}/tasks` – Create task for event
- `PUT /api/tasks/{id}` – Update task (status, deadline, assignee)
- `DELETE /api/tasks/{id}` – Delete task

## Database
- Uses PostgreSQL (Supabase compatible)
- Entities: Event, Attendee, Task, User
  ![DB design](./dbDesign)

## Real-Time Updates
- WebSocket endpoint for task progress updates

## Security
- JWT authentication for protected endpoints

## Future Work

- Add support for recurring and multi-day events.
- Implement email notifications for attendees and task deadlines.
- Add audit logging for all CRUD operations.
- Improve error handling and validation at the API level.
- Add more granular user roles and permissions.
- Integrate with external calendar APIs (Google Calendar, Outlook).
- Add API rate limiting and monitoring.

## Current Limitations

- No support for recurring or multi-day events.
- No email or push notification system.
- JWT authentication is basic; no refresh tokens or password reset.
- No admin dashboard for system monitoring.
- No built-in analytics or reporting endpoints.

## Deployment Note

- The backend is deployed at: https://eventbackend-kb4u.onrender.com
- All frontend API calls are configured to use this deployed backend by default.
- If you wish to run the backend locally (localhost), please contact the repository owner to update the Supabase database connection string, as the credentials are set for the deployed environment.
