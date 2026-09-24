package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.ApplicationBean;
import beans.DepartmentBean;
import beans.RoleBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RoleAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/role.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		// First time getting here from the menu.
		if ( user.getRestrictedOrganizationId() > 0 )  // if this is a client organization
			if ( user.getActiveRole().getOrganizationId() == 0 )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.setActiveRole( new RoleBean() );
						user.getActiveRole().setOrganizationId( user.getRestrictedOrganizationId() );
						user.getAllApplications().clear();
						user.getAllRolesForApplication().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveRole().getOrganizationId(), connection );
						user.getAllApplications().addAll( allApplications );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		String buttonPressed =  request.getParameter( "RoleAction" );
		if ( "selectOrganization".equalsIgnoreCase( buttonPressed ) )
		{
			String organizationId = request.getParameter( "organizationId" );
			int organizationIdInt = 0;
			if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						organizationIdInt = Integer.parseInt( organizationId );
						user.setActiveRole( new RoleBean() );
						user.getActiveRole().setOrganizationId( organizationIdInt );
						user.getAllApplications().clear();
						user.getAllRolesForApplication().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveRole().getOrganizationId(), connection );
						user.getAllApplications().addAll( allApplications );
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
		if ( "selectApplication".equalsIgnoreCase( buttonPressed ) )
		{
			if ( user.getActiveRole().getOrganizationId() == 0 )
			{
				user.getErrors().add( "Please select the organization first." );
			}
			else
			{
				String applicationId = request.getParameter( "applicationId" );
				int applicationIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( applicationId ) )
				{
					Connection connection = null;
					try
					{
						connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
						if ( connection != null )
						{
							applicationIdInt = Integer.parseInt( applicationId );
							user.getActiveRole().setApplicationId( applicationIdInt );
							
							user.getAllRolesForAllApplications().clear();
							ArrayList<RoleBean> allRolesForAllApplications = SecurityUtilities.getAllRolesForAllApplications( connection );
							user.getAllRolesForAllApplications().addAll( allRolesForAllApplications );

							user.getAllRolesForApplication().clear();
							ArrayList<RoleBean> allRolesForApplication = SecurityUtilities.getAllRolesForApplication( user.getActiveRole().getApplicationId(), user.getAllRolesForAllApplications() );
							user.getAllRolesForApplication().addAll( allRolesForApplication );

							connection.close();
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		if ( "addRole".equalsIgnoreCase( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				String applicationId = request.getParameter( "applicationId" );
				int applicationIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( applicationId ) )
					applicationIdInt = Integer.parseInt( applicationId );
				user.getActiveRole().setApplicationId( applicationIdInt );
				user.getActiveRole().setRoleName( request.getParameter( "roleName" ) );
				user.getActiveRole().setRoleDescription( request.getParameter( "roleDescription" ) );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateRole( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.addNewRole( user, connection );

						user.getAllRolesForAllApplications().clear();
						ArrayList<RoleBean> allRolesForAllApplications = SecurityUtilities.getAllRolesForAllApplications( connection );
						user.getAllRolesForAllApplications().addAll( allRolesForAllApplications );

						user.getAllRolesForApplication().clear();
						ArrayList<RoleBean> allRolesForApplication = SecurityUtilities.getAllRolesForApplication( user.getActiveRole().getApplicationId(), user.getAllRolesForAllApplications() );
						user.getAllRolesForApplication().addAll( allRolesForApplication );

						int saveOrganizationId = user.getActiveRole().getOrganizationId();
						int saveApplicationId = user.getActiveRole().getApplicationId();
						user.setActiveRole( new RoleBean() );
						user.getActiveRole().setOrganizationId( saveOrganizationId );
						user.getActiveRole().setApplicationId( saveApplicationId );
					}
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}

		}
		else
		if ( buttonPressed.contains( "delete_" ) )
		{
			// Parse the roleId
			String roleIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int roleId = Integer.parseInt( roleIdStr );
			// Get and set active role.
			RoleBean activeRole = SecurityUtilities.getRole( roleId, user.getAllRolesForApplication() );
			// Save the organizationId since not in the table.
			activeRole.setOrganizationId( user.getActiveRole().getOrganizationId() );
			user.setActiveRole( activeRole );
			user.setFunction( "deleteRole" );
			destinationPage = "/JSP/roleEdit.jsp";
			
		}
		else
		if ( "confirmDelete".equals( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.deleteRole( user.getActiveRole().getRoleId(), connection );

					user.getAllRolesForAllApplications().clear();
					ArrayList<RoleBean> allRolesForAllApplications = SecurityUtilities.getAllRolesForAllApplications( connection );
					user.getAllRolesForAllApplications().addAll( allRolesForAllApplications );

					user.getAllRolesForApplication().clear();
					ArrayList<RoleBean> allRolesForApplication = SecurityUtilities.getAllRolesForApplication( user.getActiveRole().getApplicationId(), user.getAllRolesForAllApplications() );
					user.getAllRolesForApplication().addAll( allRolesForApplication );

					int saveOrganizationId = user.getActiveRole().getOrganizationId();
					int saveApplicationId = user.getActiveRole().getApplicationId();
					user.setActiveRole( new RoleBean() );
					user.getActiveRole().setOrganizationId( saveOrganizationId );
					user.getActiveRole().setApplicationId( saveApplicationId );
					
					connection.close();
				}
			}
			catch( Exception e )
			{
				user.getErrors().add( "Database integrity violation.  Child rows are referencing this row." );
				int saveOrganizationId = user.getActiveRole().getOrganizationId();
				int saveApplicationId = user.getActiveRole().getApplicationId();
				user.setActiveRole( new RoleBean() );
				user.getActiveRole().setOrganizationId( saveOrganizationId );
				user.getActiveRole().setApplicationId( saveApplicationId );
				e.printStackTrace();
			}
			destinationPage = "/JSP/role.jsp";
		}
		else
		if ( "cancelEditDelete".equals( buttonPressed ) )
		{
			int saveOrganizationId = user.getActiveRole().getOrganizationId();
			int saveApplicationId = user.getActiveRole().getApplicationId();
			user.setActiveRole( new RoleBean() );
			user.getActiveRole().setOrganizationId( saveOrganizationId );
			user.getActiveRole().setApplicationId( saveApplicationId );
			destinationPage = "/JSP/role.jsp";
		}
		else
		if ( buttonPressed.contains( "edit_" ) )
		{
			// Parse the roleId
			String roleIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int roleId = Integer.parseInt( roleIdStr );
			// Get and set active role.
			RoleBean activeRole = SecurityUtilities.getRole( roleId, user.getAllRolesForApplication() );
			// Save the organizationId since not in the table.
			activeRole.setOrganizationId( user.getActiveRole().getOrganizationId() );
			activeRole.setApplicationId( user.getActiveRole().getApplicationId() );
			user.setActiveRole( activeRole );
			user.setFunction( "editRole" );
			destinationPage = "/JSP/roleEdit.jsp";
		}
		else
		if ( "saveEdit".equals( buttonPressed ) )
		{
			destinationPage = "role.jsp";
			
			user.getActiveRole().setRoleName( request.getParameter( "roleName" ) );
			user.getActiveRole().setRoleDescription( request.getParameter( "roleDescription" ) );

			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{ 
					SecurityUtilities.validateRole( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.updateRole( user.getActiveRole(), connection );

						user.getAllRolesForAllApplications().clear();
						ArrayList<RoleBean> allRolesForAllApplications = SecurityUtilities.getAllRolesForAllApplications( connection );
						user.getAllRolesForAllApplications().addAll( allRolesForAllApplications );

						user.getAllRolesForApplication().clear();
						ArrayList<RoleBean> allRolesForApplication = SecurityUtilities.getAllRolesForApplication( user.getActiveRole().getApplicationId(), user.getAllRolesForAllApplications() );
						user.getAllRolesForApplication().addAll( allRolesForApplication );
						
						
						int saveOrganizationId = user.getActiveRole().getOrganizationId();
						int saveApplicationId = user.getActiveRole().getApplicationId();
						user.setActiveRole( new RoleBean() );
						user.getActiveRole().setOrganizationId( saveOrganizationId );
						user.getActiveRole().setApplicationId( saveApplicationId );
						destinationPage = "/JSP/role.jsp";
					}
					else
						destinationPage = "/JSP/roleEdit.jsp";
						
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	
		this.view( request, response, destinationPage );
	}
}
