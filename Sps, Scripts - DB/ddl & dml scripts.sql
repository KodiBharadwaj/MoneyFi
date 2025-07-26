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




-- DML Scripts
-- To insert pre defined values in user_role table
INSERT INTO dbo.user_role_table (role_id, role_name)
VALUES (1, 'ADMIN'), (2, 'USER');

-- To add admin default login credentials
INSERT INTO dbo.user_auth_table (username, password, is_deleted, is_blocked, role_id, otp_count)
VALUES ('admin', '$2a$12$N1G9aEHD8QGhZBj3QC3TDOLpkOMMC2mDrka/YPjqEo2U0mhLwYVVy', 0, 0, 1, 0)

