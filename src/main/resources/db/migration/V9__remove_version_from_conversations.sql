-- V9: Remove version column from conversations table
-- The user decided to avoid using optimistic locking with @Version

ALTER TABLE conversations
DROP COLUMN version;
