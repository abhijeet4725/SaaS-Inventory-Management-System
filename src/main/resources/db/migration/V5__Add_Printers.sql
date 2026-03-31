-- V5: Add Printers and Print Jobs tables
-- Flyway migration for printer module

-- Printers table
CREATE TABLE IF NOT EXISTS printers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    printer_type VARCHAR(30) NOT NULL DEFAULT 'THERMAL_80MM',
    connection_type VARCHAR(20) NOT NULL DEFAULT 'NETWORK',
    ip_address VARCHAR(45),
    port INTEGER DEFAULT 9100,
    usb_path VARCHAR(255),
    paper_width INTEGER DEFAULT 80,
    is_default BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT false,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_printers_tenant ON printers(tenant_id);
CREATE INDEX idx_printers_default ON printers(tenant_id, is_default) WHERE is_default = true;

-- Print Jobs table
CREATE TABLE IF NOT EXISTS print_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    printer_id UUID REFERENCES printers(id),
    job_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_type VARCHAR(50),
    reference_id VARCHAR(100),
    content_preview TEXT,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    priority INTEGER DEFAULT 5,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    printed_at TIMESTAMP,
    deleted BOOLEAN DEFAULT false,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_print_jobs_tenant ON print_jobs(tenant_id);
CREATE INDEX idx_print_jobs_status ON print_jobs(tenant_id, status);
CREATE INDEX idx_print_jobs_printer ON print_jobs(printer_id);

-- Add comments
COMMENT ON TABLE printers IS 'Registered thermal and label printers for POS';
COMMENT ON TABLE print_jobs IS 'Print job queue for receipts and invoices';
