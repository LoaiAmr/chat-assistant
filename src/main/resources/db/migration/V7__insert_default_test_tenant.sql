-- V7: Insert default development/test tenant

INSERT INTO tenants (
    id,
    name,
    api_key,
    active,
    daily_token_limit,
    monthly_token_limit,
    created_at,
    updated_at
) VALUES (
    'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d'::uuid,
    'Development Test Tenant',
    'dev-test-api-key-12345',
    true,
    100000,
    3000000,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Comment for documentation
COMMENT ON TABLE tenants IS 'Multi-tenant support with default development tenant for testing';
