package beans;

import java.util.ArrayList;

public class UserBean
{
	private String userName;
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<OrganizationBean> allOrganizations = new ArrayList<OrganizationBean>();
	private OrganizationBean activeOrganization = new OrganizationBean();
	
	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
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

	public OrganizationBean getActiveOrganization()
	{
		return activeOrganization;
	}

	public void setActiveOrganization(OrganizationBean activeOrganization)
	{
		this.activeOrganization = activeOrganization;
	}
	
	
}
