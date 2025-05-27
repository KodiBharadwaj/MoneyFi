CREATE PROCEDURE [dbo].[getAllExpensesByMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id as id
		,et.category as category
		,et.amount as amount
		,et.date as date
		,et.recurring as recurring
		,et.description as description
		,et.is_deleted as is_deleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND MONTH(et.date) = @month
		AND YEAR(et.date) = @year
		AND et.is_deleted = @deleteStatus
END
GO





CREATE PROCEDURE [dbo].[getAllExpensesByMonthAndYearAndByCategory] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id as id
		,et.category as category
		,et.amount as amount
		,et.date as date
		,et.recurring as recurring
		,et.description as description
		,et.is_deleted as is_deleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND MONTH(et.date) = @month
		AND YEAR(et.date) = @year
		AND et.is_deleted = @deleteStatus
		AND et.category = @category
END





CREATE PROCEDURE [dbo].[getAllExpensesByYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id as id
		,et.category as category
		,et.amount as amount
		,et.date as date
		,et.recurring as recurring
		,et.description as description
		,et.is_deleted as is_deleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND YEAR(et.date) = @year
		AND et.is_deleted = @deleteStatus
END
GO





CREATE PROCEDURE [dbo].[getAllExpensesByYearAndByCategory] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT et.id as id
		,et.category as category
		,et.amount as amount
		,et.date as date
		,et.recurring as recurring
		,et.description as description
		,et.is_deleted as is_deleted
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
		AND YEAR(et.date) = @year
		AND et.is_deleted = @deleteStatus
		AND et.category = @category
END
GO





CREATE PROCEDURE [dbo].[getTotalExpenseInMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT SUM(et.amount) FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
	AND MONTH(et.date) = @month
	AND YEAR(et.date) = @year
	AND et.is_deleted = 0;
END
GO






CREATE PROCEDURE [dbo].[getTotalIncomeInMonthAndYear] (
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
		AND MONTH(it.date) = @month
		AND YEAR(it.date) = @year
		AND it.is_deleted = 0;
END
GO





CREATE PROCEDURE [dbo].[getAllExpensesByUserId] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT *
    FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId;
END
GO






CREATE PROCEDURE [dbo].[getMonthlyExpensesListInAYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)

AS
BEGIN

	SET NOCOUNT ON;

    SELECT MONTH(et.date) as month
	, SUM(et.amount)
	FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
	AND YEAR(et.date) = @year
	AND et.is_deleted = @deleteStatus
	GROUP BY MONTH(et.date)
	ORDER BY month ASC;
END
GO





CREATE PROCEDURE [dbo].[getMonthlyIncomesListInAYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)

AS
BEGIN

	SET NOCOUNT ON;

    SELECT MONTH(it.date) as month
	, SUM(it.amount)
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
	AND YEAR(it.date) = @year
	AND it.is_deleted = @deleteStatus
	GROUP BY MONTH(it.date)
	ORDER BY month ASC;
END
GO
