/****** Object:  Trigger [dbo].[trg_UpdateExpenseFromGoal]    Script Date: 28-05-2025 23:30:42 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO
CREATE TRIGGER [dbo].[trg_UpdateExpenseFromGoal]
ON [dbo].[goal_table] 
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Temporary table to hold split ids
    ;WITH SplitExpenses AS (
        SELECT 
            gt.goal_name,
            TRIM(value) AS expense_id
        FROM INSERTED gt
        CROSS APPLY STRING_SPLIT(gt.expense_ids, ',')
        WHERE value IS NOT NULL AND TRY_CAST(value AS BIGINT) IS NOT NULL
    )
    UPDATE et
    SET et.description = se.goal_name
    FROM expense_table et
    INNER JOIN SplitExpenses se ON et.id = CAST(se.expense_id AS BIGINT);
END;

GO

ALTER TABLE [dbo].[goal_table] ENABLE TRIGGER [trg_UpdateExpenseFromGoal]
GO