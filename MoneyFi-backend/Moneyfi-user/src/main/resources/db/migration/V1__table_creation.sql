/****** Object:  UserDefinedTableType [dbo].[IdListType]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TYPE [dbo].[IdListType] AS TABLE(
    [id] [bigint] NULL
    )
/****** Object:  Table [dbo].[BATCH_JOB_EXECUTION]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_JOB_EXECUTION](
    [JOB_EXECUTION_ID] [bigint] NOT NULL,
    [VERSION] [bigint] NULL,
    [JOB_INSTANCE_ID] [bigint] NOT NULL,
    [CREATE_TIME] [datetime] NOT NULL,
    [START_TIME] [datetime] NULL,
    [END_TIME] [datetime] NULL,
    [STATUS] [varchar](10) NULL,
    [EXIT_CODE] [varchar](2500) NULL,
    [EXIT_MESSAGE] [varchar](2500) NULL,
    [LAST_UPDATED] [datetime] NULL,
    PRIMARY KEY CLUSTERED
(
[JOB_EXECUTION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[BATCH_JOB_EXECUTION_CONTEXT]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_JOB_EXECUTION_CONTEXT](
    [JOB_EXECUTION_ID] [bigint] NOT NULL,
    [SHORT_CONTEXT] [varchar](2500) NOT NULL,
    [SERIALIZED_CONTEXT] [varchar](max) NULL,
    PRIMARY KEY CLUSTERED
(
[JOB_EXECUTION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
/****** Object:  Table [dbo].[BATCH_JOB_EXECUTION_PARAMS]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_JOB_EXECUTION_PARAMS](
    [JOB_EXECUTION_ID] [bigint] NOT NULL,
    [PARAMETER_NAME] [varchar](100) NOT NULL,
    [PARAMETER_TYPE] [varchar](100) NOT NULL,
    [PARAMETER_VALUE] [varchar](2500) NULL,
    [IDENTIFYING] [char](1) NOT NULL
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[BATCH_JOB_INSTANCE]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_JOB_INSTANCE](
    [JOB_INSTANCE_ID] [bigint] NOT NULL,
    [VERSION] [bigint] NULL,
    [JOB_NAME] [varchar](100) NOT NULL,
    [JOB_KEY] [varchar](32) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[JOB_INSTANCE_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    CONSTRAINT [JOB_INST_UN] UNIQUE NONCLUSTERED
(
    [JOB_NAME] ASC,
[JOB_KEY] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[BATCH_STEP_EXECUTION]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_STEP_EXECUTION](
    [STEP_EXECUTION_ID] [bigint] NOT NULL,
    [VERSION] [bigint] NOT NULL,
    [STEP_NAME] [varchar](100) NOT NULL,
    [JOB_EXECUTION_ID] [bigint] NOT NULL,
    [CREATE_TIME] [datetime] NOT NULL,
    [START_TIME] [datetime] NULL,
    [END_TIME] [datetime] NULL,
    [STATUS] [varchar](10) NULL,
    [COMMIT_COUNT] [bigint] NULL,
    [READ_COUNT] [bigint] NULL,
    [FILTER_COUNT] [bigint] NULL,
    [WRITE_COUNT] [bigint] NULL,
    [READ_SKIP_COUNT] [bigint] NULL,
    [WRITE_SKIP_COUNT] [bigint] NULL,
    [PROCESS_SKIP_COUNT] [bigint] NULL,
    [ROLLBACK_COUNT] [bigint] NULL,
    [EXIT_CODE] [varchar](2500) NULL,
    [EXIT_MESSAGE] [varchar](2500) NULL,
    [LAST_UPDATED] [datetime] NULL,
    PRIMARY KEY CLUSTERED
(
[STEP_EXECUTION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[BATCH_STEP_EXECUTION_CONTEXT]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[BATCH_STEP_EXECUTION_CONTEXT](
    [STEP_EXECUTION_ID] [bigint] NOT NULL,
    [SHORT_CONTEXT] [varchar](2500) NOT NULL,
    [SERIALIZED_CONTEXT] [varchar](max) NULL,
    PRIMARY KEY CLUSTERED
(
[STEP_EXECUTION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
/****** Object:  Table [dbo].[blacklist_token_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[blacklist_token_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [expiry] [datetime2](6) NOT NULL,
    [token] [varchar](255) NOT NULL,
    [username] [varchar](255) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[budget_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[budget_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [current_spending] [decimal](38, 2) NULL,
    [money_limit] [decimal](38, 2) NULL,
    [user_id] [bigint] NULL,
    [created_at] [datetime2](2) NULL,
    [updated_at] [datetime2](2) NULL,
    [category_id] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[category_list_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[category_list_table](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [category] [varchar](255) NULL,
    [created_at] [datetime2](6) NULL,
    [type] [varchar](255) NULL,
    [updated_at] [datetime2](6) NULL,
    [updated_by] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[contact_us_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[contact_us_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [completed_time] [datetime2](6) NULL,
    [email] [varchar](255) NULL,
    [is_request_active] [bit] NOT NULL,
    [is_verified] [bit] NOT NULL,
    [reference_number] [varchar](255) NULL,
    [request_reason] [varchar](255) NULL,
    [request_status] [varchar](255) NULL,
    [start_time] [datetime2](6) NULL,
    [image_id] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[contact_us_table_hist]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[contact_us_table_hist](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [contact_us_id] [bigint] NULL,
    [message] [varchar](255) NULL,
    [name] [varchar](255) NULL,
    [request_reason] [varchar](255) NULL,
    [request_status] [varchar](255) NULL,
    [updated_time] [datetime2](6) NULL,
    [updated_by] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[excel_template]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[excel_template](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [content] [varbinary](max) NULL,
    [content_type] [varchar](255) NULL,
    [created_by] [bigint] NULL,
    [created_time] [datetime2](6) NULL,
    [name] [varchar](255) NULL,
    [updated_by] [bigint] NULL,
    [updated_time] [datetime2](6) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
/****** Object:  Table [dbo].[expense_goal_relation_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[expense_goal_relation_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [goal_id] [bigint] NOT NULL,
    [expense_id] [bigint] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[expense_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[expense_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [amount] [decimal](38, 2) NULL,
    [date] [datetime] NULL,
    [description] [varchar](255) NULL,
    [is_deleted] [bit] NOT NULL,
    [recurring] [bit] NOT NULL,
    [user_id] [bigint] NULL,
    [created_at] [datetime2](7) NULL,
    [updated_at] [datetime2](7) NULL,
    [category_id] [int] NULL,
    [entry_mode] [varchar](255) NULL,
    [gmail_sync_date] [datetime2](6) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[gmail_processed_message]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[gmail_processed_message](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [is_verified] [bit] NULL,
    [message_id] [varchar](255) NOT NULL,
    [processed_at] [datetime2](6) NULL,
    [updated_at] [datetime2](6) NULL,
    [user_id] [bigint] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[gmail_sync_history]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[gmail_sync_history](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [gmail_processed_id] [bigint] NULL,
    [sync_time] [datetime2](6) NULL,
    [user_id] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[goal_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[goal_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [category_id] [int] NOT NULL,
    [created_at] [datetime2](6) NOT NULL,
    [current_amount] [numeric](38, 2) NOT NULL,
    [dead_line_date] [datetime2](6) NOT NULL,
    [deleted] [bit] NOT NULL,
    [description] [varchar](255) NULL,
    [goal_name] [varchar](255) NOT NULL,
    [recurring_amount] [numeric](38, 2) NOT NULL,
    [target_amount] [numeric](38, 2) NOT NULL,
    [updated_at] [datetime2](6) NOT NULL,
    [user_id] [bigint] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[income_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[income_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [amount] [decimal](38, 2) NULL,
    [date] [datetime] NULL,
    [is_deleted] [bit] NOT NULL,
    [recurring] [bit] NOT NULL,
    [source] [varchar](255) NULL,
    [user_id] [bigint] NULL,
    [created_at] [datetime2](2) NULL,
    [updated_at] [datetime2](2) NULL,
    [description] [varchar](255) NULL,
    [category_id] [int] NULL,
    [entry_mode] [varchar](255) NULL,
    [gmail_sync_date] [datetime2](6) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[income_table_deleted]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[income_table_deleted](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [expiry_date_time] [datetime2](6) NULL,
    [income_id] [bigint] NULL,
    [start_date_time] [datetime2](6) NULL,
    [deleted_at] [datetime2](2) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[otp_temp_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[otp_temp_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [email] [varchar](255) NULL,
    [expiration_time] [datetime2](6) NULL,
    [otp] [varchar](255) NULL,
    [otp_type] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[reason_code_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[reason_code_table](
    [id] [int] NOT NULL,
    [name] [varchar](100) NOT NULL
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[reason_type_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[reason_type_table](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [created_time] [datetime2](6) NULL,
    [is_deleted] [bit] NULL,
    [reason] [varchar](255) NULL,
    [reason_code] [int] NOT NULL,
    [updated_time] [datetime2](6) NULL,
    [created_by] [bigint] NULL,
    [updated_by] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[schedule_notification_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[schedule_notification_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [created_date] [datetime2](6) NULL,
    [description] [varchar](255) NULL,
    [is_active] [bit] NOT NULL,
    [is_cancelled] [bit] NOT NULL,
    [recipients] [varchar](255) NULL,
    [schedule_from] [datetime2](6) NULL,
    [schedule_to] [datetime2](6) NULL,
    [subject] [varchar](255) NULL,
    [updated_at] [datetime2](6) NULL,
    [schedule_by] [bigint] NULL,
    [notification_type] [varchar](255) NULL,
    [updated_by] [bigint] NULL,
    [parent_key] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[session_token_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[session_token_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [created_time] [datetime2](6) NULL,
    [expire_time] [datetime2](6) NULL,
    [is_active] [bit] NULL,
    [token] [varchar](255) NULL,
    [username] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[user_auth_hist_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_auth_hist_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [comment] [varchar](255) NULL,
    [reason_type_id] [int] NOT NULL,
    [updated_by] [bigint] NULL,
    [updated_time] [datetime2](6) NULL,
    [user_id] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[user_auth_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_auth_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [otp_count] [int] NOT NULL,
    [password] [varchar](255) NULL,
    [username] [varchar](255) NULL,
    [verification_code] [varchar](255) NULL,
    [verification_code_expiration] [datetime2](6) NULL,
    [is_deleted] [bit] NULL,
    [is_blocked] [bit] NULL,
    [role_id] [int] NULL,
    [login_code_value] [int] NULL,
    [last_reset] [datetime2](6) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    CONSTRAINT [uk_user_auth_table] UNIQUE NONCLUSTERED
(
[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[user_gmail_auth]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_gmail_auth](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [access_token] [text] NULL,
    [created_at] [datetimeoffset](6) NULL,
    [expires_at] [datetimeoffset](6) NOT NULL,
    [refresh_token] [text] NULL,
    [user_id] [bigint] NOT NULL,
    [count] [int] NULL,
    [is_active] [bit] NULL,
    [sync_reset_at] [datetime2](6) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    CONSTRAINT [UKhbgwxxw7bhyl6cccdbtca9a31] UNIQUE NONCLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
/****** Object:  Table [dbo].[user_notification_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_notification_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [is_read] [bit] NOT NULL,
    [schedule_id] [bigint] NULL,
    [username] [varchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
/****** Object:  Table [dbo].[user_profile_details_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_profile_details_table](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [address] [varchar](255) NULL,
    [date_of_birth] [date] NULL,
    [gender] [varchar](255) NULL,
    [income_range] [float] NOT NULL,
    [marital_status] [varchar](255) NULL,
    [name] [varchar](255) NULL,
    [phone] [varchar](255) NULL,
    [user_id] [bigint] NULL,
    [created_date] [datetime] NULL,
    [profile_image] [text] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    CONSTRAINT [uk_user_profile_details_table] UNIQUE NONCLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
/****** Object:  Table [dbo].[user_role_table]    Script Date: 13-04-2026 23:29:47 ******/
CREATE TABLE [dbo].[user_role_table](
    [role_id] [int] NOT NULL,
    [role_name] [varchar](50) NOT NULL,
    CONSTRAINT [PK_user_role_table] PRIMARY KEY CLUSTERED
(
[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    CONSTRAINT [UQ_user_role_table_role_name] UNIQUE NONCLUSTERED
(
[role_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION] ADD  DEFAULT (NULL) FOR [START_TIME]
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION] ADD  DEFAULT (NULL) FOR [END_TIME]
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION] ADD  DEFAULT (NULL) FOR [START_TIME]
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION] ADD  DEFAULT (NULL) FOR [END_TIME]
ALTER TABLE [dbo].[user_auth_table] ADD  DEFAULT (getdate()) FOR [last_reset]
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION]  WITH CHECK ADD  CONSTRAINT [JOB_INST_EXEC_FK] FOREIGN KEY([JOB_INSTANCE_ID])
    REFERENCES [dbo].[BATCH_JOB_INSTANCE] ([JOB_INSTANCE_ID])
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION] CHECK CONSTRAINT [JOB_INST_EXEC_FK]
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION_CONTEXT]  WITH CHECK ADD  CONSTRAINT [JOB_EXEC_CTX_FK] FOREIGN KEY([JOB_EXECUTION_ID])
    REFERENCES [dbo].[BATCH_JOB_EXECUTION] ([JOB_EXECUTION_ID])
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION_CONTEXT] CHECK CONSTRAINT [JOB_EXEC_CTX_FK]
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION_PARAMS]  WITH CHECK ADD  CONSTRAINT [JOB_EXEC_PARAMS_FK] FOREIGN KEY([JOB_EXECUTION_ID])
    REFERENCES [dbo].[BATCH_JOB_EXECUTION] ([JOB_EXECUTION_ID])
ALTER TABLE [dbo].[BATCH_JOB_EXECUTION_PARAMS] CHECK CONSTRAINT [JOB_EXEC_PARAMS_FK]
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION]  WITH CHECK ADD  CONSTRAINT [JOB_EXEC_STEP_FK] FOREIGN KEY([JOB_EXECUTION_ID])
    REFERENCES [dbo].[BATCH_JOB_EXECUTION] ([JOB_EXECUTION_ID])
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION] CHECK CONSTRAINT [JOB_EXEC_STEP_FK]
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION_CONTEXT]  WITH CHECK ADD  CONSTRAINT [STEP_EXEC_CTX_FK] FOREIGN KEY([STEP_EXECUTION_ID])
    REFERENCES [dbo].[BATCH_STEP_EXECUTION] ([STEP_EXECUTION_ID])
ALTER TABLE [dbo].[BATCH_STEP_EXECUTION_CONTEXT] CHECK CONSTRAINT [STEP_EXEC_CTX_FK]
ALTER TABLE [dbo].[blacklist_token_table]  WITH CHECK ADD  CONSTRAINT [FK_blacklist_username_relationship] FOREIGN KEY([username])
    REFERENCES [dbo].[user_auth_table] ([username])
ALTER TABLE [dbo].[blacklist_token_table] CHECK CONSTRAINT [FK_blacklist_username_relationship]
ALTER TABLE [dbo].[budget_table]  WITH CHECK ADD  CONSTRAINT [fk_budget_category_id_category_list_id] FOREIGN KEY([category_id])
    REFERENCES [dbo].[category_list_table] ([id])
ALTER TABLE [dbo].[budget_table] CHECK CONSTRAINT [fk_budget_category_id_category_list_id]
ALTER TABLE [dbo].[budget_table]  WITH CHECK ADD  CONSTRAINT [FK_budget_user] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[budget_table] CHECK CONSTRAINT [FK_budget_user]
ALTER TABLE [dbo].[category_list_table]  WITH CHECK ADD  CONSTRAINT [fk_category_list_user_id] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[category_list_table] CHECK CONSTRAINT [fk_category_list_user_id]
ALTER TABLE [dbo].[contact_us_table]  WITH CHECK ADD  CONSTRAINT [FK_contact_us_user] FOREIGN KEY([email])
    REFERENCES [dbo].[user_auth_table] ([username])
ALTER TABLE [dbo].[contact_us_table] CHECK CONSTRAINT [FK_contact_us_user]
ALTER TABLE [dbo].[contact_us_table_hist]  WITH CHECK ADD  CONSTRAINT [FK_contact_us_hist_contact_us] FOREIGN KEY([contact_us_id])
    REFERENCES [dbo].[contact_us_table] ([id])
ALTER TABLE [dbo].[contact_us_table_hist] CHECK CONSTRAINT [FK_contact_us_hist_contact_us]
ALTER TABLE [dbo].[contact_us_table_hist]  WITH CHECK ADD  CONSTRAINT [FK_contact_us_hist_user_id] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[contact_us_table_hist] CHECK CONSTRAINT [FK_contact_us_hist_user_id]
ALTER TABLE [dbo].[excel_template]  WITH CHECK ADD  CONSTRAINT [FK_template_created_user_id] FOREIGN KEY([created_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[excel_template] CHECK CONSTRAINT [FK_template_created_user_id]
ALTER TABLE [dbo].[excel_template]  WITH CHECK ADD  CONSTRAINT [FK_template_updated_user_id] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[excel_template] CHECK CONSTRAINT [FK_template_updated_user_id]
ALTER TABLE [dbo].[expense_goal_relation_table]  WITH CHECK ADD  CONSTRAINT [FKdx6setivqpf792x00p7tgu4pc] FOREIGN KEY([expense_id])
    REFERENCES [dbo].[expense_table] ([id])
ALTER TABLE [dbo].[expense_goal_relation_table] CHECK CONSTRAINT [FKdx6setivqpf792x00p7tgu4pc]
ALTER TABLE [dbo].[expense_table]  WITH CHECK ADD  CONSTRAINT [fk_expense_category_id_category_list_id] FOREIGN KEY([category_id])
    REFERENCES [dbo].[category_list_table] ([id])
ALTER TABLE [dbo].[expense_table] CHECK CONSTRAINT [fk_expense_category_id_category_list_id]
ALTER TABLE [dbo].[expense_table]  WITH CHECK ADD  CONSTRAINT [FK_expense_user] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[expense_table] CHECK CONSTRAINT [FK_expense_user]
ALTER TABLE [dbo].[gmail_processed_message]  WITH CHECK ADD  CONSTRAINT [FK_gmail_processed_user_id] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[gmail_processed_message] CHECK CONSTRAINT [FK_gmail_processed_user_id]
ALTER TABLE [dbo].[gmail_sync_history]  WITH CHECK ADD  CONSTRAINT [FK_gmail_sync_history_user_id] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[gmail_sync_history] CHECK CONSTRAINT [FK_gmail_sync_history_user_id]
ALTER TABLE [dbo].[income_table]  WITH CHECK ADD  CONSTRAINT [fk_income_category_id_category_list_id] FOREIGN KEY([category_id])
    REFERENCES [dbo].[category_list_table] ([id])
ALTER TABLE [dbo].[income_table] CHECK CONSTRAINT [fk_income_category_id_category_list_id]
ALTER TABLE [dbo].[income_table]  WITH CHECK ADD  CONSTRAINT [FK_income_user] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[income_table] CHECK CONSTRAINT [FK_income_user]
ALTER TABLE [dbo].[income_table_deleted]  WITH CHECK ADD  CONSTRAINT [FK_income_deleted_income] FOREIGN KEY([income_id])
    REFERENCES [dbo].[income_table] ([id])
ALTER TABLE [dbo].[income_table_deleted] CHECK CONSTRAINT [FK_income_deleted_income]
ALTER TABLE [dbo].[reason_type_table]  WITH CHECK ADD  CONSTRAINT [FK_created_by_user_id] FOREIGN KEY([created_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[reason_type_table] CHECK CONSTRAINT [FK_created_by_user_id]
ALTER TABLE [dbo].[reason_type_table]  WITH CHECK ADD  CONSTRAINT [FK_reason_type_updated_by_user_id] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[reason_type_table] CHECK CONSTRAINT [FK_reason_type_updated_by_user_id]
ALTER TABLE [dbo].[schedule_notification_table]  WITH CHECK ADD  CONSTRAINT [FK_schedule_by_user_id] FOREIGN KEY([schedule_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[schedule_notification_table] CHECK CONSTRAINT [FK_schedule_by_user_id]
ALTER TABLE [dbo].[schedule_notification_table]  WITH CHECK ADD  CONSTRAINT [FK_updated_by_user_id] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[schedule_notification_table] CHECK CONSTRAINT [FK_updated_by_user_id]
ALTER TABLE [dbo].[session_token_table]  WITH CHECK ADD  CONSTRAINT [FK_session_token_user] FOREIGN KEY([username])
    REFERENCES [dbo].[user_auth_table] ([username])
ALTER TABLE [dbo].[session_token_table] CHECK CONSTRAINT [FK_session_token_user]
ALTER TABLE [dbo].[user_auth_hist_table]  WITH CHECK ADD  CONSTRAINT [FK_user_hist_updated_by_user] FOREIGN KEY([updated_by])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[user_auth_hist_table] CHECK CONSTRAINT [FK_user_hist_updated_by_user]
ALTER TABLE [dbo].[user_auth_hist_table]  WITH CHECK ADD  CONSTRAINT [FK_user_hist_user] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[user_auth_hist_table] CHECK CONSTRAINT [FK_user_hist_user]
ALTER TABLE [dbo].[user_gmail_auth]  WITH CHECK ADD  CONSTRAINT [fk_gmail_auth_user_id] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[user_gmail_auth] CHECK CONSTRAINT [fk_gmail_auth_user_id]
ALTER TABLE [dbo].[user_notification_table]  WITH CHECK ADD  CONSTRAINT [FK_notification_user] FOREIGN KEY([username])
    REFERENCES [dbo].[user_auth_table] ([username])
ALTER TABLE [dbo].[user_notification_table] CHECK CONSTRAINT [FK_notification_user]
ALTER TABLE [dbo].[user_notification_table]  WITH CHECK ADD  CONSTRAINT [FK_user_notification_schedule_id] FOREIGN KEY([schedule_id])
    REFERENCES [dbo].[schedule_notification_table] ([id])
ALTER TABLE [dbo].[user_notification_table] CHECK CONSTRAINT [FK_user_notification_schedule_id]
ALTER TABLE [dbo].[user_profile_details_table]  WITH CHECK ADD  CONSTRAINT [FK_user_profile_details] FOREIGN KEY([user_id])
    REFERENCES [dbo].[user_auth_table] ([id])
ALTER TABLE [dbo].[user_profile_details_table] CHECK CONSTRAINT [FK_user_profile_details]