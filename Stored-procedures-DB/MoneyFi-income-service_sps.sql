CREATE PROCEDURE [dbo].[getAllIncomesByMonthAndYear] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id as id
		,it.amount as amount
		,it.source as source
		,it.date as date
		,it.category as category
		,it.recurring as recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
	AND MONTH(it.date) = @month
	AND YEAR(it.date) = @year
	AND it.is_deleted = @deleteStatus

	ORDER BY id
END
GO





CREATE PROCEDURE [dbo].[getAllIncomesByMonthAndYearAndByCategory] (
	@userId BIGINT,
	@month INT,
	@year INT,
	@category VARCHAR(100),
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id as id
		,it.amount as amount
		,it.source as source
		,it.date as date
		,it.category as category
		,it.recurring as recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND MONTH(it.date) = @month
		AND YEAR(it.date) = @year
		AND it.is_deleted = @deleteStatus
		AND it.category = @category

	ORDER BY id
END
GO





CREATE PROCEDURE [dbo].[getDeletedIncomesInAMonth] (
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
	WHERE it.user_id = @userId
	AND MONTH(it.date) = @month
	AND YEAR(it.date) = @year
	AND it.is_deleted = 1
END
GO




CREATE PROCEDURE [dbo].[getAllIncomesByYear] (
	@userId BIGINT,
	@year INT,
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id as id
		,it.amount as amount
		,it.source as source
		,it.date as date
		,it.category as category
		,it.recurring as recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND YEAR(it.date) = @year
		AND it.is_deleted = @deleteStatus

	ORDER BY id
END
GO






CREATE PROCEDURE [dbo].[getAllIncomesByYearAndByCategory] (
	@userId BIGINT,
	@year INT,
	@category VARCHAR(100),
	@deleteStatus BIT
	)
AS
BEGIN

	SET NOCOUNT ON;

	SELECT it.id as id
		,it.amount as amount
		,it.source as source
		,it.date as date
		,it.category as category
		,it.recurring as recurring
	FROM income_table it WITH (NOLOCK)
	WHERE it.user_id = @userId
		AND YEAR(it.date) = @year
		AND it.is_deleted = @deleteStatus
		AND it.category = @category

	ORDER BY id
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




CREATE PROCEDURE [dbo].[getRemainingIncomeUpToPreviousMonthByMonthAndYear] (
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
	AND YEAR(it.date) = @year
	AND MONTH(it.date) < @month
	AND it.is_deleted = 0;
END
GO






CREATE PROCEDURE [dbo].[getTotalExpensesUpToPreviousMonth] (
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
	AND YEAR(et.date) = @year
	AND MONTH(et.date) < @month
	AND et.is_deleted = 0;
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





CREATE PROCEDURE [dbo].[getAvailableBalanceOfUser] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

	DECLARE @totalIncome NUMERIC(38,10) = (
		SELECT ISNULL(SUM(amount), 0)
		FROM income_table
		WHERE user_id = @userId
			AND is_deleted = 0
			);

    DECLARE @totalExpense NUMERIC(38,10) = (
		SELECT ISNULL(SUM(amount), 0)
		FROM expense_table
		WHERE user_id = @userId
			AND is_deleted = 0
			);

    DECLARE @remaining NUMERIC(38,10) = @totalIncome - @totalExpense;

    SELECT @remaining AS RemainingAmount;

END
GO