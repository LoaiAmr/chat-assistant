-- V8: Add version column for optimistic locking

ALTER TABLE conversations
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Comment for documentation
COMMENT ON COLUMN conversations.version IS 'Version number for optimistic locking (JPA @Version)';
