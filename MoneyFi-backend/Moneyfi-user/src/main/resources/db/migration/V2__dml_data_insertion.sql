-- To insert pre-defined values in user_role table
IF NOT EXISTS (
   SELECT 1 FROM dbo.user_role_table WHERE role_id = 1
)
BEGIN
INSERT INTO dbo.user_role_table (role_id, role_name)
VALUES (1, 'ADMIN'), (2, 'USER'), (3,'DEVELOPER'), (4,'MAINTAINER');
END

-- To add maintainer default login credentials
IF NOT EXISTS (
    SELECT 1 FROM dbo.user_auth_table WHERE username = 'maintainer@moneyfi-access.com'
)
BEGIN
INSERT INTO dbo.user_auth_table (username, password, is_deleted, is_blocked, role_id, otp_count, login_code_value, last_reset)
VALUES ('maintainer@moneyfi-access.com', '$2a$12$N1G9aEHD8QGhZBj3QC3TDOLpkOMMC2mDrka/YPjqEo2U0mhLwYVVy', 0, 0, 4, 0, 4, GETDATE());
END


-- Fixed reason id and reason names
-- To add maintainer default login credentials
IF NOT EXISTS (
    SELECT 1 FROM dbo.reason_code_table WHERE id = 1
)
BEGIN
INSERT INTO reason_code_table (id, name)
VALUES (1, 'Block Account'), (2, 'Password Change'), (3, 'Name Change'), (4, 'Unblock Account'), (5, 'Delete Account'), (6, 'Account retrieval'),
       (7, 'Phone Number Change'), (8, 'Ignore User Request'), (9, 'Gmail Sync Request Type'), (10, 'Forgot Username'), (11, 'Admin Creation'),
       (12, 'Admin Update'), (13, 'Admin Block'), (14, 'Admin Unblock'), (15, 'Admin Delete'), (16, 'Admin Retrieval');
END