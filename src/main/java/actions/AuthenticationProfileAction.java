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
		System.out.println( "Hit AuthenticationProfileAction" );
		String destinationPage = "/JSP/authenticationProfile.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		String buttonPressed =  request.getParameter( "AuthenticationProfileAction" );
		System.out.println( "NEW button pressed is " + buttonPressed );
		if ( "addAuthenticationProfile".equalsIgnoreCase( buttonPressed ) )
		{
			AuthenticationProfileBean authentication = new AuthenticationProfileBean();
			System.out.println( "Save the new authentication profile here." );
			Connection connection = null;
			try
			{
				System.out.println( "WORK SO FAR." );

				String organizationId = request.getParameter( "organizationId" );
				int organizationIdInt = 0;
				if ( ! "SELECT".equalsIgnoreCase( organizationId ) )
					organizationIdInt = Integer.parseInt( organizationId );
				authentication.setOrganizationId( organizationIdInt );
				String userId = request.getParameter( "userId" );
				authentication.setUserId( userId );
				String textPassword = request.getParameter( "password" );
				authentication.setTextPassword( textPassword );
				// Update the bean with secure password and salt.
				SecurityUtilities.createSecurePasswordAndSalt( authentication, textPassword );
				String firstName = request.getParameter( "firstName" );
				authentication.setFirstName( firstName );
				String lastName = request.getParameter( "lastName" );
				authentication.setLastName( lastName );
				String mobilePhone = request.getParameter( "mobilePhone" );
				authentication.setMobilePhone( mobilePhone );
				String officePhone = request.getParameter( "officePhone" );
				authentication.setOfficePhone( officePhone );
				String officePhoneExt = request.getParameter( "officePhoneExt" );
				authentication.setOfficePhoneExt( officePhoneExt );
				String homePhone = request.getParameter( "homePhone" );
				authentication.setHomePhone( homePhone );
				String failedLoginAttemptsStr = request.getParameter( "failedLoginAttempts" );
				if ( SecurityUtilities.isEmpty( failedLoginAttemptsStr ) )
					failedLoginAttemptsStr = "0";
				int failedLoginAttempts = Integer.parseInt( failedLoginAttemptsStr );
				authentication.setFailedLoginAttempts( failedLoginAttempts );
				String verificationCodeMethod = request.getParameter( "verificationCodeMethod" );
				authentication.setVerificationCodeMethod( verificationCodeMethod );
				user.setActiveAuthenticationProfile( authentication );
				
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					user.getErrors().addAll( SecurityUtilities.validateAuthenticationProfile( authentication, user, connection ) );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.addNewAuthenticationProfile( user, connection );
						user.getAllAuthenticationProfiles().clear();
						ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( connection );
						user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
						user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
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
					ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( connection );
					user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
					user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			destinationPage = "/JSP/authenticationProfile.jsp";
		}
		else
		if ( "cancelEditDelete".equals( buttonPressed ) )
		{
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					user.getAllAuthenticationProfiles().clear();
					ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = SecurityUtilities.getAllAuthenticationProfiles( connection );
					user.getAllAuthenticationProfiles().addAll( allAuthenticationProfiles );
					user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			user.setActiveAuthenticationProfile( new AuthenticationProfileBean() );
			destinationPage = "/JSP/authenticationProfile.jsp";
		}
//		else
//		if ( buttonPressed.contains( "edit_" ) )
//		{
//			// Parse the authenticatinProfileId
//			String authenticationProfileIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
//			int authenticationProfileId = Integer.parseInt( authenticationProfileIdStr );
//			// Get and set active authentication profile.
//			AuthenticationProfileBean activeAuthenticationProfile = SecurityUtilities.getAuthenticationProfile( authenticationProfileId, user.getAllAuthenticationProfiles() );
//			user.setActiveAuthenticationProfile( activeAuthenticationProfile );
//			user.setFunction( "editAuthenticationProfile" );
//			destinationPage = "/JSP/authenticationProfileEdit.jsp";
//		}
//		else
//		if ( "saveEdit".equals( buttonPressed ) )
//		{
//			destinationPage = "/JSP/organization.jsp";
//			System.out.println( "Save the modified authentication profile here." );
//			AuthenticationProfileBean authenticationProfile = new AuthenticationProfileBean();
//			SecurityUtilities.loadAuthenticationProfile( request, authenticationProfile );
//			Connection connection = null;
//			try
//			{
//				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
//				if ( connection != null )
//				{
//					user.getErrors().addAll( SecurityUtilities.validateOrganization( user.getActiveOrganization(), connection ) );
//					if ( user.getErrors().size() == 0 )
//					{
//						SecurityUtilities.updateOrganization( user.getActiveOrganization(), connection );
//						user.getAllOrganizations().clear();
//						ArrayList<OrganizationBean> allOrganizations = SecurityUtilities.getAllOrganizations( connection );
//						user.getAllOrganizations().addAll( allOrganizations );
//						user.setActiveOrganization( new OrganizationBean() );
//						destinationPage = "/JSP/organization.jsp";
//					}
//					else
//						destinationPage = "/JSP/organizationEdit.jsp";
//						
//					connection.close();
//				}
//			}
//			catch( Exception e )
//			{
//				e.printStackTrace();
//			}
//		}
	
		this.view( request, response, destinationPage );
	}
}
