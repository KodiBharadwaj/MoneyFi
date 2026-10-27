CREATE TABLE email_notification_failed_details_table (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    mdc_request_id VARCHAR(500) NULL,
    reference_number VARCHAR(50) NULL,
    message VARCHAR(MAX) NOT NULL,
    user_details VARCHAR(MAX) NOT NULL,
    created_at DATETIME2(3) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME2(3) NULL,
    updated_by VARCHAR(100) NULL
);