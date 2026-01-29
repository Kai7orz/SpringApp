-- User table migration for BCrypt support and timestamp
ALTER TABLE dev_database.user
    MODIFY password VARCHAR(255) NOT NULL,
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Insert default roles if not exist
INSERT IGNORE INTO dev_database.role (id, role_name) VALUES (1, 'ADMIN');
INSERT IGNORE INTO dev_database.role (id, role_name) VALUES (2, 'USER');
