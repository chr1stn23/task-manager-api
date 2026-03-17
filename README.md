# Task Manager API

REST API for managing tasks with authentication and user-based access control.

Built with Spring Boot and secured using JWT.

---

## Features

- User registration and authentication
- JWT-based authentication
- CRUD operations for tasks
- Pagination and filtering
- Multi-user task isolation
- Global error handling
- Input validation
- Swagger API documentation

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT Authentication
- OpenAPI / Swagger
- PostgreSQL (or H2 for development)
- Maven

---

## Architecture

The project follows a layered architecture:

Controller → Service → Repository → Database

- Controller: Handles HTTP requests
- Service: Business logic
- Repository: Data access layer
- Entity: Database models
- DTO: Request and response objects

---

## Project Structure

- src/main/java/com/christian/taskmanager
    - config
    - controller
    - dto
    - entity
    - exception
    - mapper
    - repository
    - scheduler
    - security
    - service
    - util

---

## Authentication

The API uses JWT tokens for authentication.

### Login

POST /api/auth/login

Response example:

```
{
  "success": true,
  "data": {
    "token": "JWT_TOKEN"
  }
}
```

Use the token in requests:

Authorization: Bearer JWT_TOKEN

---

## API Documentation

Swagger UI is available at:

http://localhost:8080/swagger-ui/index.html

---

## Environment Variables & Docker

The project uses environment variables for sensitive configuration, such as JWT secrets and admin credentials. Create an
.env file in the project root with the following variables:

```
# JWT secret key
JWT_SECRET=your_jwt_secret

# Admin user credentials
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=secure_password
```

---

## Running with Docker Compose

You can start the database and backend using Docker Compose:

```
docker-compose up --build
```

This will:

- Launch a PostgreSQL database with persistent storage.
- Build and run the Spring Boot backend.
- Automatically configure environment variables from your .env.

> Note: If you use Docker Compose, you don't need to run mvn spring-boot:run.
---

## Running the Project

### 1. Clone the repository

```
git clone https://github.com/chr1stn23/task-manager-api.git
```

### 2. Navigate to the project

```
cd task-manager-api
```

### 3. Run the application

``mvn spring-boot:run``

---

## Example Endpoints

| Method  | Endpoint                | Description         |
|---------|-------------------------|---------------------|
| POST    | /api/auth/register      | Register a new user |
| POST    | /api/auth/login         | Authenticate user   |
| GET     | /api/tasks              | Get tasks           |
| POST    | /api/tasks              | Create task         |
| PUT     | /api/tasks/{id}         | Update task         |
| DELETE  | /api/tasks/{id}         | Delete task         |
| RESTORE | /api/tasks/{id}/restore | Restore task        |

---

## Error Handling

All responses follow a consistent format:

```
{
  "success": false,
  "timestamp": "2026-03-07T14:00:00",
  "error": {
    "message": "Task not found",
    "code": "NOT_FOUND"
  }
}
```

---

## Author

Christian Lara