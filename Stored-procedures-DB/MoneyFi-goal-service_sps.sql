CREATE PROCEDURE [dbo].[getAllGoalsByUserId] (
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

		SELECT gt.id as id
			,gt.goal_name as goalName
			,gt.current_amount as currentAmount
			,gt.target_amount as targetAmount
			,gt.dead_line as deadLine
			,gt.category as category
			,gt.is_deleted as isDeleted
			,CAST(DATEDIFF(DAY, @today, CAST(gt.dead_line AS DATE)) AS BIGINT) AS daysRemaining
			,CAST(((gt.current_amount / gt.target_amount) * 100) AS BIGINT) AS progressPercentage
		FROM goal_table gt WITH (NOLOCK)
		WHERE gt.user_id = @userId
			AND gt.is_deleted = 0

			) AS tempTable

		
	ORDER BY id
END
GO






CREATE PROCEDURE [dbo].[getTotalCurrentGoalIncome] (
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






CREATE PROCEDURE [dbo].[getTotalTargetGoalIncome] (
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