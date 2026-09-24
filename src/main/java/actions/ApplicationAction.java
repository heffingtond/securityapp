package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.ApplicationBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApplicationAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/application.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		// First time getting here from the menu.
		if ( user.getRestrictedOrganizationId() > 0 )  // if this is a client organization
			if ( user.getActiveApplication().getOrganizationId() == 0 )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.setActiveApplication( new ApplicationBean() );
						user.getActiveApplication().setOrganizationId( user.getRestrictedOrganizationId() );
						user.getAllApplications().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveApplication().getOrganizationId(), connection );
						user.getAllApplications().addAll( allApplications );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		String buttonPressed =  request.getParameter( "ApplicationAction" );
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
						user.setActiveApplication( new ApplicationBean() );
						user.getActiveApplication().setOrganizationId( organizationIdInt );
						user.getAllApplications().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveApplication().getOrganizationId(), connection );
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
		if ( "addApplication".equalsIgnoreCase( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				String organizationId = request.getParameter( "organizationId" );
				int organizationIdInt = 0;
				if ( organizationId == null ) // User has no select window.
				{
					organizationIdInt = user.getLoginAuthenticationProfile().getOrganizationId();
					user.getActiveApplication().setOrganizationId( organizationIdInt );
				}
				else
				if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
				{
					organizationIdInt = Integer.parseInt( organizationId );
					user.getActiveApplication().setOrganizationId( organizationIdInt );
				}
				user.getActiveApplication().setApplicationName( request.getParameter( "applicationName" ) );
				user.getActiveApplication().setApplicationCode( request.getParameter( "applicationCode" ) );
				user.getActiveApplication().setApplicationDescription( request.getParameter( "applicationDescription" ) );
				user.getActiveApplication().setDomainName( request.getParameter( "domainName" ) );
				user.getActiveApplication().setOwner( request.getParameter( "owner" ) );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateApplication( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						// Update the bean with secure password and salt.
						SecurityUtilities.addNewApplication( user, connection );
						user.getAllApplications().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveApplication().getOrganizationId(), connection );
						user.getAllApplications().addAll( allApplications );
						int saveOrganizationId = user.getActiveApplication().getOrganizationId();
						user.setActiveApplication( new ApplicationBean() );
						user.getActiveApplication().setOrganizationId( saveOrganizationId );
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
			// Parse the applicationId
			String applicationIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int applicationId = Integer.parseInt( applicationIdStr );
			// Get and set active application.
			ApplicationBean activeApplication = SecurityUtilities.getApplication( applicationId, user.getAllApplications() );
			user.setActiveApplication( activeApplication );
			user.setFunction( "deleteApplication" );
			destinationPage = "/JSP/applicationEdit.jsp";
			
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
					SecurityUtilities.deleteApplication( user.getActiveApplication().getApplicationId(), connection );
					user.getAllApplications().clear();
					ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveApplication().getOrganizationId(), connection );
					user.getAllApplications().addAll( allApplications );
					int saveOrganizationId = user.getActiveApplication().getOrganizationId();
					user.setActiveApplication( new ApplicationBean() );
					user.getActiveApplication().setOrganizationId( saveOrganizationId );
					connection.close();
				}
			}
			catch( Exception e )
			{
				user.getErrors().add( "Database integrity violation.  Child rows are referencing this row." );
				int saveOrganizationId = user.getActiveApplication().getOrganizationId();
				user.setActiveApplication( new ApplicationBean() );
				user.getActiveApplication().setOrganizationId( saveOrganizationId );
				e.printStackTrace();
			}
			destinationPage = "/JSP/application.jsp";
		}
		else
		if ( "cancelEditDelete".equals( buttonPressed ) )
		{
			int saveOrganizationId = user.getActiveApplication().getOrganizationId();
			user.setActiveApplication( new ApplicationBean() );
			user.getActiveApplication().setOrganizationId( saveOrganizationId );
			destinationPage = "/JSP/application.jsp";
		}
		else
		if ( buttonPressed.contains( "edit_" ) )
		{
			// Parse the applicationId
			String applicationIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int applicationId = Integer.parseInt( applicationIdStr );
			// Get and set active application.
			ApplicationBean activeApplication = SecurityUtilities.getApplication( applicationId, user.getAllApplications() );
			user.setActiveApplication( activeApplication );
			user.setFunction( "editApplication" );
			destinationPage = "/JSP/applicationEdit.jsp";
		}
		else
		if ( "saveEdit".equals( buttonPressed ) )
		{
			destinationPage = "application.jsp";
			
			user.getActiveApplication().setApplicationName( request.getParameter( "applicationName" ) );
			user.getActiveApplication().setApplicationCode( request.getParameter( "applicationCode" ) );
			user.getActiveApplication().setApplicationDescription( request.getParameter( "applicationDescription" ) );
			user.getActiveApplication().setDomainName( request.getParameter( "domainName" ) );
			user.getActiveApplication().setOwner( request.getParameter( "owner" ) );

			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{ 
					SecurityUtilities.validateApplication( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.updateApplication( user.getActiveApplication(), connection );
						user.getAllApplications().clear();
						ArrayList<ApplicationBean> allApplications = SecurityUtilities.getAllApplications( user.getActiveApplication().getOrganizationId(), connection );
						user.getAllApplications().addAll( allApplications );
						int saveOrganizationId = user.getActiveApplication().getOrganizationId();
						user.setActiveApplication( new ApplicationBean() );
						user.getActiveApplication().setOrganizationId( saveOrganizationId );
						destinationPage = "/JSP/application.jsp";
					}
					else
						destinationPage = "/JSP/applicationEdit.jsp";
						
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
