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

