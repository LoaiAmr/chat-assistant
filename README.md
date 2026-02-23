# Chat Assistant

A multi-tenant Spring Boot application that provides an AI-powered chat assistant using OpenAI's GPT models. This application features conversation management, content moderation, token usage tracking, and comprehensive rate limiting.

## Features

- **Multi-tenant Architecture**: Supports multiple tenants with isolated data and configurations
- **OpenAI Integration**: Powered by OpenAI's GPT models (default: gpt-4o-mini)
- **Conversation Management**: Create, retrieve, and manage chat conversations
- **Content Moderation**: Built-in content moderation using OpenAI's moderation API
- **Token Usage Tracking**: Monitor and track token consumption with cost calculation
- **Rate Limiting**: Configurable rate limiting per tenant
- **Resilience**: Circuit breaker pattern and retry mechanisms using Resilience4j
- **Caching**: Response caching using Caffeine for improved performance
- **Audit Logging**: Comprehensive audit trail for all operations
- **Database Migrations**: Flyway-based database version control
- **RESTful API**: Clean REST API with proper error handling

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.9**
- **Spring AI 1.1.2** (OpenAI integration)
- **PostgreSQL** (Database)
- **Flyway** (Database migrations)
- **Resilience4j** (Circuit breaker, rate limiter)
- **Caffeine** (Caching)
- **Lombok** (Boilerplate reduction)
- **Maven** (Build tool)

## Architecture

This project follows **Hexagonal Architecture (Ports and Adapters)**:

```
├── domain/              # Core business logic and entities
├── application/         # Use cases and application services
├── infrastructure/      # External integrations (DB, AI, cache)
└── presentation/        # REST controllers and DTOs
```

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+ (or use included Maven wrapper)
- OpenAI API key

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/LoaiAmr/chat-assistant.git
cd chat-assistant
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE chat_assistant_dev;
CREATE USER chat_user WITH PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE chat_assistant_dev TO chat_user;
```

Or use Docker Compose (if configured):

```bash
docker-compose up -d
```

### 3. Configuration

Create a `.env` file or set environment variables:

```bash
# Database Configuration
DB_USERNAME=chat_user
DB_PASSWORD=changeme

# OpenAI Configuration
OPENAI_API_KEY=your-actual-api-key-here
OPENAI_BASE_URL=https://api.openai.com

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

Alternatively, update `src/main/resources/application.yaml` directly.

### 4. Build the Project

```bash
./mvnw clean package
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8089/api`

## API Documentation

### Health Check

```bash
GET http://localhost:8089/api/actuator/health
```

### Chat Endpoints

#### Send a Chat Message

```bash
POST http://localhost:8089/api/v1/chat
Headers:
  X-Tenant-ID: test-tenant-001
  Content-Type: application/json

Body:
{
  "message": "Hello, how are you?",
  "conversationId": "optional-conversation-id"
}
```

### Conversation Endpoints

#### Get Conversation History

```bash
GET http://localhost:8089/api/v1/conversations/{conversationId}
Headers:
  X-Tenant-ID: test-tenant-001
```

#### List Conversations

```bash
GET http://localhost:8089/api/v1/conversations
Headers:
  X-Tenant-ID: test-tenant-001
```

### Token Usage Endpoints

#### Get Token Usage Statistics

```bash
GET http://localhost:8089/api/v1/token-usage
Headers:
  X-Tenant-ID: test-tenant-001
```

## Configuration

Key configuration options in `application.yaml`:

### AI Configuration

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          max-tokens: 2000
```

### Rate Limiting

```yaml
chat:
  rate-limit:
    requests-per-minute: 60
    requests-per-hour: 1000
    enabled: true
```

### Content Moderation

```yaml
chat:
  moderation:
    enabled: true
    reject-on-flag: true
    categories:
      - HATE
      - SEXUAL
      - VIOLENCE
      - SELF_HARM
```

### Token Budget

```yaml
chat:
  token:
    default-daily-limit: 100000
    default-monthly-limit: 3000000
    cost-per-1k-prompt-tokens: 0.00015
    cost-per-1k-completion-tokens: 0.0006
```

## Testing

Run all tests:

```bash
./mvnw test
```

Run a specific test class:

```bash
./mvnw test -Dtest=ChatControllerTest
```

## Project Structure

```
src/
├── main/
│   ├── java/com/loai/spring/ai/chat_assistant/
│   │   ├── application/           # Application layer
│   │   │   ├── dto/              # Request/Response DTOs
│   │   │   ├── exception/        # Application exceptions
│   │   │   ├── port/             # Ports (interfaces)
│   │   │   ├── service/          # Application services
│   │   │   └── usecase/          # Use cases
│   │   ├── domain/               # Domain layer
│   │   │   ├── exception/        # Domain exceptions
│   │   │   ├── model/            # Domain entities
│   │   │   └── repository/       # Repository interfaces
│   │   ├── infrastructure/       # Infrastructure layer
│   │   │   ├── ai/              # AI provider implementations
│   │   │   ├── audit/           # Audit service
│   │   │   ├── cache/           # Cache implementation
│   │   │   ├── config/          # Spring configurations
│   │   │   ├── persistence/     # JPA entities and repositories
│   │   │   ├── ratelimit/       # Rate limiting
│   │   │   └── security/        # Security (tenant context)
│   │   └── presentation/        # Presentation layer
│   │       ├── exception/       # Global exception handler
│   │       └── rest/            # REST controllers
│   └── resources/
│       ├── application.yaml     # Application configuration
│       └── db/migration/        # Flyway migrations
└── test/                        # Test classes
```

## Deployment

### WAR Deployment

This application is packaged as a WAR file for deployment to servlet containers (Tomcat, Jetty, etc.):

```bash
./mvnw clean package
```

The WAR file will be in `target/chat-assistant-0.0.1-SNAPSHOT.war`

### Docker Deployment

See [DOCKER.md](DOCKER.md) for Docker deployment instructions.

## Monitoring

The application includes Spring Boot Actuator endpoints:

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
- Info: `/actuator/info`

## Postman Collection

Import the Postman collection for easy API testing:

- Collection: `Chat-Assistant-API.postman_collection.json`
- Environment: `Chat-Assistant.postman_environment.json`

See [POSTMAN_SETUP.md](POSTMAN_SETUP.md) for setup instructions.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [Spring AI](https://spring.io/projects/spring-ai)
- Powered by [OpenAI](https://openai.com/)
- Circuit breaker by [Resilience4j](https://resilience4j.readme.io/)