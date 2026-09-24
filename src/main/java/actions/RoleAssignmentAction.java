package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.ApplicationBean;
import beans.ApplicationUserDepartmentRoleBean;
import beans.AuthenticationProfileBean;
import beans.DepartmentBean;
import beans.RoleBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RoleAssignmentAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/roleAssignment.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		// First time getting here from the menu.
		if ( user.getRestrictedOrganizationId() > 0 )  // if this is a client organization
			if ( user.getActiveRoleAssignment().getOrganizationId() == 0 )
			{
				user.setActiveRoleAssignment( new ApplicationUserDepartmentRoleBean() );
				user.getActiveRoleAssignment().setOrganizationId( user.getRestrictedOrganizationId() );
				user.getAssignedRolesForUser().clear();
				try
				{
					Connection connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveRoleAssignment().getOrganizationId(), 
								   																										 connection );	
						user.setAllAuthenticationProfiles( allAuthenticationProfiles );
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveRoleAssignment().getOrganizationId(), 
									 																	   connection );
						// The Watchtower application needs to be available for all organizations, not just the Watchtower organization.
						
						String selectedOrganizationName = SecurityUtilities.getOrganization( user.getActiveRoleAssignment().getOrganizationId(),  user.getAllOrganizations() ).getOrganizationName();
						System.out.println( "ORG Name selected is " +  selectedOrganizationName );
						if ( ! "Watchtower".equalsIgnoreCase( selectedOrganizationName ) )
						{
							System.out.println( "Watchtower IS NOT THE ORG.  Add watchtower to the app list" );
							ApplicationBean watchtower = SecurityUtilities.getApplication( "SECURITY_APP", connection);
							if ( watchtower != null )
							{
								System.out.println( "Watchtower is not null" );
								allApplications.add( watchtower );
							}
						}
						user.setAllApplications( allApplications );
						user.setFunction( "selectOrganization" );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		String buttonPressed =  request.getParameter( "RoleAssignmentAction" );
		if ( "selectOrganization".equalsIgnoreCase( buttonPressed ) )
		{
			String organizationId = request.getParameter( "organizationId" );
			int organizationIdInt = 0;
			if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
			{
				organizationIdInt = Integer.parseInt( organizationId );
				user.setActiveRoleAssignment( new ApplicationUserDepartmentRoleBean() );
				user.getActiveRoleAssignment().setOrganizationId( organizationIdInt );
				user.getAssignedRolesForUser().clear();
				try
				{
					Connection connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveRoleAssignment().getOrganizationId(), 
								   																										 connection );	
						user.setAllAuthenticationProfiles( allAuthenticationProfiles );
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveRoleAssignment().getOrganizationId(), 
									 																	   connection );
						// The Watchtower application needs to be available for all organizations, not just the Watchtower organization.
						
						String selectedOrganizationName = SecurityUtilities.getOrganization( user.getActiveRoleAssignment().getOrganizationId(),  user.getAllOrganizations() ).getOrganizationName();
						System.out.println( "ORG Name selected is " +  selectedOrganizationName );
						if ( ! "Watchtower".equalsIgnoreCase( selectedOrganizationName ) )
						{
							System.out.println( "Watchtower IS NOT THE ORG.  Add watchtower to the app list" );
							ApplicationBean watchtower = SecurityUtilities.getApplication( "SECURITY_APP", connection);
							if ( watchtower != null )
							{
								System.out.println( "Watchtower is not null" );
								allApplications.add( watchtower );
							}
						}
						user.setAllApplications( allApplications );
						user.setFunction( "selectOrganization" );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		if ( "selectUser".equalsIgnoreCase( buttonPressed ) )
		{
			// Parse the authenticationProfileId   
  			String authenticationProfileIdStr = request.getParameter( "authenticationProfileId" );
  			int authenticationProfileId = 0;
  			if ( ! "SELECT".equalsIgnoreCase( authenticationProfileIdStr ) )
  				authenticationProfileId = Integer.parseInt( authenticationProfileIdStr );
  			else
  				user.getErrors().add( "Please select a user to continue." );
  			
  			if ( user.getErrors().size() == 0 )
  			{
				user.getActiveRoleAssignment().setAuthenticationProfileId( authenticationProfileId );
				user.getActiveRoleAssignment().setApplicationId( 0 );
				user.getActiveRoleAssignment().setDepartmentId( 0 );
				user.getActiveRoleAssignment().setRoleId( 0 );
				user.getAssignedRolesForUser().clear();

				user.setFunction( "selectUser" );
  			}
			destinationPage = "/JSP/roleAssignment.jsp";
		}
		else
		if ( "selectApplication".equalsIgnoreCase( buttonPressed ) )
		{
			// Parse the applicationId   
  			String applicationIdStr = request.getParameter( "applicationId" );
  			int applicationId = 0;
  			if ( ! "SELECT".equalsIgnoreCase( applicationIdStr ) )
  				applicationId = Integer.parseInt( applicationIdStr );
  			else
  				user.getErrors().add( "Please select an application to continue." );
  			
  			if ( user.getErrors().size() == 0 )
  			{
				user.getActiveRoleAssignment().setApplicationId( applicationId );
				try
				{
					Connection connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.getAllRolesForApplication().clear();
						ArrayList<RoleBean> allRoles = SecurityUtilities.getAllRolesForApplication( user.getActiveRoleAssignment().getApplicationId(), 
																									user.getAllRolesForAllApplications() );	
						// If the organization is not Watchtower, remove the 'SECURITY_MASTER' role because the only role available to other organizations
						// is the 'SECURITY_ORGANIZATION' role.
						String selectedOrganizationName = SecurityUtilities.getOrganization( user.getActiveRoleAssignment().getOrganizationId(),  user.getAllOrganizations() ).getOrganizationName();
						System.out.println( "ORG Name selected WHEN SELECT APP is " +  selectedOrganizationName );
						if ( ! "Watchtower".equalsIgnoreCase( selectedOrganizationName ) )
						{
							for( RoleBean role : allRoles )
								if ( "SECURITY_MASTER".equals( role.getRoleName() ) )
								{
									System.out.println( "Removing SECURITY_MASTER role" );
									allRoles.remove( role );
								}
						}

						user.getAllRolesForApplication().addAll( allRoles );
						
						user.getAllDepartments().clear();
						ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments( user.getActiveRoleAssignment().getOrganizationId(), 
					                																	connection );	
						user.getAllDepartments().addAll( allDepartments );
						user.getAssignedRolesForUser().clear();
						ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser = SecurityUtilities.getAssignedRolesForUser( user.getActiveRoleAssignment().getAuthenticationProfileId(), 
								   																									   connection);	
						user.getAssignedRolesForUser().addAll( assignedRolesForUser );
						connection.close();
						user.setFunction( "selectApplication" );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
  			}
			destinationPage = "/JSP/roleAssignment.jsp";
		}
		else
		if ( "addRoleAssignment".equalsIgnoreCase( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				String authenticationProfileId = request.getParameter( "authenticationProfileId" );
				int authenticationProfileIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( authenticationProfileId ) )
					authenticationProfileIdInt = Integer.parseInt( authenticationProfileId );

				String applicationId = request.getParameter( "applicationId" );
				int applicationIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( applicationId ) )
					applicationIdInt = Integer.parseInt( applicationId );

				String departmentId = request.getParameter( "departmentId" );
				int departmentIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( departmentId ) )
					departmentIdInt = Integer.parseInt( departmentId );

				String roleId = request.getParameter( "roleId" );
				int roleIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( roleId ) )
					roleIdInt = Integer.parseInt( roleId );

				user.getActiveRoleAssignment().setAuthenticationProfileId( authenticationProfileIdInt );
				user.getActiveRoleAssignment().setApplicationId( applicationIdInt );
				user.getActiveRoleAssignment().setDepartmentId( departmentIdInt );
				user.getActiveRoleAssignment().setRoleId( roleIdInt );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateRoleAssignment( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.addNewRoleAssignment( user, connection );
						user.getAssignedRolesForUser().clear();
						ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser = SecurityUtilities.getAssignedRolesForUser( user.getActiveRoleAssignment().getAuthenticationProfileId(), 
																																	   connection);	
						user.getAssignedRolesForUser().addAll( assignedRolesForUser );
					}
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			destinationPage = "/JSP/roleAssignment.jsp";
		}
		else
		if ( buttonPressed.contains( "delete_" ) )
		{
			// Parse the applicationUserDepartmentRoleId
			String applicationUserDepartmentRoleIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int applicationUserDepartmentRoleId = Integer.parseInt( applicationUserDepartmentRoleIdStr );
			// Get and set active role.
			int saveOrganizationId = user.getActiveRoleAssignment().getOrganizationId();
			ApplicationUserDepartmentRoleBean activeRoleAssignment = SecurityUtilities.getAssignedRoleForUser( applicationUserDepartmentRoleId, user.getAssignedRolesForUser() );
			user.setActiveRoleAssignment( activeRoleAssignment );
			user.getActiveRoleAssignment().setOrganizationId( saveOrganizationId );
			user.setFunction( "deleteRole" );
			
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.deleteRoleAssignment( user.getActiveRoleAssignment().getApplicationUserDepartmentRoleId(), connection );
					user.getAssignedRolesForUser().clear();
					ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser = SecurityUtilities.getAssignedRolesForUser( user.getActiveRoleAssignment().getAuthenticationProfileId(), 
																																   connection );
					user.getAssignedRolesForUser().addAll( assignedRolesForUser );
					connection.close();
				}
			}
			catch( Exception e )
			{
				user.getErrors().add( "Database integrity violation.  Child rows are referencing this row." );
				e.printStackTrace();
			}
			destinationPage = "/JSP/roleAssignment.jsp";
		}
	
		this.view( request, response, destinationPage );
	}
}
