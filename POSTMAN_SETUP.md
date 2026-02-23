# Postman Setup Guide

This guide explains how to import and use the Postman collection for testing the Chat Assistant API.

## Files

- `Chat-Assistant-API.postman_collection.json` - Complete API collection with all endpoints
- `Chat-Assistant.postman_environment.json` - Environment variables for local development

## Tenant Authentication

This API uses multi-tenant architecture with API key authentication. Every request to `/api/**` endpoints requires a valid tenant API key.

### Default Test Tenant

For development and testing, a default tenant is automatically created when the database is initialized:

- **Tenant Name:** Development Test Tenant
- **API Key:** `dev-test-api-key-12345`
- **Daily Token Limit:** 100,000 tokens
- **Monthly Token Limit:** 3,000,000 tokens

The Postman collection and environment are pre-configured with this API key.

### How It Works

All API requests must include the header:
```
X-Tenant-API-Key: dev-test-api-key-12345
```

The Postman collection automatically adds this header using the `{{tenantApiKey}}` environment variable. You don't need to manually add it to each request.

## Import Instructions

### 1. Import the Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select **File** tab
4. Choose `Chat-Assistant-API.postman_collection.json`
5. Click **Import**

### 2. Import the Environment (Optional but Recommended)

1. Click **Import** button again
2. Select **File** tab
3. Choose `Chat-Assistant.postman_environment.json`
4. Click **Import**
5. Select "Chat Assistant - Local" from the environment dropdown (top right)

## API Endpoints Overview

### Chat Operations (`/api/v1/chat`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/chat` | POST | Send chat message (creates new conversation or continues existing one) |

**Request Body:**
```json
{
  "conversationId": "optional-uuid",
  "message": "Your message here",
  "systemPrompt": "Optional system prompt",
  "temperature": 0.7,
  "maxTokens": 1000
}
```

**Response:**
```json
{
  "conversationId": "uuid",
  "message": {
    "id": "uuid",
    "role": "ASSISTANT",
    "content": "AI response",
    "promptTokens": 100,
    "completionTokens": 150,
    "createdAt": "2024-01-01T12:00:00Z"
  },
  "tokensUsed": {
    "prompt": 100,
    "completion": 150,
    "total": 250
  },
  "moderationPassed": true
}
```

### Conversation Management (`/api/v1/conversations`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/conversations` | GET | List all conversations (paginated) |
| `/api/v1/conversations/{id}` | GET | Get specific conversation with full message history |
| `/api/v1/conversations/{id}` | DELETE | Delete a conversation |

**List Query Parameters:**
- `page` - Page number (0-indexed, default: 0)
- `size` - Items per page (default: 20)
- `sort` - Sort field and direction (default: updatedAt,desc)

**Example:** `/api/v1/conversations?page=0&size=20&sort=updatedAt,desc`

### Token Usage (`/api/v1/tenants/usage`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/tenants/usage` | GET | Get token usage statistics |

**Query Parameters:**
- `startDate` - Start date in ISO format (YYYY-MM-DD), optional
- `endDate` - End date in ISO format (YYYY-MM-DD), optional

**Example:** `/api/v1/tenants/usage?startDate=2024-01-01&endDate=2024-01-31`

**Response:**
```json
{
  "tenantId": "uuid",
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  },
  "summary": {
    "totalTokens": 50000,
    "promptTokens": 20000,
    "completionTokens": 30000,
    "estimatedCost": 15.00,
    "requestCount": 100
  },
  "dailyBreakdown": [...],
  "limits": {
    "dailyLimit": 100000,
    "monthlyLimit": 3000000,
    "remainingToday": 50000,
    "remainingThisMonth": 2950000
  }
}
```

## Usage Workflow

### 1. Start a New Conversation

Use the **"Send Chat Message (New Conversation)"** request:
- Don't include `conversationId` in the request
- The response will contain a new `conversationId`
- This ID is automatically saved to the environment variable

### 2. Continue the Conversation

Use the **"Send Chat Message (Existing Conversation)"** request:
- The `{{conversationId}}` variable will be automatically populated
- All messages in this conversation maintain context

### 3. View Conversation History

Use **"Get Conversation by ID"** request to see all messages in the conversation.

### 4. Monitor Token Usage

Use **"Get Current Month Usage"** to track your API token consumption and costs.

### 5. Clean Up

Use **"Delete Conversation"** to remove conversations you no longer need.

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `baseUrl` | API base URL | `http://localhost:8089/api` |
| `conversationId` | Current conversation ID (auto-populated) | UUID string |
| `tenantApiKey` | Tenant API key for authentication (auto-included in headers) | `dev-test-api-key-12345` |

## Tips

1. **Auto-save Conversation ID**: The collection includes a test script that automatically saves the `conversationId` from chat responses
2. **Pagination**: Use the pagination parameters in the "List All Conversations" request to navigate through large datasets
3. **Error Handling**: Check the response status codes and error messages for troubleshooting
4. **Token Limits**: Monitor your usage with the Token Usage endpoints to stay within limits

## Application Configuration

Make sure your application is running with these settings (from `application.yaml`):

- **Port**: 8089
- **Context Path**: /api
- **Full Base URL**: http://localhost:8089/api

## Starting the Application

Before testing with Postman, start the application:

```bash
./mvnw spring-boot:run
```

Or if you've built the WAR:

```bash
./mvnw clean package
# Deploy the WAR to your servlet container
```

## Common Issues

### Connection Refused
- Ensure the application is running on port 8089
- Check if another application is using the port

### Invalid Conversation ID
- Make sure you've created a conversation first
- Check that the UUID format is correct

### Validation Errors
- Verify all required fields are present
- Check that values are within allowed ranges (e.g., temperature: 0.0-2.0)

### Tenant Context Not Set
- **Error:** "Tenant context not set. Ensure TenantInterceptor is configured."
- **Cause:** Missing or invalid `X-Tenant-API-Key` header
- **Solution:**
  - Ensure the "Chat Assistant - Local" environment is selected
  - Verify the `tenantApiKey` variable is set to `dev-test-api-key-12345`
  - Check that the header is present in the request (it should be added automatically)
  - Ensure the database migration created the default tenant (run `./mvnw spring-boot:run`)

## Request Examples

### Minimal Chat Request
```json
{
  "message": "Hello!"
}
```

### Full Chat Request
```json
{
  "conversationId": "123e4567-e89b-12d3-a456-426614174000",
  "message": "Explain Spring Boot to me",
  "systemPrompt": "You are a Java expert",
  "temperature": 0.7,
  "maxTokens": 1500
}
```

## API Response Codes

- **200 OK** - Request successful
- **204 No Content** - Deletion successful
- **400 Bad Request** - Validation error or invalid input
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

## Advanced Usage

### Creating Additional Tenants

If you need to test with multiple tenants:

1. **Connect to your PostgreSQL database:**
   ```bash
   psql -U chat_user -d chat_assistant_dev
   ```

2. **Insert a new tenant:**
   ```sql
   INSERT INTO tenants (name, api_key, active, daily_token_limit, monthly_token_limit)
   VALUES ('My Custom Tenant', 'my-custom-api-key', true, 100000, 3000000);
   ```

3. **Update the environment variable:**
   - In Postman, go to the environment settings
   - Update `tenantApiKey` to `my-custom-api-key`
   - All subsequent requests will use the new tenant

### Switching Between Tenants

1. **Create multiple Postman environments** (e.g., "Tenant A", "Tenant B"):
   - Duplicate the "Chat Assistant - Local" environment
   - Name it appropriately (e.g., "Chat Assistant - Tenant A")
   - Set different `tenantApiKey` values in each environment

2. **Switch environments:**
   - Use the dropdown in the top-right corner of Postman
   - Select the environment for the tenant you want to test
   - All requests will automatically use that tenant's API key

### Verifying Tenant Context

Check the application logs to see which tenant is being used:

```
Tenant context set for request: tenantId=a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d, uri=/api/v1/chat
```

This confirms the tenant authentication is working correctly.

### Testing Tenant Isolation

To verify that conversations are isolated per tenant:

1. Send a chat message using Tenant A's API key
2. Switch to Tenant B's API key
3. List conversations - you should only see Tenant B's conversations
4. Switch back to Tenant A - you should only see Tenant A's conversations

This demonstrates the multi-tenant architecture is working correctly.

---

For more information, refer to the API controller documentation in the source code:
- `ChatController.java`
- `ConversationController.java`
- `TokenUsageController.java`
