# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.9 application with Spring AI integration, packaged as a WAR file for deployment to servlet containers. The project uses:
- Java 17
- Spring AI 1.1.2 (configured for OpenAI integration)
- Maven for build management
- Lombok for boilerplate reduction
- Package name: `com.loai.spring.ai.chat_assistant` (note: underscore, not hyphen)

## Build Commands

**Build the project:**
```bash
./mvnw clean package
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Run tests:**
```bash
./mvnw test
```

**Run a single test class:**
```bash
./mvnw test -Dtest=ChatAssistantApplicationTests
```

**Run a specific test method:**
```bash
./mvnw test -Dtest=ClassName#methodName
```

## Project Structure

- **Main application:** `src/main/java/com/loai/spring/ai/chat_assistant/ChatAssistantApplication.java`
- **Servlet initializer:** `src/main/java/com/loai/spring/ai/chat_assistant/ServletInitializer.java` - enables WAR deployment to external servlet containers
- **Configuration:** `src/main/resources/application.yaml`
- **Static resources:** `src/main/resources/static/`
- **Templates:** `src/main/resources/templates/`

## Key Configuration Notes

- The pom.xml includes Spring AI BOM for dependency management
- Lombok annotation processing is configured in the Maven compiler plugin
- Tomcat is provided scope (WAR packaging for external deployment)
- Spring AI OpenAI integration is available but not yet configured in dependencies

## Maven Wrapper

Use `./mvnw` (Linux/Mac) or `mvnw.cmd` (Windows) instead of system Maven for consistent builds.