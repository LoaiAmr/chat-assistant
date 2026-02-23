-- V4: Create token_usage table for tracking daily token consumption

CREATE TABLE token_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    usage_date DATE NOT NULL,
    prompt_tokens BIGINT NOT NULL DEFAULT 0,
    completion_tokens BIGINT NOT NULL DEFAULT 0,
    total_tokens BIGINT GENERATED ALWAYS AS (prompt_tokens + completion_tokens) STORED,
    estimated_cost_usd DECIMAL(10, 6),
    request_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, usage_date)
);

-- Indexes for performance
CREATE INDEX idx_token_usage_tenant_date ON token_usage(tenant_id, usage_date DESC);
CREATE INDEX idx_token_usage_date ON token_usage(usage_date DESC);

-- Comments for documentation
COMMENT ON TABLE token_usage IS 'Aggregated daily token usage per tenant for cost tracking and budget enforcement';
COMMENT ON COLUMN token_usage.usage_date IS 'Date of usage (one row per tenant per day)';
COMMENT ON COLUMN token_usage.prompt_tokens IS 'Total prompt tokens used on this date';
COMMENT ON COLUMN token_usage.completion_tokens IS 'Total completion tokens used on this date';
COMMENT ON COLUMN token_usage.total_tokens IS 'Computed column: total tokens (prompt + completion)';
COMMENT ON COLUMN token_usage.estimated_cost_usd IS 'Estimated cost in USD based on OpenAI pricing';
COMMENT ON COLUMN token_usage.request_count IS 'Number of API requests made on this date';
