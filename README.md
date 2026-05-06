# CINX Microservices

## Overview
CINX is a robust, scalable microservices-based backend platform designed for an online learning ecosystem. It handles user authentication, course management, enrollments, cart and payments, social interactions, and real-time notifications. The system is distributed across multiple autonomous services to ensure high availability, fault tolerance, and independent scalability.

## Tech Stack
- **Core Framework:** Java 21, Spring Boot 3.5.x, Spring Cloud 2025.0.1
- **Database:** MySQL 8.0 (Database per service)
- **Message Broker:** RabbitMQ 3
- **Service Discovery & Routing:** Netflix Eureka, Spring Cloud Gateway
- **Documentation:** Springdoc OpenAPI (Swagger)
- **Infrastructure & Deployment:** Docker, Docker Compose

## Architecture Summary
### Architecture Type
**Microservices Architecture:** The application is broken down into loosely coupled, highly cohesive services based on business domains.

### Patterns Used
- **API Gateway Pattern:** A single entry point (`gateway` module) routing all client requests to appropriate downstream services.
- **Service Registry & Discovery:** `eureka` and `discovery` services for dynamic service registration and resolution.
- **Database per Service:** Each microservice (e.g., `userdb`, `coursedb`, `cartdb`) has its own logical database to ensure data encapsulation and autonomy.
- **Event-Driven Architecture:** Asynchronous communication between services using **RabbitMQ** (e.g., emitting events after a successful DB write, handled by dead-letter queues on failure).

## Project Structure
The repository is structured as a multi-module Maven project:
```
cinx/
├── auth/            # Authentication & Authorization (JWT)
├── cart/            # Shopping cart management
├── common/          # Shared DTOs, Exceptions, and Utilities
├── course/          # Course catalog and curriculum management
├── enrollment/      # Student course enrollments
├── eureka/          # Eureka Server for service registry
├── gateway/         # API Gateway routing and security filtering
├── learning/        # Quiz, lessons, and progress tracking
├── notification/    # FCM Push Notifications and WebSockets
├── payment/         # Payment processing integration
├── recommendation/  # Recommendation engine
├── social/          # Social features (reviews, discussions, follow)
├── user/            # User and Instructor profiles management
├── mysql-init/      # Database initialization scripts
└── docker-compose.yml
```

### Microservice Internal Structure
Each microservice strictly adheres to the following layered architecture:
`controller/` ➔ `service/` ➔ `repository/` ➔ `model/` ➔ `dto/` ➔ `mapper/`

## How to Run

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose

### 1. Environment Configuration (`.env`)
Create a `.env` file in the root directory based on the variables required. Example:
```env
DOCKERHUB_USERNAME=your_dockerhub_username
# Add other secret keys, JWT secrets, and external API keys here
```

### 2. Running Locally (Development)
You can run the required infrastructure (MySQL, RabbitMQ) using Docker Compose, and run the microservices via Maven or your IDE.
```bash
# Start backing services (MySQL, RabbitMQ)
docker-compose up -d mysql rabbitmq

# Build the project
mvn clean install -DskipTests

# Run Eureka and Gateway first, then other services
mvn spring-boot:run -pl eureka
mvn spring-boot:run -pl gateway
# ... run other services like user, course, auth
```

### 3. Running with Docker Compose (Full Stack)
To spin up the entire microservices ecosystem:
```bash
docker-compose up -d
```
This will pull/build the images and start all services, including Eureka, Gateway, and all domain services, connecting them via `cinx-network`.

## Environment Profiles
The application uses Spring Boot Profiles to separate environment-specific configurations:
- **`dev` Profile:** Used for local development. Typically contains configurations for localhost databases, debug logging, and local RabbitMQ instances.
- **`prod` Profile:** Used for production deployment. Contains production database URLs, strict security configurations, optimized connection pools, and external service URLs.

Run a service with a specific profile using:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Documentation
API documentation is automatically generated using **Springdoc OpenAPI**.
Once the services are running, the aggregated Swagger UI can typically be accessed via the API Gateway. Each service defines its endpoints under `/api/v1/<resource>`.
Protected endpoints require a Bearer JWT Token (`@SecurityRequirement(name = "bearer-jwt")`).

## Deployment
- **Dockerized:** Every microservice is packaged as a Docker container.
- **Registry:** Images are pushed to Docker Hub (`${DOCKERHUB_USERNAME}/cinx-<service>:latest`).
- **Orchestration:** Managed via `docker-compose` for simple single-node deployment, which can be easily adapted for Kubernetes (K8s) or Docker Swarm for clustered production environments.
