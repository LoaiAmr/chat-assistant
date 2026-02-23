-- V1: Create tenants table for multi-tenancy support

CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    daily_token_limit INTEGER,
    monthly_token_limit INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_tenants_api_key ON tenants(api_key);
CREATE INDEX idx_tenants_active ON tenants(active);

-- Comments for documentation
COMMENT ON TABLE tenants IS 'Stores tenant information for multi-tenant support';
COMMENT ON COLUMN tenants.api_key IS 'Unique API key for tenant authentication';
COMMENT ON COLUMN tenants.daily_token_limit IS 'Maximum tokens allowed per day (null = unlimited)';
COMMENT ON COLUMN tenants.monthly_token_limit IS 'Maximum tokens allowed per month (null = unlimited)';
