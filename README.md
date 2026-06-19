# The Imposter - Backend Service

[![Java 21](https://img.shields.io/badge/Java-21-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot 4.0.1](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.5-blue?logo=postgresql)](https://www.postgresql.org/)
[![GraphQL](https://img.shields.io/badge/GraphQL-Apollo-e10098?logo=graphql)](https://graphql.org/)

A robust and scalable backend service for **The Imposter** game, built with modern Java technologies and real-time capabilities. This is a multiplayer party game where players try to identify the impostor among them.

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Monitoring & Observability](#monitoring--observability)
- [Architecture](#architecture)
- [Testing](#testing)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Game Features
- 🎮 **Real-time Multiplayer Gameplay** - WebSocket-powered real-time updates for seamless player interactions
- 🛡️ **Impostor Detection** - Dynamic assignment of impostor roles to players
- 🗣️ **Discussion System** - Coordinated discussion phases where players debate and vote
- 🎯 **Custom Words** - Players can submit custom words for game rounds
- 🌍 **Multi-language Support** - Currently supporting Spanish (ES) and English (EN)
- ⚙️ **Game Difficulty Levels** - Multiple difficulty options (Easy, Medium, Hard, Mixed, Custom)

### Room Management
- 👥 **Room Creation & Joining** - Create public or private game rooms
- 🔧 **Room Configuration** - Customize number of impostors, difficulty, language, and privacy settings
- 👨‍💼 **Admin Controls** - Room admin can edit settings, start games, and manage players
- 🚪 **Player Management** - Join, leave, and kick player functionality with connection tracking

### Player Features
- ✅ **Player Ready Status** - Confirm readiness before game starts
- 💓 **Heartbeat Monitoring** - Track player connection status and detect disconnections
- 📝 **Word Assignment** - Each player gets a secret word (except the impostor)
- 🏆 **Player Statistics** - Track last impostors and game history

### Advanced Features
- 🔐 **Security** - Spring Security integration with authentication support
- 📊 **Rate Limiting** - Bucket4j integration for API rate limiting
- 📈 **Monitoring** - Prometheus metrics and Spring Actuator endpoints
- 📝 **Logging** - Structured logging with Logback and Logstash encoder
- 🗄️ **Database Versioning** - Flyway migration system for schema management

## Technologies

### Core Framework
- **Java 21** - Latest LTS Java version with modern language features
- **Spring Boot 4.0.1** - Production-ready application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Object-relational mapping and database access

### API & Real-time Communication
- **GraphQL** - Query language for flexible API design
- **WebSocket** - Real-time bidirectional communication for live game updates
- **Spring WebFlux** - Reactive web framework support

### Database
- **PostgreSQL 17.5** - Primary relational database
- **Flyway** - Database schema versioning and migration management
- **Hibernate** - ORM framework for database operations

### Monitoring & Observability
- **Spring Actuator** - Application monitoring and metrics
- **Micrometer Prometheus** - Metrics collection and export
- **Logback + Logstash Encoder** - Structured logging for log aggregation
- **Health Probes** - Kubernetes-ready health check endpoints

### Development Tools
- **Lombok** - Reduce boilerplate code with annotations
- **Maven 3.9.9** - Build and dependency management
- **Bucket4j** - Rate limiting library

### DevOps & Deployment
- **Docker** - Multi-stage containerization for optimized images
- **Alpine Linux** - Lightweight runtime container base image

## Prerequisites

- **Java 21 JDK** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Docker & Docker Compose** - For containerized deployment
- **PostgreSQL 17.5** - Relational database
- **Maven 3.9+** - Build tool
- **Git** - Version control

## Installation

### Clone the Repository
```bash
git clone https://github.com/yourusername/the-imposter-be.git
cd the-imposter-be
```

### Build the Project
```bash
# Using Maven wrapper (no Maven installation required)
./mvnw clean package

# Or with Maven installed
mvn clean package
```

### Set Up PostgreSQL
```bash
# Create a new database
createdb theimpostor

# Run migrations (automatic with Spring Boot)
```

## Configuration

### Environment Variables

Configure these environment variables for your deployment:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=theimpostor
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# Security
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_secure_password

# Application
SPRING_PROFILES_ACTIVE=local # or 'production'
```

### Application Profiles

- **local** - Local development with debug logging
- **production** - Production configuration with optimized settings

### Configuration Files

- `application.yaml` - Default configuration
- `application-local.yml` - Local development overrides
- `logback-spring.xml` - Logging configuration

## Running the Application

### Local Development with IDE

1. Set program arguments: `--spring.profiles.active=local`
2. Set environment variable: `-Dspring.profiles.active=local`
3. Run the `TheimposterApplication.java` main class

### Using Maven
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### Docker Container
```bash
# Build Docker image
docker build -t the-imposter-be:latest .

# Run container
docker run -p 8080:8080 -p 8081:8081 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=theimpostor \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_password \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=admin_password \
  the-imposter-be:latest
```

### Docker Compose (Recommended)
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:17.5-alpine
    environment:
      POSTGRES_DB: theimpostor
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  api:
    build: .
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      DB_HOST: postgres
      DB_DATABASE: theimpostor
      DB_USERNAME: postgres
      DB_PASSWORD: your_password
      ADMIN_USERNAME: admin
      ADMIN_PASSWORD: admin_password
    depends_on:
      - postgres

volumes:
  postgres_data:
```

The application will start on port 8080 by default.

## API Documentation

### GraphQL Endpoint
- **URL**: `http://localhost:8080/graphql`
- **WebSocket**: `ws://localhost:8080/graphql` (for subscriptions)

### Main Queries

#### Get Room by Code
```graphql
query {
  getRoomByCode(roomCode: "ABC123") {
    queryCount
    result {
      publicId
      code
      adminPublicId
      numImpostors
      language
      gameStatus
      difficulty
      privateRoom
      players {
        publicId
        name
        admin
        confirmed
        connected
      }
    }
  }
}
```

#### Get Player Word
```graphql
query {
  getPlayerWord(playerPublicId: "player-uuid") {
    queryCount
    result {
      word
    }
  }
}
```

#### Get All Public Rooms
```graphql
query {
  getAllPublicRooms {
    queryCount
    result {
      publicId
      code
      adminName
      numImpostors
      language
      difficulty
    }
  }
}
```

### Main Mutations

#### Create Room
```graphql
mutation {
  createRoom(
    numImpostors: 2
    playerName: "John"
    language: ES
    difficulty: MEDIUM
    privateRoom: false
  ) {
    queryCount
    result {
      publicId
      code
    }
  }
}
```

#### Join Room
```graphql
mutation {
  joinRoom(roomCode: "ABC123", playerName: "Jane") {
    queryCount
    result {
      publicId
      code
    }
  }
}
```

#### Start Game
```graphql
mutation {
  startGame(roomPublicId: "room-uuid") {
    # Returns Boolean
  }
}
```

#### Player Ready
```graphql
mutation {
  playerReady(playerPublicId: "player-uuid") {
    # Returns Boolean
  }
}
```

#### Start Game Discussion
```graphql
mutation {
  startDiscussion(roomPublicId: "room-uuid") {
    # Returns Boolean
  }
}
```

### Subscriptions (Real-time Updates)

```graphql
subscription {
  roomStatus(roomPublicId: "room-uuid") {
    status
    startingPlayer
    language
    numImposters
    lastImposters
    lastWord
    difficulty
    privateRoom
    players {
      publicId
      name
      admin
      confirmed
      connected
    }
  }
}
```

### Data Types

#### GameStatus
- WAITING - Waiting for players
- WORD_SUBMISSION - Custom word submission phase
- STARTED - Game starting
- PLAYING - Active gameplay

#### Language
- ES - Spanish
- EN - English

#### Difficulty
- EASY - Easy difficulty with common words
- MEDIUM - Medium difficulty
- HARD - Hard difficulty with uncommon words
- MIXED - Mixed difficulty
- CUSTOM - Player-submitted custom words

## Database

### Schema
- **Room** - Game room entity with settings and status
- **Player** - Player entity with role and connection status
- **CustomWord** - Custom word submissions for games

### Migrations
Database migrations are automatically applied on startup using Flyway. Located in `src/main/resources/db/migration/`:

```
V20251222181847__initial_tables.sql
V20251228211814__serial_to_bigint_in_room_and_player.sql
V20251230193658__drop_room_name_column.sql
... and more
```

### Connection Pool
- **Driver**: PostgreSQL JDBC Driver
- **Dialect**: PostgreSQL Dialect (Hibernate)
- **DDL-Auto**: Validate (schema validation only)

## Monitoring & Observability

### Metrics Endpoint
- **URL**: `http://localhost:8081/actuator/prometheus`
- **Format**: Prometheus text format

### Health Check Endpoint
- **URL**: `http://localhost:8081/actuator/health`
- **Details**: Always shown with liveliness and readiness probes

### Application Info Endpoint
- **URL**: `http://localhost:8081/actuator/info`

### Logging
- **File**: `logs/app.log`
- **Format**: Structured JSON with Logstash encoder
- **Root Level**: INFO
- **Application Level**: DEBUG (com.roycaroje.theimposter)
- **Flyway Level**: DEBUG

### Management Port
- **Port**: 8081 (separate from application port 8080)
- **Exposed Endpoints**: health, info, prometheus

## Architecture

### Project Structure
```
src/main/java/com/roycaroje/theimposter/
├── config/              # Spring configuration classes
├── graphql/             # GraphQL schema definitions
├── model/               # JPA entities
├── publisher/           # Event publishing for real-time updates
├── repository/          # Spring Data JPA repositories
├── resolver/            # GraphQL resolvers (Query, Mutation, Subscription)
├── service/             # Business logic services
├── transformer/         # DTO transformers
├── util/                # Utility classes
└── TheimposterApplication.java

src/main/resources/
├── application.yaml     # Main configuration
├── application-local.yml # Local development config
├── logback-spring.xml   # Logging configuration
├── words.json           # Default word database
└── db/migration/        # Flyway database migrations
```

### Design Patterns
- **Service Layer** - Separation of business logic
- **Repository Pattern** - Database abstraction
- **DTO Pattern** - Data transfer objects for GraphQL
- **Transformer Pattern** - Entity to DTO transformation
- **Event-Driven** - WebSocket subscriptions for real-time updates

### Key Components
- **GraphQL Resolvers** - Handle GraphQL queries, mutations, and subscriptions
- **Services** - Core business logic (Room, Player, Word management)
- **Publishers** - WebSocket message publishing for real-time updates
- **Security Filter** - Authentication and rate limiting

## Testing

Run the test suite:
```bash
./mvnw test
```

Test Framework & Libraries:
- Spring Boot Test
- Spring Security Test
- GraphQL Test Support
- Spring WebFlux Test

## Development

### Prerequisites for Development
- IDE: IntelliJ IDEA (recommended) or Eclipse
- IDE Plugins: Lombok plugin for your IDE

### Code Style
- Follow standard Java conventions
- Use Lombok annotations to reduce boilerplate
- Include proper JavaDoc for public APIs
- Write meaningful commit messages

### Hot Reload (Spring DevTools)
For quicker development iteration, add Spring DevTools to your POM:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Before Submitting a PR
- Ensure all tests pass: `./mvnw test`
- Build successfully: `./mvnw clean package`
- Follow the existing code style
- Update documentation if needed
- Add tests for new features

## Troubleshooting

### Database Connection Issues
```
Connection refused: localhost:5432
```
**Solution**: Ensure PostgreSQL is running on port 5432 with the correct credentials

### Port Already in Use
```
Address already in use: 0.0.0.0:8080
```
**Solution**: Change the port in `application.yaml` or kill the process using port 8080

### Docker Build Fails
**Solution**: Ensure Docker daemon is running and you have sufficient disk space

## Performance Notes

- **Memory Configuration**: Runs with 512MB min heap and 1GB max heap in Docker
- **Database Indexes**: Configured for optimal query performance
- **Rate Limiting**: Implemented via Bucket4j to prevent abuse
- **WebSocket Optimization**: Efficient message queuing for real-time updates

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues, questions, or suggestions, please open an issue on GitHub or contact the development team.

---

**Made with ❤️ by the Jean Edouard Roy Caro**
