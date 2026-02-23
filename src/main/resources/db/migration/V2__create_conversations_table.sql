-- V2: Create conversations table

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_conversations_tenant_id ON conversations(tenant_id);
CREATE INDEX idx_conversations_created_at ON conversations(created_at DESC);
CREATE INDEX idx_conversations_tenant_created ON conversations(tenant_id, created_at DESC);

-- Comments for documentation
COMMENT ON TABLE conversations IS 'Stores conversation groupings for chat messages';
COMMENT ON COLUMN conversations.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';
COMMENT ON COLUMN conversations.title IS 'Optional conversation title (can be auto-generated)';
