package beans;

import java.util.ArrayList;

public class UserBean
{
	private String userName;
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<OrganizationBean> allOrganizations = new ArrayList<OrganizationBean>();
	private ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = new ArrayList<AuthenticationProfileBean>();
	private ArrayList<DepartmentBean> allDepartments = new ArrayList<DepartmentBean>();
	private ArrayList<ApplicationBean> allApplications = new ArrayList<ApplicationBean>();
	private ArrayList<RoleBean> allRolesForApplication = new ArrayList<RoleBean>();
	private ArrayList<RoleBean> allRolesForAllApplications = new ArrayList<RoleBean>();
	private ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser = new ArrayList<ApplicationUserDepartmentRoleBean>();
	private ApplicationUserDepartmentRoleBean activeRoleAssignment = new ApplicationUserDepartmentRoleBean();
	private OrganizationBean activeOrganization = new OrganizationBean();
	private AuthenticationProfileBean activeAuthenticationProfile = new AuthenticationProfileBean();
	private DepartmentBean activeDepartment = new DepartmentBean();
	private AuthenticationProfileBean loginAuthenticationProfile = new AuthenticationProfileBean();
	private ApplicationBean activeApplication = new ApplicationBean();
	private RoleBean activeRole = new RoleBean();
	private VerificationCodeBean activeVerificationCode = new VerificationCodeBean();
	private String function;
	private int restrictedOrganizationId = 0;
	
	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		if ( userName != null )
			userName = userName.trim();
		this.userName = userName;
	}

	public ArrayList<String> getErrors()
	{
		return errors;
	}

	public ArrayList<String> getClearErrors()
	{
		errors.clear();
		return null;
	}

	public void setErrors(ArrayList<String> errors)
	{
		this.errors = errors;
	}

	public ArrayList<OrganizationBean> getAllOrganizations()
	{
		return allOrganizations;
	}

	public void setAllOrganizations(ArrayList<OrganizationBean> allOrganizations)
	{
		this.allOrganizations = allOrganizations;
	}
	
	public ArrayList<AuthenticationProfileBean> getAllAuthenticationProfiles()
	{
		return allAuthenticationProfiles;
	}

	public void setAllAuthenticationProfiles(ArrayList<AuthenticationProfileBean> allAuthenticationProfiles)
	{
		this.allAuthenticationProfiles = allAuthenticationProfiles;
	}
	
	public ArrayList<DepartmentBean> getAllDepartments()
	{
		return allDepartments;
	}

	public void setAllDepartments(ArrayList<DepartmentBean> allDepartments)
	{
		this.allDepartments = allDepartments;
	}

	public ArrayList<ApplicationBean> getAllApplications()
	{
		return allApplications;
	}

	public void setAllApplications(ArrayList<ApplicationBean> allApplications)
	{
		this.allApplications = allApplications;
	}
	
	public ArrayList<RoleBean> getAllRolesForApplication()
	{
		return allRolesForApplication;
	}

	public void setAllRolesForApplication(ArrayList<RoleBean> allRolesForApplication)
	{
		this.allRolesForApplication = allRolesForApplication;
	}

	public ArrayList<RoleBean> getAllRolesForAllApplications()
	{
		return allRolesForAllApplications;
	}

	public void setAllRolesForAllApplications(ArrayList<RoleBean> allRolesForAllApplications)
	{
		this.allRolesForAllApplications = allRolesForAllApplications;
	}

	public ArrayList<ApplicationUserDepartmentRoleBean> getAssignedRolesForUser()
	{
		return assignedRolesForUser;
	}

	public void setAssignedRolesForUser(ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser)
	{
		this.assignedRolesForUser = assignedRolesForUser;
	}

	public ApplicationUserDepartmentRoleBean getActiveRoleAssignment()
	{
		return activeRoleAssignment;
	}

	public void setActiveRoleAssignment(ApplicationUserDepartmentRoleBean activeRoleAssignment)
	{
		this.activeRoleAssignment = activeRoleAssignment;
	}

	public OrganizationBean getActiveOrganization()
	{
		return activeOrganization;
	}

	public void setActiveOrganization(OrganizationBean activeOrganization)
	{
		this.activeOrganization = activeOrganization;
	}
	
	public AuthenticationProfileBean getActiveAuthenticationProfile()
	{
		return activeAuthenticationProfile;
	}

	public void setActiveAuthenticationProfile(AuthenticationProfileBean activeAuthenticationProfile)
	{
		this.activeAuthenticationProfile = activeAuthenticationProfile;
	}

	public DepartmentBean getActiveDepartment()
	{
		return activeDepartment;
	}

	public void setActiveDepartment(DepartmentBean activeDepartment)
	{
		this.activeDepartment = activeDepartment;
	}

	public AuthenticationProfileBean getLoginAuthenticationProfile()
	{
		return loginAuthenticationProfile;
	}

	public void setLoginAuthenticationProfile(AuthenticationProfileBean loginAuthenticationProfile)
	{
		this.loginAuthenticationProfile = loginAuthenticationProfile;
	}

	public ApplicationBean getActiveApplication()
	{
		return activeApplication;
	}

	public void setActiveApplication(ApplicationBean activeApplication)
	{
		this.activeApplication = activeApplication;
	}

	public RoleBean getActiveRole()
	{
		return activeRole;
	}

	public void setActiveRole(RoleBean activeRole)
	{
		this.activeRole = activeRole;
	}

	public VerificationCodeBean getActiveVerificationCode()
	{
		return activeVerificationCode;
	}

	public void setActiveVerificationCode(VerificationCodeBean activeVerificationCode)
	{
		this.activeVerificationCode = activeVerificationCode;
	}

	public String getFunction()
	{
		return function;
	}

	public void setFunction(String function)
	{
		this.function = function;
	}

	public int getRestrictedOrganizationId()
	{
		return restrictedOrganizationId;
	}

	public void setRestrictedOrganizationId(int restrictedOrganizationId)
	{
		this.restrictedOrganizationId = restrictedOrganizationId;
	}
}
