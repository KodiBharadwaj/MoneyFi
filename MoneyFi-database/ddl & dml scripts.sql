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
VALUES (1, 'ADMIN'), (2, 'USER'), (3,'DEVELOPER'), (4,'MAINTAINER');

-- To add maintainer default login credentials
INSERT INTO dbo.user_auth_table (username, password, is_deleted, is_blocked, role_id, otp_count, login_code_value)
VALUES ('maintainer@moneyfi-access.com', '$2a$12$N1G9aEHD8QGhZBj3QC3TDOLpkOMMC2mDrka/YPjqEo2U0mhLwYVVy', 0, 0, 4, 0, 4)


-- Credentials for monitor service as client
-- username : client
-- password : client123
INSERT INTO dbo.user_auth_table (username, password, is_deleted, is_blocked, role_id, otp_count)
VALUES ('client', '$2a$12$H3ThrYDby00iblvWzKHt7OLBHkEiQvx4ZJCiWIK35FRlG5cTD8J8G', 0, 0, 3, 0)


-- Fixed reason id and reason names
INSERT INTO reason_code_table (id, name)
VALUES (1, 'Block Account'), (2, 'Password Change'), (3, 'Name Change'), (4, 'Unblock Account'), (5, 'Delete Account'), (6, 'Account retrieval'), 
(7, 'Phone Number Change'), (8, 'Ignore User Request'), (9, 'Gmail Sync Request Type'), (10, 'Forgot Username'), (11, 'Admin Creation'), 
(12, 'Admin Update'), (13, 'Admin Block'), (14, 'Admin Unblock'), (15, 'Admin Delete'), (16, 'Admin Retrieval');


--sample code for primary foreign key relationship
ALTER TABLE user_notification_table
ADD CONSTRAINT FK_user_notification_schedule_id
FOREIGN KEY (schedule_id) 
REFERENCES schedule_notification_table(id);

