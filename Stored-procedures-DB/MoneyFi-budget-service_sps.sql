CREATE PROCEDURE [dbo].[getAllBudgetsByUserId] (
	@userId BIGINT
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT bt.id as id
		,bt.category as category
		,bt.current_spending as currentSpending
		,bt.money_limit as moneyLimit
	FROM budget_table bt WITH (NOLOCK)
	WHERE bt.user_id = @userId

	ORDER BY id
END
GO





CREATE PROCEDURE [dbo].[getAllBudgetsByUserIdAndByCategory] (
	@userId BIGINT,
	@category VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT bt.id as id
		,bt.category as category
		,bt.current_spending as currentSpending
		,bt.money_limit as moneyLimit
	FROM budget_table bt WITH (NOLOCK)
	WHERE bt.user_id = @userId
		AND bt.category = @category

	ORDER BY id
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

    SELECT SUM(et.amount)
    FROM expense_table et WITH (NOLOCK)
	WHERE et.user_id = @userId
	    AND et.is_deleted = 0;
        AND MONTH(et.date) = @month
        AND YEAR(et.date) = @year

END
GO