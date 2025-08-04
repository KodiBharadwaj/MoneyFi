-- NOTE
-- First run the DDL Scripts listed below and then DML Scripts which are defined after DDL Scripts

-- DDL Scripts
-- To create user_role table
CREATE TABLE dbo.user_role_table (
    role_id INT NOT NULL,
    role_name VARCHAR(50) NOT NULL,

    CONSTRAINT PK_user_role_table PRIMARY KEY (role_id),
    CONSTRAINT UQ_user_role_table_role_name UNIQUE (role_name)
);

-- To store the excel temples.
CREATE TABLE excel_template (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    content VARBINARY(MAX),
    content_type VARCHAR(100)
);





-- DML Scripts
-- To insert pre defined values in user_role table
INSERT INTO dbo.user_role_table (role_id, role_name)
VALUES (1, 'ADMIN'), (2, 'USER');

-- To add admin default login credentials
INSERT INTO dbo.user_auth_table (username, password, is_deleted, is_blocked, role_id, otp_count)
VALUES ('admin', '$2a$12$N1G9aEHD8QGhZBj3QC3TDOLpkOMMC2mDrka/YPjqEo2U0mhLwYVVy', 0, 0, 1, 0)

-- Insert an excel template with necessary path for user profile upload and parsing
INSERT INTO excel_template (id, name, content, content_type)
SELECT 1, 'profile-template.xlsx',
       BulkColumn,
       'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
FROM OPENROWSET(BULK 'C:\sample.xlsx', SINGLE_BLOB) AS x;

