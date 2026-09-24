package beans;

import java.util.ArrayList;
import java.util.TreeMap;

public class AuthenticationProfileBean
{
	private int authenticationProfileId;
	private int organizationId;
	private String userId;
	private String textPassword;
	private String password;
	private String salt;
	private String firstName;
	private String lastName;
	private String mobilePhone;
	private String officePhone;
	private String officePhoneExt;
	private String homePhone;
	private int failedLoginAttempts;
	private String verificationCodeMethod;
	private String clientResult;
	private TreeMap< String, ArrayList<String>> rolesForDepartment = new TreeMap< String, ArrayList<String>>();

	
	public int getAuthenticationProfileId()
	{
		return authenticationProfileId;
	}
	
	public void setAuthenticationProfileId(int authenticationProfileId)
	{
		this.authenticationProfileId = authenticationProfileId;
	}
	
	public int getOrganizationId()
	{
		return organizationId;
	}

	public void setOrganizationId(int organizationId)
	{
		this.organizationId = organizationId;
	}

	public String getUserId()
	{
		return userId;
	}
	
	public void setUserId(String userId)
	{
		if ( userId != null )
			userId = userId.trim();
		this.userId = userId;
	}
	
	public String getTextPassword()
	{
		return textPassword;
	}

	public void setTextPassword(String textPassword)
	{
		if ( textPassword != null )
			textPassword = textPassword.trim();
		this.textPassword = textPassword;
	}

	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getSalt()
	{
		return salt;
	}
	
	public void setSalt(String salt)
	{
		this.salt = salt;
	}
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	public String getLastName()
	{
		return lastName;
	}
	
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	public String getMobilePhone()
	{
		return mobilePhone;
	}
	
	public void setMobilePhone(String mobilePhone)
	{
		this.mobilePhone = mobilePhone;
	}

	public String getOfficePhone() 
	{
		return officePhone;
	}

	public void setOfficePhone(String officePhone) 
	{
		this.officePhone = officePhone;
	}

	public String getOfficePhoneExt() 
	{
		return officePhoneExt;
	}

	public void setOfficePhoneExt(String officePhoneExt) 
	{
		this.officePhoneExt = officePhoneExt;
	}

	public String getHomePhone()
	{
		return homePhone;
	}
	
	public void setHomePhone(String homePhone)
	{
		this.homePhone = homePhone;
	}
	
	public int getFailedLoginAttempts()
	{
		return failedLoginAttempts;
	}
	
	public void setFailedLoginAttempts(int failedLoginAttempts)
	{
		this.failedLoginAttempts = failedLoginAttempts;
	}
	
	public String getVerificationCodeMethod()
	{
		return verificationCodeMethod;
	}
	
	public void setVerificationCodeMethod(String verificationCodeMethod)
	{
		this.verificationCodeMethod = verificationCodeMethod;
	}

	public String getClientResult()
	{
		return clientResult;
	}

	public void setClientResult(String clientResult)
	{
		this.clientResult = clientResult;
	}

	public TreeMap<String, ArrayList<String>> getRolesForDepartment()
	{
		return rolesForDepartment;
	}

	public void setRolesForDepartment(TreeMap<String, ArrayList<String>> rolesForDepartment)
	{
		this.rolesForDepartment = rolesForDepartment;
	}
}
