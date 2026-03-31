-- Initialize PostgreSQL extensions and default tenant
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create default schema for shared tables
-- The application uses shared database with tenant_id for multi-tenancy

-- Initial database setup complete
-- Flyway will handle table creation via migrations
