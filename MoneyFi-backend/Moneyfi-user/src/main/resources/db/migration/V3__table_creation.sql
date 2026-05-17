CREATE TABLE batch_job_details_addon (
     batch_id BIGINT IDENTITY(1,1) PRIMARY KEY,

     job_type VARCHAR(255) NOT NULL,

     created_by BIGINT NULL,
     updated_by BIGINT NULL,

     created_at DATETIME2 NULL,
     updated_at DATETIME2 NULL
);