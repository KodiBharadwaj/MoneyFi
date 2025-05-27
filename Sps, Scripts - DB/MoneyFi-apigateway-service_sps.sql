CREATE PROCEDURE [dbo].[getSessionTokenModelByToken] (
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




CREATE PROCEDURE [dbo].[getSessionTokenModelByUsername] (
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




CREATE PROCEDURE [dbo].[getBlackListTokenDetailsByToken] (
	@token VARCHAR(1000)
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT btt.*
    FROM blacklist_token_table btt WITH (NOLOCK)
	WHERE btt.token = @token;
END
GO





CREATE PROCEDURE [dbo].[getUserAuthDetailsByUsername] (
	@username VARCHAR(100)
	)
AS
BEGIN

	SET NOCOUNT ON;

    SELECT uat.*
	FROM user_auth_table uat WITH (NOLOCK)
	WHERE uat.username = @username;
END
GO





CREATE PROCEDURE [dbo].[getUserAuthDetailsListWhoseOtpCountGreaterThanThree] (
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
