-- Database initialization script for chat-assistant
-- This script runs automatically when the PostgreSQL container starts for the first time

-- Ensure the database exists (should already be created via POSTGRES_DB env var)
-- Create any required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set default timezone
SET TIME ZONE 'UTC';

-- Grant necessary permissions to the user
GRANT ALL PRIVILEGES ON DATABASE chat_assistant_dev TO chat_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO chat_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO chat_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO chat_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO chat_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO chat_user;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully';
END $$;
