ALTER TABLE income_table
ALTER COLUMN amount DECIMAL(38,2)

ALTER TABLE income_table
ALTER COLUMN date DATETIME

ALTER TABLE expense_table
ALTER COLUMN amount DECIMAL(38,2)

ALTER TABLE expense_table
ALTER COLUMN date DATETIME

ALTER TABLE budget_table
ALTER COLUMN current_spending DECIMAL(38,2)

ALTER TABLE budget_table
ALTER COLUMN money_limit DECIMAL(38,2)

ALTER TABLE goal_table
ALTER COLUMN current_amount DECIMAL(38,2)

ALTER TABLE goal_table
ALTER COLUMN target_amount DECIMAL(38,2)

ALTER TABLE goal_table
ALTER COLUMN dead_line DATETIME


