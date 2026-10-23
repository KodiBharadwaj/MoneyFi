CREATE TABLE user_session_time_table (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_time_min INT NOT NULL,

    created_by VARCHAR(100) NOT NULL,
    created_at DATETIME2(3) NOT NULL,

    updated_by VARCHAR(100) NULL,
    updated_at DATETIME2(3) NULL,

    CONSTRAINT FK_session_time_user_auth
     FOREIGN KEY (user_id)
     REFERENCES user_auth_table (id)
);