-- V3: Create messages table with moderation and token tracking

-- Create enum types for message role and moderation status
CREATE TYPE message_role AS ENUM ('SYSTEM', 'USER', 'ASSISTANT');
CREATE TYPE moderation_status AS ENUM ('PENDING', 'APPROVED', 'FLAGGED', 'REJECTED');

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role message_role NOT NULL,
    content TEXT NOT NULL,
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    total_tokens INTEGER GENERATED ALWAYS AS (COALESCE(prompt_tokens, 0) + COALESCE(completion_tokens, 0)) STORED,
    moderation_status moderation_status NOT NULL DEFAULT 'PENDING',
    moderation_flags JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_moderation_status ON messages(moderation_status);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at DESC);

-- Partial index for flagged messages
CREATE INDEX idx_messages_flagged ON messages(conversation_id) WHERE moderation_status = 'FLAGGED';

-- Comments for documentation
COMMENT ON TABLE messages IS 'Stores individual chat messages with moderation and token tracking';
COMMENT ON COLUMN messages.role IS 'Message role: SYSTEM (system prompt), USER (user message), or ASSISTANT (AI response)';
COMMENT ON COLUMN messages.content IS 'Message text content';
COMMENT ON COLUMN messages.prompt_tokens IS 'Number of tokens in the prompt (for cost tracking)';
COMMENT ON COLUMN messages.completion_tokens IS 'Number of tokens in the completion (for cost tracking)';
COMMENT ON COLUMN messages.total_tokens IS 'Computed column: total tokens used (prompt + completion)';
COMMENT ON COLUMN messages.moderation_status IS 'Moderation status: PENDING, APPROVED, FLAGGED, or REJECTED';
COMMENT ON COLUMN messages.moderation_flags IS 'JSON data containing moderation flag categories and scores';
