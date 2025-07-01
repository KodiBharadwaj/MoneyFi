USE [master]
GO
/****** Object:  Database [moneyfi_db]    Script Date: 02-07-2025 00:24:52 ******/
CREATE DATABASE [moneyfi_db]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'moneyfi_db', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\moneyfi_db.mdf' , SIZE = 73728KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'moneyfi_db_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\moneyfi_db_log.ldf' , SIZE = 139264KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
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
/****** Object:  User [santhosh]    Script Date: 02-07-2025 00:24:52 ******/
CREATE USER [santhosh] FOR LOGIN [santhosh] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  Table [dbo].[blacklist_token_table]    Script Date: 02-07-2025 00:24:52 ******/
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
/****** Object:  Table [dbo].[budget_table]    Script Date: 02-07-2025 00:24:52 ******/
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
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[contact_us_table]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[contact_us_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NULL,
	[images] [text] NULL,
	[message] [varchar](255) NULL,
	[name] [varchar](255) NULL,
	[is_verified] [bit] NULL,
	[reference_number] [varchar](255) NULL,
	[is_request_active] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[expense_table]    Script Date: 02-07-2025 00:24:52 ******/
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
/****** Object:  Table [dbo].[feedback_table]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[feedback_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[comments] [varchar](255) NULL,
	[email] [varchar](255) NULL,
	[name] [varchar](255) NULL,
	[rating] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[goal_table]    Script Date: 02-07-2025 00:24:52 ******/
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
/****** Object:  Table [dbo].[income_table]    Script Date: 02-07-2025 00:24:52 ******/
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
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[income_table_deleted]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[income_table_deleted](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[expiry_date_time] [datetime2](6) NULL,
	[income_id] [bigint] NULL,
	[start_date_time] [datetime2](6) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[otp_temp_table]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[otp_temp_table](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NULL,
	[expiration_time] [datetime2](6) NULL,
	[otp] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[session_token_table]    Script Date: 02-07-2025 00:24:52 ******/
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
/****** Object:  Table [dbo].[user_auth_table]    Script Date: 02-07-2025 00:24:52 ******/
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
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[user_profile_details_table]    Script Date: 02-07-2025 00:24:52 ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  StoredProcedure [dbo].[getAccountStatementOfUser]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================

CREATE procedure [dbo].[getAccountStatementOfUser] (
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
			,CONCAT(it.source, ' (', it.category, ')') AS description
			,it.amount AS amount
			,'credit' AS creditOrDebit
		FROM income_table it WITH (NOLOCK)
		WHERE it.user_id = @userId
			AND it.is_deleted = 0
			AND it.date BETWEEN @startDate AND @endDate

		UNION ALL

		SELECT et.date AS transactionDate
			,CONCAT(et.description, ' (', et.category, ')') AS description
			,et.amount AS amount
			,'debit' AS creditOrDebit
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId
			AND et.is_deleted = 0
			AND et.date BETWEEN @startDate AND @endDate

		ORDER BY transactionDate,
		creditOrDebit
		END

	ELSE
	BEGIN
		SELECT it.date AS transactionDate
			,CONCAT(it.source, ' (', it.category, ')') AS description
			,it.amount AS amount
			,'credit' AS creditOrDebit
		FROM income_table it WITH (NOLOCK)
		WHERE it.user_id = @userId
			AND it.is_deleted = 0
			AND it.date BETWEEN @startDate AND @endDate

		UNION ALL

		SELECT et.date AS transactionDate
			,CONCAT(et.description, ' (', et.category, ')') as description
			,et.amount AS amount
			,'debit' AS creditOrDebit
		FROM expense_table et WITH (NOLOCK)
		WHERE et.user_id = @userId
			AND et.is_deleted = 0
			AND et.date BETWEEN @startDate AND @endDate

		ORDER BY transactionDate,
		creditOrDebit

		offset @offset rows
		FETCH NEXT @limit rows ONLY;
		END
END


GO
/****** Object:  StoredProcedure [dbo].[getActiveUserDetailsForAdmin]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[getActiveUserDetailsForAdmin]
	
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
	WHERE uat.is_blocked = 0
		AND uat.is_deleted = 0
		AND uat.role_id <> 1
	ORDER BY createdDateTime DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getAdminOverviewPageDetails]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[getAdminOverviewPageDetails]

AS
BEGIN
	
	SET NOCOUNT ON;

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
	WHERE uat.role_id <> 1
END
GO
/****** Object:  StoredProcedure [dbo].[getAllBudgetsByUserId]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE   PROCEDURE [dbo].[getAllBudgetsByUserId] (
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
		,CAST((currentSpending/moneyLimit)*100 AS BIGINT) AS progressPercentage
	FROM (
		SELECT bt.id AS id
		,bt.category AS category
		,SUM(ISNULL(et.amount, 0)) AS currentSpending
		,bt.money_limit AS moneyLimit
		FROM budget_table bt WITH (NOLOCK)
		LEFT JOIN expense_table et WITH (NOLOCK) ON et.user_id = bt.user_id 
			AND MONTH(et.date) = @month
			AND YEAR(et.date) = @year
			AND et.is_deleted = 0
			AND bt.category = et.category
		WHERE bt.user_id = @userId
		GROUP BY 
        bt.id, bt.category, bt.money_limit
				) AS tempTable

		ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllBudgetsByUserIdAndByCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getAllBudgetsByUserIdAndByCategory] (
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
		,CAST((currentSpending/moneyLimit)*100 AS BIGINT) AS progressPercentage
	FROM (
		SELECT bt.id as id
			,bt.category as category
			,SUM(ISNULL(et.amount, 0)) AS currentSpending
			,bt.money_limit as moneyLimit
		FROM budget_table bt WITH (NOLOCK)
		LEFT JOIN expense_table et WITH (NOLOCK) ON et.user_id = bt.user_id
			AND et.category = bt.category
			AND MONTH(et.date) = @month
			AND YEAR(et.date) = @year
			AND et.is_deleted = 0
		WHERE bt.user_id = @userId
			AND bt.category = @category

		GROUP BY bt.id, bt.category, bt.money_limit
				) AS tempTable

		ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByMonthAndYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getAllExpensesByMonthAndYear] (
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
		
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByMonthAndYearAndByCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllExpensesByMonthAndYearAndByCategory] (
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
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByUserId]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllExpensesByUserId] (
	@userId BIGINT
	)
AS
BEGIN
	SET NOCOUNT ON;

    SELECT et.* 
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId;
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllExpensesByYear] (
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
END
GO
/****** Object:  StoredProcedure [dbo].[getAllExpensesByYearAndByCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllExpensesByYearAndByCategory] (
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
END
GO
/****** Object:  StoredProcedure [dbo].[getAllGoalsByUserId]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getAllGoalsByUserId] (
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
/****** Object:  StoredProcedure [dbo].[getAllIncomesByMonthAndYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllIncomesByMonthAndYear] (
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

	ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByMonthAndYearAndByCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllIncomesByMonthAndYearAndByCategory] (
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

	ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllIncomesByYear] (
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

	ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesByYearAndByCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllIncomesByYearAndByCategory] (
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

	ORDER BY id
END
GO
/****** Object:  StoredProcedure [dbo].[getAllIncomesOfUser]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getAllIncomesOfUser] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT * 
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId;
END
GO
/****** Object:  StoredProcedure [dbo].[getAvailableBalanceOfUser]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getAvailableBalanceOfUser] (
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
/****** Object:  StoredProcedure [dbo].[getBlackListTokenDetailsByToken]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getBlackListTokenDetailsByToken] (
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
/****** Object:  StoredProcedure [dbo].[getBlockedUserDetailsForAdmin]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create PROCEDURE [dbo].[getBlockedUserDetailsForAdmin]
	
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
	WHERE uat.is_blocked = 1
		AND uat.is_deleted = 0
		AND uat.role_id <> 1
	ORDER BY createdDateTime DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getContactUsDetailsOfUsers]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[getContactUsDetailsOfUsers]
	
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT cntUs.* 
	FROM contact_us_table cntUs WITH (NOLOCK) 
	WHERE cntUs.is_verified = 0
	ORDER BY cntUs.id DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getDeletedIncomesInAMonth]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getDeletedIncomesInAMonth] (
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
/****** Object:  StoredProcedure [dbo].[getDeletedUserDetailsForAdmin]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create PROCEDURE [dbo].[getDeletedUserDetailsForAdmin]
	
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
	WHERE uat.is_deleted = 1
		AND uat.role_id <> 1
	ORDER BY createdDateTime DESC
END
GO
/****** Object:  StoredProcedure [dbo].[getIncomeByIncomeId]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getIncomeByIncomeId] (
	@incomeId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT it.amount 
	FROM income_table it WITH (NOLOCK)
	WHERE it.id = @incomeId;
END
GO
/****** Object:  StoredProcedure [dbo].[getIncomeBySourceAndCategory]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getIncomeBySourceAndCategory] (
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
/****** Object:  StoredProcedure [dbo].[getMonthlyExpensesListInAYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getMonthlyExpensesListInAYear] (
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
/****** Object:  StoredProcedure [dbo].[getMonthlyIncomesListInAYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getMonthlyIncomesListInAYear] (
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
/****** Object:  StoredProcedure [dbo].[getOverviewPageDetails]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getOverviewPageDetails] (
	@userId BIGINT,
	@month INT,
	@year INT
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

    DECLARE @availableBalance NUMERIC(38,2) = @totalIncome - @totalExpense;

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
/****** Object:  StoredProcedure [dbo].[getProfileDetailsByUserId]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getProfileDetailsByUserId] (
	@userId BIGINT
	)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    SELECT updt.* 
	FROM user_profile_details_table updt WITH (NOLOCK)
	WHERE updt.user_id = @userId;
END
GO
/****** Object:  StoredProcedure [dbo].[getProfileDetailsOfUser]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getProfileDetailsOfUser] (
	@userId BIGINT
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
	WHERE uat.id = @userId
		AND uat.is_blocked = 0
		AND uat.is_blocked = 0
END

GO
/****** Object:  StoredProcedure [dbo].[getSessionTokenModelByToken]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getSessionTokenModelByToken] (
	@token VARCHAR(1000)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT stt.* 
	FROM session_token_table stt WITH (NOLOCK)
	WHERE stt.token = @token;
END
GO
/****** Object:  StoredProcedure [dbo].[getSessionTokenModelByUsername]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getSessionTokenModelByUsername] (
	@username VARCHAR(1000)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT stt.* 
	FROM session_token_table stt WITH (NOLOCK)
	WHERE stt.username = @username;
END
GO
/****** Object:  StoredProcedure [dbo].[getTotalCurrentGoalIncome]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getTotalCurrentGoalIncome] (
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
/****** Object:  StoredProcedure [dbo].[getTotalExpenseInMonthAndYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getTotalExpenseInMonthAndYear] (
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
/****** Object:  StoredProcedure [dbo].[getTotalIncomeInMonthAndYear]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getTotalIncomeInMonthAndYear] (
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
/****** Object:  StoredProcedure [dbo].[getTotalTargetGoalIncome]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE procedure [dbo].[getTotalTargetGoalIncome] (
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
/****** Object:  StoredProcedure [dbo].[getUserAuthDetailsByUsername]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getUserAuthDetailsByUsername] (
	@username VARCHAR(100)
	)
AS
BEGIN
	
	SET NOCOUNT ON;

    SELECT uat.* 
	FROM user_auth_table uat WITH (NOLOCK)
	WHERE uat.username = @username
		AND uat.is_blocked = 0
		AND uat.is_deleted = 0
END
GO
/****** Object:  StoredProcedure [dbo].[getUserAuthDetailsListWhoseOtpCountGreaterThanThree]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getUserAuthDetailsListWhoseOtpCountGreaterThanThree] (
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
/****** Object:  StoredProcedure [dbo].[getUserDetailsForAccountStatement]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getUserDetailsForAccountStatement] (
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
/****** Object:  StoredProcedure [dbo].[getUsersByUsingUserProfileDetails]    Script Date: 02-07-2025 00:24:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[getUsersByUsingUserProfileDetails] (
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
USE [master]
GO
ALTER DATABASE [moneyfi_db] SET  READ_WRITE 
GO
