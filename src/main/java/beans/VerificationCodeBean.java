package beans;

public class VerificationCodeBean
{
	private int verificationCodeId;
	private int authenticationProfileId;
	private int applicationId;
	private String verificationCode;
	private String createdTimestamp;
	
	public int getVerificationCodeId()
	{
		return verificationCodeId;
	}
	
	public void setVerificationCodeId(int verificationCodeId)
	{
		this.verificationCodeId = verificationCodeId;
	}
	
	public int getAuthenticationProfileId()
	{
		return authenticationProfileId;
	}
	
	public void setAuthenticationProfileId(int authenticationProfileId)
	{
		this.authenticationProfileId = authenticationProfileId;
	}
	
	public int getApplicationId()
	{
		return applicationId;
	}
	
	public void setApplicationId(int applicationId)
	{
		this.applicationId = applicationId;
	}
	
	public String getVerificationCode()
	{
		return verificationCode;
	}
	
	public void setVerificationCode(String verificationCode)
	{
		this.verificationCode = verificationCode;
	}
	
	public String getCreatedTimestamp()
	{
		return createdTimestamp;
	}
	
	public void setCreatedTimestamp(String createdTimestamp)
	{
		this.createdTimestamp = createdTimestamp;
	}
}
