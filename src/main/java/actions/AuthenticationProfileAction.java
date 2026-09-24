package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.AuthenticationProfileBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationProfileAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/authenticationProfile.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
		
		// First time getting here from the menu.
		if ( user.getRestrictedOrganizationId() > 0 )  // if this is a client organization
			if ( user.getActiveAuthenticationProfile().getOrganizationId() == 0 )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
						user.getActiveAuthenticationProfile().setOrganizationId( user.getRestrictedOrganizationId() );
						user.getAllAuthenticationProfiles().clear();
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveAuthenticationProfile().getOrganizationId(), connection );
						user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		String buttonPressed =  request.getParameter( "AuthenticationProfileAction" );
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
						user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
						user.getActiveAuthenticationProfile().setOrganizationId( organizationIdInt );
						user.getAllAuthenticationProfiles().clear();
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveAuthenticationProfile().getOrganizationId(), connection );
						user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
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
		if ( "addAuthenticationProfile".equalsIgnoreCase( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				String organizationId = request.getParameter( "organizationId" );
				int organizationIdInt = 0;
				if ( organizationId == null ) // User has no select window.
				{
					organizationIdInt = user.getLoginAuthenticationProfile().getOrganizationId();
					user.getActiveAuthenticationProfile().setOrganizationId( organizationIdInt );
				}
				else
				if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
				{
					organizationIdInt = Integer.parseInt( organizationId );
					user.getActiveAuthenticationProfile().setOrganizationId( organizationIdInt );
				}
				user.getActiveAuthenticationProfile().setUserId( request.getParameter( "userId" ) );
				user.getActiveAuthenticationProfile().setTextPassword( request.getParameter( "textPassword" ) );
				user.getActiveAuthenticationProfile().setFirstName( request.getParameter( "firstName" ) );
				user.getActiveAuthenticationProfile().setLastName( request.getParameter( "lastName" ) );
				user.getActiveAuthenticationProfile().setMobilePhone( request.getParameter( "mobilePhone" ) );
				user.getActiveAuthenticationProfile().setOfficePhone( request.getParameter( "officePhone" ) );
				user.getActiveAuthenticationProfile().setOfficePhoneExt( request.getParameter( "officePhoneExt" ) );
				user.getActiveAuthenticationProfile().setHomePhone( request.getParameter( "homePhone" ) );
				String failedLoginAttemptsStr = request.getParameter( "failedLoginAttempts" );
				if ( SecurityUtilities.isEmpty( failedLoginAttemptsStr ) )
					failedLoginAttemptsStr = "0";
				int failedLoginAttempts = Integer.parseInt( failedLoginAttemptsStr );
				user.getActiveAuthenticationProfile().setFailedLoginAttempts( failedLoginAttempts );
				user.getActiveAuthenticationProfile().setVerificationCodeMethod( request.getParameter( "verificationCodeMethod" ) );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateAuthenticationProfile( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						// Update the bean with secure password and salt.
						SecurityUtilities.createSecurePasswordAndSalt( user.getActiveAuthenticationProfile() );
						SecurityUtilities.addNewAuthenticationProfile( user, connection );
						user.getAllAuthenticationProfiles().clear();
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveAuthenticationProfile().getOrganizationId(), connection );
						user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
						int saveOrganizationId = user.getActiveAuthenticationProfile().getOrganizationId();
						user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
						user.getActiveAuthenticationProfile().setOrganizationId( saveOrganizationId );
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
			// Parse the authenticationProfileId
			String authenticationProfileIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int authenticationProfileId = Integer.parseInt( authenticationProfileIdStr );
			// Get and set active authentication profile.
			AuthenticationProfileBean activeAuthenticationProfile = SecurityUtilities.getAuthenticationProfile( authenticationProfileId, user.getAllAuthenticationProfiles() );
			user.setActiveAuthenticationProfile( activeAuthenticationProfile );
			user.setFunction( "deleteAuthenticationProfile" );
			destinationPage = "/JSP/authenticationProfileEdit.jsp";
			
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
					SecurityUtilities.deleteAuthenticationProfile( user.getActiveAuthenticationProfile().getAuthenticationProfileId(), connection );
					user.getAllAuthenticationProfiles().clear();
					ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveAuthenticationProfile().getOrganizationId(), connection );
					user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
					int saveOrganizationId = user.getActiveAuthenticationProfile().getOrganizationId();
					user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
					user.getActiveAuthenticationProfile().setOrganizationId( saveOrganizationId );
					connection.close();
				}
			}
			catch( Exception e )
			{
				user.getErrors().add( "Database integrity violation.  Child rows are referencing this row." );
				int saveOrganizationId = user.getActiveAuthenticationProfile().getOrganizationId();
				user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
				user.getActiveAuthenticationProfile().setOrganizationId( saveOrganizationId );
				e.printStackTrace();
			}
			destinationPage = "/JSP/authenticationProfile.jsp";
		}
		else
		if ( "cancelEditDelete".equals( buttonPressed ) )
		{
			int saveOrganizationId = user.getActiveAuthenticationProfile().getOrganizationId();
			user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
			user.getActiveAuthenticationProfile().setOrganizationId( saveOrganizationId );
			destinationPage = "/JSP/authenticationProfile.jsp";
		}
		else
		if ( buttonPressed.contains( "edit_" ) )
		{
			// Parse the authenticatinProfileId
			String authenticationProfileIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int authenticationProfileId = Integer.parseInt( authenticationProfileIdStr );
			// Get and set active authentication profile.
			AuthenticationProfileBean activeAuthenticationProfile = SecurityUtilities.getAuthenticationProfile( authenticationProfileId, user.getAllAuthenticationProfiles() );
			user.setActiveAuthenticationProfile( activeAuthenticationProfile );
			user.setFunction( "editAuthenticationProfile" );
			destinationPage = "/JSP/authenticationProfileEdit.jsp";
		}
		else
		if ( "saveEdit".equals( buttonPressed ) )
		{
			destinationPage = "authenticationProfile.jsp";

			user.getActiveAuthenticationProfile().setUserId( request.getParameter( "userId" ) );
			user.getActiveAuthenticationProfile().setTextPassword( request.getParameter( "textPassword" ) );
			user.getActiveAuthenticationProfile().setFirstName( request.getParameter( "firstName" ) );
			user.getActiveAuthenticationProfile().setLastName( request.getParameter( "lastName" ) );
			user.getActiveAuthenticationProfile().setMobilePhone( request.getParameter( "mobilePhone" ) );
			user.getActiveAuthenticationProfile().setOfficePhone( request.getParameter( "officePhone" ) );
			user.getActiveAuthenticationProfile().setOfficePhoneExt( request.getParameter( "officePhoneExt" ) );
			user.getActiveAuthenticationProfile().setHomePhone( request.getParameter( "homePhone" ) );
			int failedLoginAttemptsInt = 0;
			try
			{
				failedLoginAttemptsInt = Integer.parseInt( request.getParameter( "failedLoginAttempts" ) );
			}
			catch( Exception e )
			{
				failedLoginAttemptsInt = 0;
			}
			user.getActiveAuthenticationProfile().setFailedLoginAttempts( failedLoginAttemptsInt );
			user.getActiveAuthenticationProfile().setVerificationCodeMethod( request.getParameter( "verificationCodeMethod" ) );

			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.validateAuthenticationProfile( user, connection );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.updateAuthenticationProfile( user.getActiveAuthenticationProfile(), connection );
						user.getAllAuthenticationProfiles().clear();
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( user.getActiveAuthenticationProfile().getOrganizationId(), connection );
						user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
						int saveOrganizationId = user.getActiveAuthenticationProfile().getOrganizationId();
						user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
						user.getActiveAuthenticationProfile().setOrganizationId( saveOrganizationId );
						destinationPage = "/JSP/authenticationProfile.jsp";
					}
					else
						destinationPage = "/JSP/authenticationProfileEdit.jsp";
						
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
