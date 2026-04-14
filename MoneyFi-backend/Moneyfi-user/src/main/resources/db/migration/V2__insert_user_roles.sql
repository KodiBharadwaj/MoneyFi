-- V1__create_user_roles.sql
CREATE TABLE dbo.user_role_table (
    role_id INT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    CONSTRAINT PK_user_role_table PRIMARY KEY (role_id),
    CONSTRAINT UQ_user_role_table_role_name UNIQUE (role_name)
);