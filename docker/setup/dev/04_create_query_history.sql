-- Query History table for tracking SQL executions
CREATE TABLE IF NOT EXISTS dev_database.query_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    sql_text TEXT NOT NULL,
    execution_time_ms INT NOT NULL,
    rows_scanned INT,
    rows_returned INT,
    index_used VARCHAR(255),
    explain_result JSON,
    status ENUM('SUCCESS', 'ERROR', 'TIMEOUT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
