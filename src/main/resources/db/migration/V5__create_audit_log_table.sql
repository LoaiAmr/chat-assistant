-- V5: Create audit_log table for comprehensive audit trail

-- Create enum type for audit event types
CREATE TYPE audit_event_type AS ENUM (
    'CHAT_REQUEST',
    'MODERATION_FLAGGED',
    'RATE_LIMIT_EXCEEDED',
    'TOKEN_BUDGET_EXCEEDED',
    'ERROR'
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    conversation_id UUID REFERENCES conversations(id) ON DELETE SET NULL,
    event_type audit_event_type NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_tenant_created ON audit_logs(tenant_id, created_at DESC);

-- Comments for documentation
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for all system events';
COMMENT ON COLUMN audit_logs.event_type IS 'Type of event: CHAT_REQUEST, MODERATION_FLAGGED, RATE_LIMIT_EXCEEDED, TOKEN_BUDGET_EXCEEDED, ERROR';
COMMENT ON COLUMN audit_logs.event_data IS 'JSON data containing event-specific details';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address of the request originator';
COMMENT ON COLUMN audit_logs.user_agent IS 'User agent string from the request';
COMMENT ON COLUMN audit_logs.correlation_id IS 'Unique ID for tracing requests across the system';
