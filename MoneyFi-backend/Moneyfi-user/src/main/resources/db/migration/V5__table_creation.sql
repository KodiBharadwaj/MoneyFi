CREATE TABLE email_notification_failed_details_table (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    mdc_request_id VARCHAR(500) NULL,
    reference_number VARCHAR(50) NULL,
    message VARCHAR(MAX) NOT NULL,
    username VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(500) NOT NULL,
    created_at DATETIME2(3) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME2(3) NULL,
    updated_by VARCHAR(100) NULL
);