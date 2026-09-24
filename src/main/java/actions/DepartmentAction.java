package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.AuthenticationProfileBean;
import beans.DepartmentBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DepartmentAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/department.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		// First time getting here from the menu.
		if ( user.getRestrictedOrganizationId() > 0 )  // if this is a client organization
			if ( user.getActiveDepartment().getOrganizationId() == 0 )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.setActiveDepartment( new DepartmentBean() );
						user.getActiveDepartment().setOrganizationId( user.getRestrictedOrganizationId() );
						user.getAllDepartments().clear();
						ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments( user.getActiveDepartment().getOrganizationId(), connection );
						user.getAllDepartments().addAll( allDepartments );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		String buttonPressed =  request.getParameter( "DepartmentAction" );
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
						user.setActiveDepartment( new DepartmentBean() );
						user.getActiveDepartment().setOrganizationId( organizationIdInt );
						user.getAllDepartments().clear();
						ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments( user.getActiveDepartment().getOrganizationId(), connection );
						user.getAllDepartments().addAll( allDepartments );
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
		if ( "addDepartment".equalsIgnoreCase( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				String organizationId = request.getParameter( "organizationId" );
				int organizationIdInt = 0;
				if ( organizationId == null ) // User has no select window.
				{
					organizationIdInt = user.getLoginAuthenticationProfile().getOrganizationId();
					user.getActiveDepartment().setOrganizationId( organizationIdInt );
				}
				else
				if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
				{
					organizationIdInt = Integer.parseInt( organizationId );
					user.getActiveDepartment().setOrganizationId( organizationIdInt );
				}

				user.getActiveDepartment().setDepartmentName( request.getParameter( "departmentName" ) );
				user.getActiveDepartment().setDepartmentCode( request.getParameter( "departmentCode" ) );
				user.getActiveDepartment().setDepartmentDescription( request.getParameter( "departmentDescription" ) );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateDepartment( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						// Update the bean with secure password and salt.
						SecurityUtilities.addNewDepartment( user, connection );
						user.getAllDepartments().clear();
						ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments( user.getActiveDepartment().getOrganizationId(), connection );
						user.getAllDepartments().addAll( allDepartments );
						int saveOrganizationId = user.getActiveDepartment().getOrganizationId();
						user.setActiveDepartment( new DepartmentBean() );
						user.getActiveDepartment().setOrganizationId( saveOrganizationId );
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
			// Parse the departmentId
			String departmentIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int departmentId = Integer.parseInt( departmentIdStr );
			// Get and set active department.
			DepartmentBean activeDepartment = SecurityUtilities.getDepartment( departmentId, user.getAllDepartments() );
			user.setActiveDepartment( activeDepartment );
			user.setFunction( "deleteDepartment" );
			destinationPage = "/JSP/departmentEdit.jsp";
			
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
					SecurityUtilities.deleteDepartment( user.getActiveDepartment().getDepartmentId(), connection );
					user.getAllDepartments().clear();
					ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments(  user.getActiveDepartment().getOrganizationId(),connection );
					user.getAllDepartments().addAll( allDepartments );
					int saveOrganizationId = user.getActiveDepartment().getOrganizationId();
					user.setActiveDepartment( new DepartmentBean() );
					user.getActiveDepartment().setOrganizationId( saveOrganizationId );
					connection.close();
				}
			}
			catch( Exception e )
			{
				user.getErrors().add( "Database integrity violation.  Child rows are referencing this row." );
				int saveOrganizationId = user.getActiveDepartment().getOrganizationId();
				user.setActiveDepartment( new DepartmentBean() );
				user.getActiveDepartment().setOrganizationId( saveOrganizationId );
				e.printStackTrace();
			}
			destinationPage = "/JSP/department.jsp";
		}
		else
		if ( "cancelEditDelete".equals( buttonPressed ) )
		{
			int saveOrganizationId = user.getActiveDepartment().getOrganizationId();
			user.setActiveDepartment( new DepartmentBean() );
			user.getActiveDepartment().setOrganizationId( saveOrganizationId );
			destinationPage = "/JSP/department.jsp";
		}
		else
		if ( buttonPressed.contains( "edit_" ) )
		{
			// Parse the departmentId
			String departmentIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int departmentId = Integer.parseInt( departmentIdStr );
			// Get and set active department.
			DepartmentBean activeDepartment = SecurityUtilities.getDepartment( departmentId, user.getAllDepartments() );
			user.setActiveDepartment( activeDepartment );
			user.setFunction( "editDepartment" );
			destinationPage = "/JSP/departmentEdit.jsp";
		}
		else
		if ( "saveEdit".equals( buttonPressed ) )
		{
			destinationPage = "department.jsp";
			
			user.getActiveDepartment().setDepartmentName( request.getParameter( "departmentName" ) );
			user.getActiveDepartment().setDepartmentCode( request.getParameter( "departmentCode" ) );
			user.getActiveDepartment().setDepartmentDescription( request.getParameter( "departmentDescription" ) );

			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{ 
					SecurityUtilities.validateDepartment( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.updateDepartment( user.getActiveDepartment(), connection );
						user.getAllDepartments().clear();
						ArrayList<DepartmentBean> allDepartments = SecurityUtilities.getAllDepartments(  user.getActiveDepartment().getOrganizationId(),connection );
						user.getAllDepartments().addAll( allDepartments );
						int saveOrganizationId = user.getActiveDepartment().getOrganizationId();
						user.setActiveDepartment( new DepartmentBean() );
						user.getActiveDepartment().setOrganizationId( saveOrganizationId );
						destinationPage = "/JSP/department.jsp";
					}
					else
						destinationPage = "/JSP/departmentEdit.jsp";
						
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
