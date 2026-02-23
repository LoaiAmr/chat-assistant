# Docker Deployment Guide

This guide explains how to run the chat-assistant application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose v2.0+

## Quick Start

1. **Set up environment variables:**
   ```bash
   # Copy the example env file
   cp .env.example .env

   # Edit .env and add your OpenAI API key
   # Required: OPENAI_API_KEY=your-actual-api-key
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Check service status:**
   ```bash
   docker-compose ps
   ```

4. **View logs:**
   ```bash
   # All services
   docker-compose logs -f

   # Just the application
   docker-compose logs -f app

   # Just the database
   docker-compose logs -f postgres
   ```

5. **Access the application:**
   - Application: http://localhost:8080/api
   - Health check: http://localhost:8080/api/actuator/health
   - Metrics: http://localhost:8080/api/actuator/metrics

## Services

### PostgreSQL Database
- **Container:** chat-assistant-postgres
- **Port:** 5432
- **Database:** chat_assistant_dev
- **User:** chat_user (configurable via .env)
- **Password:** changeme (configurable via .env)
- **Data persistence:** Stored in Docker volume `postgres_data`

### Spring Boot Application
- **Container:** chat-assistant-app
- **Port:** 8080
- **Context path:** /api
- **Depends on:** PostgreSQL (waits for health check)

## Configuration

### Environment Variables

Edit the `.env` file to customize:

- `OPENAI_API_KEY` - Your OpenAI API key (required)
- `OPENAI_BASE_URL` - OpenAI API base URL (default: https://api.openai.com)
- `DB_USERNAME` - Database username (default: chat_user)
- `DB_PASSWORD` - Database password (default: changeme)
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: dev)
- `JAVA_OPTS` - JVM options (default: -Xmx512m -Xms256m)

### Database Initialization

The PostgreSQL container automatically runs initialization scripts from `docker/init-db/` on first startup. These scripts:
- Create required PostgreSQL extensions (uuid-ossp, pg_trgm)
- Set timezone to UTC
- Grant necessary permissions to the database user

The Spring Boot application uses Flyway for database migrations, which run automatically on startup.

## Management Commands

### Start services
```bash
docker-compose up -d
```

### Stop services
```bash
docker-compose stop
```

### Stop and remove containers
```bash
docker-compose down
```

### Stop and remove containers + volumes (deletes all data)
```bash
docker-compose down -v
```

### Rebuild and restart
```bash
docker-compose up -d --build
```

### View container logs
```bash
docker-compose logs -f [service-name]
```

### Execute commands in containers
```bash
# Access PostgreSQL
docker-compose exec postgres psql -U chat_user -d chat_assistant_dev

# Access application container shell
docker-compose exec app sh
```

### Restart a specific service
```bash
docker-compose restart app
docker-compose restart postgres
```

## Troubleshooting

### Application won't start
1. Check if PostgreSQL is healthy:
   ```bash
   docker-compose ps postgres
   ```

2. View application logs:
   ```bash
   docker-compose logs app
   ```

3. Verify environment variables:
   ```bash
   docker-compose config
   ```

### Database connection issues
1. Ensure PostgreSQL container is running and healthy
2. Check database credentials in .env file
3. Verify network connectivity:
   ```bash
   docker-compose exec app ping postgres
   ```

### Port already in use
If ports 8080 or 5432 are already in use, modify the port mappings in `docker-compose.yml`:

```yaml
services:
  postgres:
    ports:
      - "5433:5432"  # Changed host port to 5433

  app:
    ports:
      - "8081:8080"  # Changed host port to 8081
```

### View health status
```bash
# Application health
curl http://localhost:8080/api/actuator/health

# Database health
docker-compose exec postgres pg_isready -U chat_user
```

### Reset database
```bash
# Stop services and remove volumes
docker-compose down -v

# Start services again (fresh database)
docker-compose up -d
```

## Production Considerations

For production deployment, consider:

1. **Security:**
   - Use strong, unique passwords
   - Don't commit .env file to version control
   - Consider using Docker secrets for sensitive data
   - Enable SSL/TLS for PostgreSQL connections
   - Use a reverse proxy (nginx/traefik) with HTTPS

2. **Performance:**
   - Adjust JVM memory settings in JAVA_OPTS
   - Configure PostgreSQL connection pool settings
   - Set up database backups
   - Monitor resource usage

3. **Monitoring:**
   - Set up log aggregation (ELK, Splunk, etc.)
   - Enable Prometheus metrics collection
   - Configure alerting for health check failures

4. **Backups:**
   ```bash
   # Backup database
   docker-compose exec postgres pg_dump -U chat_user chat_assistant_dev > backup.sql

   # Restore database
   docker-compose exec -T postgres psql -U chat_user chat_assistant_dev < backup.sql
   ```

## Development Tips

### Hot reload
For development with hot reload, you can mount the source code as a volume:

```yaml
app:
  volumes:
    - ./src:/app/src
```

Then rebuild when needed:
```bash
docker-compose restart app
```

### Running tests
```bash
# Run tests inside container
docker-compose exec app sh -c "./mvnw test"
```

### Accessing PostgreSQL
```bash
# Via docker exec
docker-compose exec postgres psql -U chat_user -d chat_assistant_dev

# Via local psql client
psql -h localhost -p 5432 -U chat_user -d chat_assistant_dev
```

## Network Architecture

All services run on a dedicated bridge network `chat-assistant-network`, allowing:
- Service-to-service communication using container names
- Isolation from other Docker networks
- Custom DNS resolution within the network

## Volume Management

### List volumes
```bash
docker volume ls | grep chat-assistant
```

### Inspect volume
```bash
docker volume inspect chat-assistant_postgres_data
```

### Backup volume
```bash
docker run --rm -v chat-assistant_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

## Support

For issues or questions:
1. Check application logs: `docker-compose logs -f app`
2. Check database logs: `docker-compose logs -f postgres`
3. Review health endpoints: http://localhost:8080/api/actuator/health
4. Verify configuration: `docker-compose config`
