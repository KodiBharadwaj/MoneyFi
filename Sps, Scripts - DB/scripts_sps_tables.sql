USE [master]
GO
/****** Object:  Database [moneyfi_db]    Script Date: 22-11-2025 22:24:59 ******/
CREATE DATABASE [moneyfi_db]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'moneyfi_db', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\moneyfi_db.mdf' , SIZE = 73728KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'moneyfi_db_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\moneyfi_db_log.ldf' , SIZE = 401408KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [moneyfi_db] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [moneyfi_db].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [moneyfi_db] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [moneyfi_db] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [moneyfi_db] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [moneyfi_db] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [moneyfi_db] SET ARITHABORT OFF 
GO
ALTER DATABASE [moneyfi_db] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [moneyfi_db] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [moneyfi_db] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [moneyfi_db] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [moneyfi_db] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [moneyfi_db] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [moneyfi_db] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [moneyfi_db] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [moneyfi_db] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [moneyfi_db] SET  DISABLE_BROKER 
GO
ALTER DATABASE [moneyfi_db] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [moneyfi_db] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [moneyfi_db] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [moneyfi_db] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [moneyfi_db] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [moneyfi_db] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [moneyfi_db] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [moneyfi_db] SET RECOVERY FULL 
GO
ALTER DATABASE [moneyfi_db] SET  MULTI_USER 
GO
ALTER DATABASE [moneyfi_db] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [moneyfi_db] SET DB_CHAINING OFF 
GO
ALTER DATABASE [moneyfi_db] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [moneyfi_db] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [moneyfi_db] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [moneyfi_db] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'moneyfi_db', N'ON'
GO
ALTER DATABASE [moneyfi_db] SET QUERY_STORE = ON
GO
ALTER DATABASE [moneyfi_db] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [moneyfi_db]
GO
/****** Object:  User [santhosh]    Script Date: 22-11-2025 22:24:59 ******/
CREATE USER [santhosh] WITHOUT LOGIN WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_datareader] ADD MEMBER [santhosh]
GO
/****** Object:  Table [dbo].[blacklist_token_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[blacklist_token_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[expiry] [datetime2](6) NOT NULL,
	[token] [varchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[budget_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[budget_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[category] [varchar](255) NULL,
	[current_spending] [decimal](38, 2) NULL,
	[money_limit] [decimal](38, 2) NULL,
	[user_id] [bigint] NULL,
	[created_at] [datetime2](2) NULL,
	[updated_at] [datetime2](2) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[contact_us_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[contact_us_table_hist]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[contact_us_table_hist](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[contact_us_id] [bigint] NULL,
	[message] [varchar](255) NULL,
	[name] [varchar](255) NULL,
	[request_reason] [varchar](255) NULL,
	[request_status] [varchar](255) NULL,
	[updated_time] [datetime2](6) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[excel_template]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[excel_template](
	[id] [int] NOT NULL,
	[name] [varchar](255) NULL,
	[content] [varbinary](max) NULL,
	[content_type] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[expense_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[expense_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[amount] [decimal](38, 2) NULL,
	[category] [varchar](255) NULL,
	[date] [datetime] NULL,
	[description] [varchar](255) NULL,
	[is_deleted] [bit] NOT NULL,
	[recurring] [bit] NOT NULL,
	[user_id] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[goal_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[goal_table](
	[current_amount] [decimal](38, 2) NULL,
	[dead_line] [datetime2](6) NULL,
	[is_deleted] [bit] NOT NULL,
	[target_amount] [decimal](38, 2) NULL,
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NULL,
	[category] [varchar](255) NULL,
	[expense_ids] [varchar](255) NULL,
	[goal_name] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[income_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[income_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[amount] [decimal](38, 2) NULL,
	[category] [varchar](255) NULL,
	[date] [datetime] NULL,
	[is_deleted] [bit] NOT NULL,
	[recurring] [bit] NOT NULL,
	[source] [varchar](255) NULL,
	[user_id] [bigint] NULL,
	[created_at] [datetime2](2) NULL,
	[updated_at] [datetime2](2) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[income_table_deleted]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[otp_temp_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[reason_code_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[reason_code_table](
	[id] [int] NOT NULL,
	[name] [varchar](100) NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[reason_type_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[reason_type_table](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[created_time] [datetime2](6) NULL,
	[is_deleted] [bit] NULL,
	[reason] [varchar](255) NULL,
	[reason_code] [int] NOT NULL,
	[updated_time] [datetime2](6) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[schedule_notification_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[session_token_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[user_auth_hist_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[user_auth_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [uk_user_auth_table] UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[user_notification_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[user_profile_details_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[user_role_table]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[getAccountStatementOfUser]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

create   procedure [dbo].[getAccountStatementOfUser] (
	@userId BIGINT,
	@startDate DATE,
	@endDate DATE,
	@offset INT,
	@limit INT
	)
AS
BEGIN
	
	SET NOCOUNT ON;
	
	IF @limit <= 0
	BEGIN

		SELECT it.date AS transactionDate
			,CONVERT(VARCHAR(5), it.date, 108) AS transactionTime
			,CONCAT(it.source, ' (', it.category, ')') AS description
			,it.amount AS amount
			,'credit' AS creditOrDebit
		FROM income_table it WITH (NOLOCK)
		WHERE it.user_id = @userId
			AND it.is_deleted = 0
			AND CAST(it.date AS DATE) BETWEEN @startDate AND @endDate

		UNION ALL

		SELECT et.date AS transactionDate
			,CONVERT(VARCHAR(5), et.date, 108) AS transactionTime
			,CONCAT(et.description, ' (', et.category, ')') AS description
			,et.amount AS amount
			,'debit' AS creditOrDebit
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId
			AND et.is_deleted = 0
			AND CAST(et.date AS DATE) BETWEEN @startDate AND @endDate

		ORDER BY transactionDate,
		creditOrDebit
		END

	ELSE
	BEGIN
		SELECT it.date AS transactionDate
			,CONVERT(VARCHAR(5), it.date, 108) AS transactionTime
			,CONCAT(it.source, ' (', it.category, ')') AS description
			,it.amount AS amount
			,'credit' AS creditOrDebit
		FROM income_table it WITH (NOLOCK)
		WHERE it.user_id = @userId
			AND it.is_deleted = 0
			AND CAST(it.date AS DATE) BETWEEN @startDate AND @endDate

		UNION ALL

		SELECT et.date AS transactionDate
			,CONVERT(VARCHAR(5), et.date, 108) AS transactionTime
			,CONCAT(et.description, ' (', et.category, ')') as description
			,et.amount AS amount
			,'debit' AS creditOrDebit
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId
			AND et.is_deleted = 0
			AND CAST(et.date AS DATE) BETWEEN @startDate AND @endDate

		ORDER BY transactionDate,
		creditOrDebit

		offset @offset rows
		FETCH NEXT @limit rows ONLY;
		END
END


GO
/****** Object:  StoredProcedure [dbo].[getAdminOverviewPageDetails]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getAdminOverviewPageDetails]

AS
BEGIN
	
	SET NOCOUNT ON;

	DROP TABLE IF EXISTS #userCountTempTable
	DROP TABLE IF EXISTS #requestCountTempTable

	CREATE TABLE #userCountTempTable (
		activeUsers BIGINT,
		blockedUsers BIGINT,
		deletedUsers BIGINT
	)

	CREATE TABLE #requestCountTempTable (
		accountUnblockRequests BIGINT,
		nameChangeRequests BIGINT,
		accountReactivateRequests BIGINT,
		userDefectRaises BIGINT,
		userFeedbacks BIGINT
	)

	INSERT INTO #userCountTempTable
    SELECT 
        CAST(COUNT(CASE 
					WHEN uat.is_blocked = 0 AND uat.is_deleted = 0 
						THEN 1 
					END) AS BIGINT) AS activeUsers,
        CAST(COUNT(CASE 
					WHEN uat.is_blocked = 1 AND uat.is_deleted = 0 
						THEN 1 
					END) AS BIGINT) AS blockedUsers,
        CAST(COUNT(CASE 
					WHEN uat.is_deleted = 1 
						THEN 1 
					END) AS BIGINT) AS deletedUsers
	FROM user_auth_table uat WITH (NOLOCK) 
	INNER JOIN user_role_table urt WITH (NOLOCK) ON urt.role_id = uat.role_id
	WHERE urt.role_name IN ('USER')


	INSERT INTO #requestCountTempTable
	SELECT 
		COUNT(CASE WHEN cut.request_reason = 'ACCOUNT_UNBLOCK_REQUEST' THEN 1 END) AS accountUnblockRequests,
		COUNT(CASE WHEN cut.request_reason = 'NAME_CHANGE_REQUEST' THEN 1 END) AS nameChangeRequests,
		COUNT(CASE WHEN cut.request_reason = 'ACCOUNT_NOT_DELETE_REQUEST' THEN 1 END) AS accountReactivateRequests,
		COUNT(CASE WHEN cut.request_reason = 'USER_DEFECT_UPDATE' THEN 1 END) AS userDefectRaises,
		COUNT(CASE WHEN cut.request_reason = 'USER_FEEDBACK_UPDATE' THEN 1 END) AS userFeedbacks
	FROM contact_us_table cut WITH (NOLOCK)
	WHERE cut.is_request_active = 1
		AND cut.request_status = 'SUBMITTED'
		AND cut.is_verified = 0


	SELECT 
		u.activeUsers,
		u.blockedUsers,
		u.deletedUsers,
		r.accountUnblockRequests,
		r.nameChangeRequests,
		r.accountReactivateRequests,
		r.userDefectRaises,
		r.userFeedbacks
	FROM #userCountTempTable u
	CROSS JOIN #requestCountTempTable r;

END
GO
/****** Object:  StoredProcedure [dbo].[getAllActiveSchedulesOfAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getAllActiveSchedulesOfAdmin]

AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT snt.id AS scheduleId
		,snt.subject AS subject
		,snt.description AS description
		,snt.is_cancelled AS isCancelled
		,snt.created_date AS createdDate
		,snt.schedule_from AS scheduleFrom
		,snt.schedule_to AS scheduleTo
	FROM schedule_notification_table snt WITH (NOLOCK) 
	WHERE snt.is_active = 1
		AND CAST(snt.schedule_to AS DATE) > CAST(GETDATE() AS DATE)
END
GO
/****** Object:  StoredProcedure [dbo].[getAllBudgetsByUserId]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[getAllBudgetsByUserId] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT id
		,category
		,currentSpending
		,moneyLimit
		,CASE 
			WHEN moneyLimit = 0 THEN 0
			ELSE CAST((currentSpending * 100.0 / moneyLimit) AS BIGINT)
		 END AS progressPercentage
		 ,createdAt
		 ,updatedAt
	FROM (
		SELECT bt.id AS id
			,bt.category AS category
			,SUM(ISNULL(et.amount, 0)) AS currentSpending
			,bt.money_limit AS moneyLimit
			,bt.created_at AS createdAt
			,bt.updated_at AS updatedAt
		FROM budget_table bt WITH (NOLOCK)
		LEFT JOIN expense_table et WITH (NOLOCK) ON et.user_id = bt.user_id 
			AND MONTH(et.date) = @month
			AND YEAR(et.date) = @year
			AND et.is_deleted = 0
			AND bt.category = et.category
		WHERE bt.user_id = @userId
		GROUP BY 
        bt.id, bt.category, bt.money_limit, bt.created_at, bt.updated_at
				) AS tempTable

		ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllBudgetsByUserIdAndByCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[getAllBudgetsByUserIdAndByCategory] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT id
		,category
		,currentSpending
		,moneyLimit
		,CASE 
			WHEN moneyLimit = 0 THEN 0
			ELSE CAST((currentSpending * 100.0 / moneyLimit) AS BIGINT)
		 END AS progressPercentage
		 ,createdAt
		 ,updatedAt
	FROM (
		SELECT bt.id as id
			,bt.category as category
			,SUM(ISNULL(et.amount, 0)) AS currentSpending
			,bt.money_limit as moneyLimit
			,bt.created_at AS createdAt
			,bt.updated_at AS updatedAt
		FROM budget_table bt WITH (NOLOCK)
		LEFT JOIN expense_table et WITH (NOLOCK) ON et.user_id = bt.user_id
			AND et.category = bt.category
			AND MONTH(et.date) = @month
			AND YEAR(et.date) = @year
			AND et.is_deleted = 0
		WHERE bt.user_id = @userId
			AND bt.category = @category

		GROUP BY bt.id, bt.category, bt.money_limit, bt.created_at, bt.updated_at
				) AS tempTable

		ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByMonthAndYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[getAllExpensesByMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id AS id
		,et.category AS category
		,et.amount AS amount
		,et.date AS date
		,et.recurring AS recurring
		,et.description AS description
		,et.is_deleted AS isDeleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND et.is_deleted = @deleteStatus
		AND MONTH(et.date) = @month
		AND YEAR(et.date) = @year
	ORDER BY et.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByMonthAndYearAndByCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllExpensesByMonthAndYearAndByCategory] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id AS id
		,et.category AS category
		,et.amount AS amount
		,et.date AS date
		,et.recurring AS recurring
		,et.description AS description
		,et.is_deleted AS isDeleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND et.is_deleted = @deleteStatus
		AND et.category = @category
		AND MONTH(et.date) = @month
		AND YEAR(et.date) = @year
	ORDER BY et.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllExpensesByYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id AS id
		,et.category AS category
		,et.amount AS amount
		,et.date AS date
		,et.recurring AS recurring
		,et.description AS description
		,et.is_deleted AS isDeleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND et.is_deleted = @deleteStatus
		AND YEAR(et.date) = @year
	ORDER BY et.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByYearAndByCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllExpensesByYearAndByCategory] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id AS id
		,et.category AS category
		,et.amount AS amount
		,et.date AS date
		,et.recurring AS recurring
		,et.description AS description
		,et.is_deleted AS isDeleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND et.is_deleted = @deleteStatus
		AND et.category = @category
		AND YEAR(et.date) = @year
	ORDER BY et.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllGoalsByUserId]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getAllGoalsByUserId] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;
	
	DECLARE @today DATE = GETDATE();
	
	SELECT id
		,goalName
		,currentAmount
		,targetAmount
		,deadLine
		,category
		,isDeleted
		,daysRemaining
		,progressPercentage
		,CASE
			WHEN progressPercentage >= 100 AND daysRemaining > 0 
				THEN 'completed-early'
			WHEN progressPercentage >= 100 AND daysRemaining <=0
				THEN 'completed-on-time'
			WHEN daysRemaining <= 0
				THEN 'overdue'
			ELSE 'in-progress' 
			END AS goalStatus
		FROM (

			SELECT gt.id AS id
				,gt.goal_name AS goalName
				,gt.current_amount AS currentAmount
				,gt.target_amount AS targetAmount
				,gt.dead_line AS deadLine
				,gt.category AS category
				,gt.is_deleted AS isDeleted
				,CAST(DATEDIFF(DAY, @today, CAST(gt.dead_line AS DATE)) AS BIGINT) AS daysRemaining
				,CAST(((gt.current_amount / gt.target_amount) * 100) AS BIGINT) AS progressPercentage
			FROM goal_table gt WITH (NOLOCK)
			WHERE gt.user_id = @userId
				AND gt.is_deleted = 0

				) AS tempTable

		
	ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByMonthAndYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllIncomesByMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id AS id
		,it.amount AS amount
		,it.source AS source
		,it.date AS date
		,it.category AS category
		,it.recurring AS recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND it.is_deleted = @deleteStatus
		AND MONTH(it.date) = @month
		AND YEAR(it.date) = @year
	ORDER BY it.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByMonthAndYearAndByCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllIncomesByMonthAndYearAndByCategory] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@category VARCHAR(100),
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id AS id
		,it.amount AS amount
		,it.source AS source
		,it.date AS date
		,it.category AS category
		,it.recurring AS recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND it.is_deleted = @deleteStatus
		AND it.category = @category
		AND MONTH(it.date) = @month
		AND YEAR(it.date) = @year
	ORDER BY it.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllIncomesByYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id AS id
		,it.amount AS amount
		,it.source AS source
		,it.date AS date
		,it.category AS category
		,it.recurring AS recurring
	FROM income_table it WITH (NOLOCK) 
	WHERE it.user_id = @userId 
		AND it.is_deleted = @deleteStatus
		AND YEAR(it.date) = @year
	ORDER BY it.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByYearAndByCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getAllIncomesByYearAndByCategory] (
	@userId BIGINT,
	@year INT,
	@category VARCHAR(100),
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id AS id
		,it.amount AS amount
		,it.source AS source
		,it.date AS date
		,it.category AS category
		,it.recurring AS recurring
	FROM income_table it WITH (NOLOCK) 
	WHERE it.user_id = @userId 
		AND it.is_deleted = @deleteStatus
		AND it.category = @category
		AND YEAR(it.date) = @year
	ORDER BY it.date ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getAvailableBalanceOfUser]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getAvailableBalanceOfUser] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;
	
	DECLARE @totalIncome NUMERIC(38,2) = ( 
		SELECT ISNULL(SUM(it.amount), 0) 
		FROM income_table it WITH (NOLOCK)
		WHERE it.user_id = @userId 
			AND it.is_deleted = 0
	);

    DECLARE @totalExpense NUMERIC(38,2) = (
		SELECT ISNULL(SUM(et.amount), 0) 
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId 
			AND et.is_deleted = 0
	);

    DECLARE @remaining NUMERIC(38,2) = @totalIncome - @totalExpense;

    SELECT @remaining AS RemainingAmount;

END
GO
/****** Object:  StoredProcedure [dbo].[getBirthdayOrAnniversaryUserEmailAndName]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[getBirthdayOrAnniversaryUserEmailAndName] (
	@month INT,
	@day INT,
	@occasion VARCHAR(50)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT CONCAT(
        uat.username, 
        '-', 
        updt.name, 
        '-', 
        YEAR(
            CASE 
                WHEN @occasion = 'Anniversary' THEN updt.created_date
                WHEN @occasion = 'Birthday' THEN updt.date_of_birth
            END
        )
    )
	FROM user_profile_details_table updt WITH (NOLOCK)
	INNER JOIN user_auth_table uat WITH (NOLOCK) ON uat.id = updt.user_id
		AND uat.is_blocked = 0
		AND uat.is_deleted = 0
	INNER JOIN user_role_table urt WITH (NOLOCK) ON urt.role_id = uat.role_id
		AND urt.role_name IN ('USER')
	WHERE (
		@occasion = 'Anniversary' AND MONTH(updt.created_date) = @month AND DAY(updt.created_date) = @day
		OR
		@occasion = 'Birthday' AND MONTH(updt.date_of_birth) = @month AND DAY(updt.date_of_birth) = @day
	)
	ORDER BY updt.created_date
END
GO
/****** Object:  StoredProcedure [dbo].[getBlackListTokenDetailsByToken]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getBlackListTokenDetailsByToken] (
	@token VARCHAR(1000)
	)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    SELECT btt.* 
	FROM blacklist_token_table btt WITH (NOLOCK)
	WHERE btt.token = @token;
END
GO
/****** Object:  StoredProcedure [dbo].[getCompleteUserDetailsForAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[getCompleteUserDetailsForAdmin] (
	@username VARCHAR(50)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT updt.name AS name
		,uat.username AS username
		,updt.phone AS phoneNumber
		,updt.created_date AS createdTime
		,updt.gender AS gender
		,updt.marital_status AS maritalStatus
		,updt.date_of_birth AS dateOfBirth
		,updt.address AS address
		,uat.id as userId
		,uat.login_code_value as loginCodeValue
	FROM user_auth_table uat WITH (NOLOCK)
	INNER JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	WHERE uat.username = @username

END
GO
/****** Object:  StoredProcedure [dbo].[getDeletedIncomesInAMonth]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getDeletedIncomesInAMonth] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT
	it.id,
	it.amount,
	it.source,
	it.date,
	it.category,
	it.recurring,
	CASE 
		WHEN GETDATE() < itd.expiry_date_time
			THEN DATEDIFF(DAY, GETDATE(), itd.expiry_date_time)
		ELSE 0
	END AS daysRemained
	FROM income_table it WITH (NOLOCK)
	JOIN income_table_deleted itd WITH (NOLOCK) ON it.id = itd.income_id
		AND it.user_id = @userId
		AND it.is_deleted = 1
	WHERE MONTH(it.date) = @month
		AND YEAR(it.date) = @year
END
GO
/****** Object:  StoredProcedure [dbo].[getIncomeBySourceAndCategory]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getIncomeBySourceAndCategory] (
	@userId BIGINT,
	@source VARCHAR(50),
	@category VARCHAR(50),
	@date DATETIME
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT it.* 
	FROM income_table it WITH (NOLOCK) 
	WHERE it.user_id = @userId
		AND it.is_deleted = 0
		AND it.source = @source
		AND it.category = @category
		AND MONTH(it.date) = MONTH(@date)
		AND YEAR(it.date) = YEAR(@date);
END
GO
/****** Object:  StoredProcedure [dbo].[getMonthlyExpensesListInAYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getMonthlyExpensesListInAYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)

AS
BEGIN

	SET NOCOUNT ON;

    SELECT MONTH(et.date) as month
		,SUM(et.amount)
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND YEAR(et.date) = @year
		AND et.is_deleted = @deleteStatus
	GROUP BY MONTH(et.date)
	ORDER BY month ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getMonthlyIncomesListInAYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getMonthlyIncomesListInAYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)

AS
BEGIN

	SET NOCOUNT ON;

    SELECT MONTH(it.date) as month
		,SUM(it.amount)
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND YEAR(it.date) = @year
		AND it.is_deleted = @deleteStatus
	GROUP BY MONTH(it.date)
	ORDER BY month ASC;
END
GO
/****** Object:  StoredProcedure [dbo].[getOverviewPageDetails]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================

create   procedure [dbo].[getOverviewPageDetails] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN
	
	SET NOCOUNT ON;

	DROP TABLE IF EXISTS #tmpResult;

	DECLARE @availableBalance NUMERIC(38,2)

	-- Call SP (getAvailableBalanceOfUser) to get the available balance
	CREATE TABLE #tempTable (RemainingAmount NUMERIC(38,2));
	INSERT INTO #tempTable
	EXEC [dbo].[getAvailableBalanceOfUser] @userId

	SELECT @availableBalance = RemainingAmount FROM #tempTable;

	DECLARE @totalExpenseInMonth NUMERIC(38,2) = (
		SELECT ISNULL(SUM(amount), 0) 
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId 
			AND et.is_deleted = 0
			AND MONTH(et.date) = @month
			AND YEAR(et.date) = @year
	);

	DECLARE @totalBudget NUMERIC(38,2) = (
		SELECT ISNULL(SUM(bt.money_limit), 0) 
		FROM budget_table bt WITH (NOLOCK)
		WHERE bt.user_id = @userId 
	);

	DECLARE @toalGoalIncome NUMERIC(38,2) = (
		SELECT ISNULL(SUM(gt.current_amount), 0) 
		FROM goal_table gt WITH (NOLOCK)
		WHERE gt.user_id = @userId 
			AND gt.is_deleted = 0
	);

	DECLARE @goalProgress NUMERIC(38,2) = (
		SELECT CASE 
			WHEN targetAmount = 0
				THEN 0
			ELSE  (currentAmount/targetAmount) * 100 
			END
		FROM (
				SELECT ISNULL(SUM(gt.current_amount),0) as currentAmount
					,ISNULL(SUM(gt.target_amount),0) as targetAmount
				FROM goal_table gt WITH (NOLOCK) 
				WHERE gt.user_id = @userId
					AND gt.is_deleted = 0
			) as temp
	);

    SELECT @availableBalance AS availableBalance
		,@totalExpenseInMonth AS totalExpense 
		,@totalBudget AS totalBudget
		,CASE 
			WHEN @totalBudget = 0
				THEN 0
			ELSE (@totalExpenseInMonth/@totalBudget) * 100 
			END AS budgetProgress
		,@toalGoalIncome AS totalGoalIncome
		,@goalProgress AS goalProgress

END


GO
/****** Object:  StoredProcedure [dbo].[getProfileDetailsOfUser]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[getProfileDetailsOfUser] (
	@username VARCHAR(100)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT updt.name AS name
		,uat.username AS email
		,updt.phone AS phone
		,updt.gender AS gender
		,updt.marital_status AS maritalStatus
		,updt.date_of_birth AS dateOfBirth
		,updt.address AS address
		,updt.income_range AS incomeRange
		,CAST(updt.created_date AS DATE) AS createdDate
	FROM user_profile_details_table updt WITH (NOLOCK)
	INNER JOIN user_auth_table uat WITH (NOLOCK) ON uat.id = updt.user_id
	WHERE uat.username = @username
		AND uat.is_blocked = 0
		AND uat.is_blocked = 0
END
GO
/****** Object:  StoredProcedure [dbo].[getStausOfUserRequestUsingReferenceNumber]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getStausOfUserRequestUsingReferenceNumber] (
	@referenceNumber VARCHAR(20)
	)
AS
BEGIN
	
	SET NOCOUNT ON;
	DECLARE @maxTime TABLE (latestTime DATETIME2)

	INSERT INTO @maxTime (latestTime)
	SELECT MAX(cuth.updated_time) 
	FROM contact_us_table_hist cuth
	INNER JOIN contact_us_table cut ON cut.id = cuth.contact_us_id
	AND cut.reference_number IN ( @referenceNumber, 'COM_' + @referenceNumber);

    SELECT cut.email AS email
		,updt.name AS name
		,cut.request_status AS requestStatus
		,cut.request_reason AS requestType
		,CASE 
			WHEN cut.is_request_active = 1 THEN 'Yes'
			ELSE 'No'
			END AS isRequestActive
		,cuth.message AS description
		,cut.start_time AS requestedDate
	FROM contact_us_table cut WITH (NOLOCK)
	INNER JOIN contact_us_table_hist cuth WITH (NOLOCK) ON cuth.contact_us_id = cut.id
	INNER JOIN @maxTime mt ON mt.latestTime = cuth.updated_time
	LEFT JOIN user_auth_table uat WITH (NOLOCK) ON uat.username = cut.email
	LEFT JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	WHERE (
		-- Avoid case sensitivity so as to match reference number exactly with user and db
		cut.reference_number COLLATE Latin1_General_CS_AS = @referenceNumber
		OR
		cut.reference_number COLLATE Latin1_General_CS_AS = 'COM_' + @referenceNumber AND cut.is_request_active = 0 AND cut.is_verified = 1
	)
END
GO
/****** Object:  StoredProcedure [dbo].[getTestValuesForStreaming]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getTestValuesForStreaming]
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT * from test_table;
END
GO
/****** Object:  StoredProcedure [dbo].[getTotalCurrentGoalIncome]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getTotalCurrentGoalIncome] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT SUM(gt.current_amount)
	FROM goal_table gt WITH (NOLOCK)
	WHERE gt.user_id = @userId
		AND gt.is_deleted = 0
END
GO
/****** Object:  StoredProcedure [dbo].[getTotalExpenseInMonthAndYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getTotalExpenseInMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT SUM(et.amount) 
	FROM expense_table et WITH (NOLOCK) 
	WHERE et.user_id = @userId
		AND et.is_deleted = 0
		AND MONTH(et.date) = @month
		AND YEAR(et.date) = @year
END
GO
/****** Object:  StoredProcedure [dbo].[getTotalIncomeInMonthAndYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getTotalIncomeInMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT SUM(it.amount) 
	FROM income_table it WITH (NOLOCK) 
	WHERE it.user_id = @userId
		AND it.is_deleted = 0
		AND MONTH(it.date) = @month
		AND YEAR(it.date) = @year
END
GO
/****** Object:  StoredProcedure [dbo].[getTotalTargetGoalIncome]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getTotalTargetGoalIncome] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT SUM(gt.target_amount)
	FROM goal_table gt WITH (NOLOCK)
	WHERE gt.user_id = @userId
		AND gt.is_deleted = 0
END
GO
/****** Object:  StoredProcedure [dbo].[getUserAuthDetailsByUsername]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUserAuthDetailsByUsername] (
	@username VARCHAR(50)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT uat.* 
	FROM user_auth_table uat WITH (NOLOCK)
	WHERE uat.username = @username
END
GO
/****** Object:  StoredProcedure [dbo].[getUserAuthDetailsListWhoseOtpCountGreaterThanThree]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUserAuthDetailsListWhoseOtpCountGreaterThanThree] (
	@startOfToday DATETIME2
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT uat.* 
	FROM user_auth_table uat WITH (NOLOCK)
	WHERE uat.otp_count > 0 
		AND uat.verification_code_expiration < @startOfToday;
END
GO
/****** Object:  StoredProcedure [dbo].[getUserDetailsForPdfGeneration]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[getUserDetailsForPdfGeneration] (
	@userId BIGINT
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT updt.name AS name
		,uat.username AS username
		,updt.phone AS phoneNumber
		,updt.address AS address
	FROM user_profile_details_table updt WITH (NOLOCK)
	INNER JOIN user_auth_table uat WITH (NOLOCK) ON uat.id = updt.user_id
	WHERE updt.user_id = @userId
		AND uat.is_blocked = 0
		AND uat.is_deleted = 0
END
GO
/****** Object:  StoredProcedure [dbo].[getUserFeedbackListForAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUserFeedbackListForAdmin]

AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT cut.id AS feedbackId
		,cuth.message AS description
		,cut.start_time AS timeOfFeedback
	FROM contact_us_table cut WITH (NOLOCK) 
	INNER JOIN contact_us_table_hist cuth WITH (NOLOCK) ON cuth.contact_us_id = cut.id
	WHERE cut.request_reason = 'USER_FEEDBACK_UPDATE'
	AND cut.request_status = 'SUBMITTED'
	AND cut.is_request_active = 1
	AND cut.is_verified = 0
	ORDER BY cut.start_time DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getUserGridDetailsByStatusForAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUserGridDetailsByStatusForAdmin] (
	@status VARCHAR(20)
	)
AS
BEGIN
	
	SET NOCOUNT ON;
	
    SELECT updt.name AS name
		,uat.username AS username
		,updt.phone AS phone
		,updt.created_date AS createdDateTime
		,updt.date_of_birth AS dateOfBirth
	FROM user_auth_table uat WITH (NOLOCK) 
	INNER JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	INNER JOIN user_role_table urt WITH (NOLOCK) ON urt.role_id = uat.role_id
	--WHERE uat.is_blocked = 0
	--	AND uat.is_deleted = 0
		AND urt.role_name IN ('USER')
	WHERE (@status = 'ALL'
		OR (@status = 'ACTIVE' AND uat.is_blocked = 0 AND uat.is_deleted = 0 )
		OR (@status = 'BLOCKED' AND uat.is_blocked = 1 AND uat.is_deleted = 0 )
		OR (@status = 'DELETED' AND uat.is_deleted = 0 AND uat.is_deleted = 1 )
	)

	ORDER BY createdDateTime DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getUserIdFromUsernameAndToken]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getUserIdFromUsernameAndToken] (
	@username VARCHAR(30),
	@token VARCHAR(2000)
)
AS
BEGIN
	
	SET NOCOUNT ON;

	-- Check if token is blacklisted
    IF EXISTS (SELECT 1 FROM blacklist_token_table WHERE token = @token)
    BEGIN
        -- Return an error - use RAISERROR
        RAISERROR ('Token is blacklisted.', 16, 1);
        RETURN;
    END

    -- Return userId if token is not blacklisted
    SELECT uat.id AS userId
    FROM user_auth_table uat WITH (NOLOCK)
    WHERE uat.username = @username;
END
GO
/****** Object:  StoredProcedure [dbo].[getUserMonthlyCountInAYear]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   procedure [dbo].[getUserMonthlyCountInAYear] (
	@year INT
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT MONTH(updt.created_date) AS month
		,COUNT(uat.id) AS userCount
	FROM user_auth_table uat WITH (NOLOCK)
	INNER JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	WHERE YEAR(updt.created_date) = @year
	GROUP BY MONTH(updt.created_date)
END
GO
/****** Object:  StoredProcedure [dbo].[getUserRaisedDefectsForAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[getUserRaisedDefectsForAdmin] 
AS
BEGIN
	
	SET NOCOUNT ON;

	DECLARE @maxTime TABLE (latestTime DATETIME2)

	INSERT INTO @maxTime (latestTime)
	SELECT MAX(cuth.updated_time) 
	FROM contact_us_table_hist cuth
	INNER JOIN contact_us_table cut ON cut.id = cuth.contact_us_id
	WHERE cut.request_reason IN ('USER_DEFECT_UPDATE')
	GROUP BY cuth.contact_us_id

    SELECT cut.id AS defectId
		,updt.name AS name
		,cut.email AS username
		,cut.reference_number AS referenceNumber
		,cuth.message AS description
		,cut.request_status AS defectStatus
	FROM contact_us_table cut WITH (NOLOCK) 
	INNER JOIN contact_us_table_hist cuth WITH (NOLOCK) ON cuth.contact_us_id = cut.id
	INNER JOIN @maxTime mt ON mt.latestTime = cuth.updated_time
	LEFT JOIN user_auth_table uat WITH (NOLOCK) ON uat.username = cut.email
	LEFT JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	WHERE cut.request_reason = 'USER_DEFECT_UPDATE'
	ORDER BY cuth.updated_time DESC;
END
GO
/****** Object:  StoredProcedure [dbo].[getUserRequestsGridForAdmin]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUserRequestsGridForAdmin] (
	@requestReason VARCHAR(50)
	)
	
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT 
		CASE 
			WHEN cntUs.request_reason IN ( 'ACCOUNT_UNBLOCK_REQUEST', 'ACCOUNT_NOT_DELETE_REQUEST')
				THEN updt.name
			ELSE cuth.name 
			END AS name
		,cntUs.email AS username
		,CASE 
			WHEN cntUs.request_reason = 'NAME_CHANGE_REQUEST' 
				THEN 'Name Change'
			WHEN cntUs.request_reason = 'ACCOUNT_UNBLOCK_REQUEST' 
				THEN 'Account Unblock'
			WHEN cntUs.request_reason = 'ACCOUNT_NOT_DELETE_REQUEST' 
				THEN 'Account Retrieval'
			END AS requestType
		,cntUs.reference_number AS referenceNumber
		,cuth.message AS description
	FROM contact_us_table cntUs WITH (NOLOCK) 
	INNER JOIN contact_us_table_hist cuth WITH (NOLOCK) ON cuth.contact_us_id = cntUs.id
	INNER JOIN user_auth_table uat WITH (NOLOCK) ON	uat.username = cntUs.email
	INNER JOIN user_profile_details_table updt WITH (NOLOCK) ON updt.user_id = uat.id
	WHERE (
			@requestReason = 'All' OR cntUs.request_reason = @requestReason
		)
		AND cuth.request_reason IN ('NAME_CHANGE_REQUEST', 'ACCOUNT_UNBLOCK_REQUEST', 'ACCOUNT_NOT_DELETE_REQUEST')
		AND cntUs.is_verified = 0
		AND cntUs.is_request_active = 1
		AND cuth.request_status = 'SUBMITTED'
	ORDER BY cuth.updated_time DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getUsersByUsingUserProfileDetails]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create   procedure [dbo].[getUsersByUsingUserProfileDetails] (
	@dateOfBirth DATE,
	@name VARCHAR(255),
	@gender VARCHAR(50),
	@maritalStatus VARCHAR(50)
	)	
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT updt.* 
	FROM user_profile_details_table updt WITH (NOLOCK)
	WHERE updt.date_of_birth = @dateOfBirth
		AND updt.name = @name
		AND updt.gender = @gender
		AND updt.marital_status = @maritalStatus
END
GO
/****** Object:  StoredProcedure [dbo].[getUserScheduledNotifications]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create   procedure [dbo].[getUserScheduledNotifications] (
	@username VARCHAR(50)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT unt.id AS notificationId
		,snt.subject AS subject
		,snt.description AS description
		,snt.schedule_from AS scheduleFrom
		,snt.schedule_to AS scheduleTo
		,unt.is_read AS isRead
	FROM schedule_notification_table snt WITH (NOLOCK) 
	INNER JOIN user_notification_table unt WITH (NOLOCK) ON unt.schedule_id = snt.id
	WHERE unt.username = @username
		AND snt.is_cancelled = 0
		AND CAST(snt.schedule_to AS DATE) > CAST(GETDATE() AS DATE)
		AND snt.is_active = 1
	ORDER BY unt.is_read
		,snt.created_date DESC
END
GO
/****** Object:  StoredProcedure [dbo].[updateRecurringIncomesAndExpenses]    Script Date: 22-11-2025 22:24:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   procedure [dbo].[updateRecurringIncomesAndExpenses]
	
AS
BEGIN
	
	--SET NOCOUNT ON;

	--DECLARE @incomeTable TABLE (
 --       id BIGINT,
 --       amount DECIMAL(10,2),
 --       category VARCHAR(50),
 --       date DATETIME,
 --       is_deleted BIT,
 --       recurring BIT,
 --       source VARCHAR(500),
 --       user_id BIGINT
 --   );

	---- Step 1: Collect recurring incomes from previous month and insert into table variable
 --   INSERT INTO @incomeTable
	--SELECT it.id
	--	,it.amount
	--	,it.category
	--	,it.date
	--	,it.is_deleted
	--	,it.recurring
	--	,it.source
	--	,it.user_id
	--FROM income_table it WITH (NOLOCK)
	--WHERE it.recurring = 1
	--	AND MONTH(it.date) = MONTH(GETDATE())-1
	--	AND it.is_deleted = 0

	---- Step 2: Loop through table variable
	--DECLARE @count INT, @i INT = 1;
	--SET @count = (SELECT COUNT(*) FROM @incomeTable);

	--WHILE @i <= @count
	--BEGIN
	--	DECLARE @amount DECIMAL(10,2),
 --               @category VARCHAR(50),
 --               @is_deleted BIT,
 --               @recurring BIT,
 --               @source VARCHAR(500),
 --               @user_id BIGINT;

	--	-- Pick row by row
	--	SELECT TOP 1
 --              @amount = amount,
 --              @category = category,
 --              @is_deleted = is_deleted,
 --              @recurring = recurring,
 --              @source = source,
 --              @user_id = user_id
 --       FROM (
 --           SELECT ROW_NUMBER() OVER (ORDER BY id) AS rn, *
 --           FROM @incomeTable
 --       ) t
 --       WHERE rn = @i;

	--	-- Step 3: Insert with today's date
	--	INSERT INTO income_table (amount, category, date, is_deleted, recurring, source, user_id)
 --       VALUES (@amount, @category, GETDATE(), @is_deleted, @recurring, @source, @user_id);

 --       SET @i = @i + 1;
	--END;

	SET NOCOUNT ON;

	-- Insert new rows directly without looping
	INSERT INTO income_table (amount, category, date, is_deleted, recurring, source, user_id)
	SELECT 
		it.amount,
		it.category,
		GETDATE(),
		it.is_deleted,
		it.recurring,
		it.source,
		it.user_id
	FROM income_table it WITH (NOLOCK)
	WHERE it.recurring = 1
	  AND it.is_deleted = 0
	  AND MONTH(it.date) = MONTH(DATEADD(MONTH, -1, GETDATE()))   -- last month
	  AND YEAR(it.date)  = YEAR(DATEADD(MONTH, -1, GETDATE()))   -- last year check
	  AND NOT EXISTS (
        SELECT 1
        FROM income_table existing
        WHERE existing.recurring = 1
          AND existing.user_id = it.user_id
          AND existing.amount = it.amount
          AND existing.category = it.category
		  AND existing.source = it.source
          AND MONTH(existing.date) = MONTH(GETDATE())         -- already inserted this month?
          AND YEAR(existing.date)  = YEAR(GETDATE())
    );
END
GO
USE [master]
GO
ALTER DATABASE [moneyfi_db] SET  READ_WRITE 
GO
