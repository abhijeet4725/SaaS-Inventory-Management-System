-- V2__Seed_Data.sql
-- Insert initial seed data for development

-- Insert default tenant admin user
-- Password: Admin@123 (BCrypt hashed)
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, enabled, email_verified)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'default',
    'admin@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4uE2b7m5zvx.Svt6',
    'Admin',
    'User',
    true,
    true
);

-- Assign admin role
INSERT INTO user_roles (user_id, role) VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ADMIN');

-- Insert sample products
INSERT INTO products (id, tenant_id, name, description, sku, barcode, category, selling_price, cost_price, current_stock, min_stock_level)
VALUES 
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'default', 'Sample Product 1', 'A sample product for testing', 'SKU-001', '1234567890123', 'Electronics', 99.99, 50.00, 100, 10),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'default', 'Sample Product 2', 'Another sample product', 'SKU-002', '1234567890124', 'Electronics', 149.99, 75.00, 50, 5),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'default', 'Sample Service', 'A sample service', 'SVC-001', NULL, 'Services', 199.99, 0.00, 0, 0);

-- Update service flag for the service product
UPDATE products SET is_service = true, track_inventory = false WHERE id = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23';
