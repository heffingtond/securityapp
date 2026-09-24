package actions;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import beans.ApplicationBean;
import beans.OrganizationBean;
import beans.RoleBean;
import beans.UserBean;
import beans.VerificationCodeBean;
import core.Constants;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginAction extends Action
{
	private void twoFactorAuthentication( UserBean user, String twoFactorAuthMethod, Connection connection ) throws Exception
	{
		// Generate a 6 digit random code.
		String code = SecurityUtilities.generateAuthenticationCode();
		// Send authentication code to user via chosen method (email or text)
		// add the code to the verification code table.
		user.getActiveVerificationCode().setVerificationCode( code );
		
		// First set the authentication profile ID
		user.getActiveVerificationCode().setAuthenticationProfileId( user.getLoginAuthenticationProfile().getAuthenticationProfileId() );
		
		// Then, get the application ID of the app being logged into.
		ApplicationBean myApp = SecurityUtilities.getApplication( Constants.APPLICATION_CODE, connection );
		user.getActiveVerificationCode().setApplicationId( myApp.getApplicationId() );

		if ( "TEXT".equals( twoFactorAuthMethod ) )
			SecurityUtilities.sendCodeText( user );
		else
		if ( "EMAIL".equals( twoFactorAuthMethod ) )
			SecurityUtilities.sendCodeEmail( user );
		
		// Natural PK: authentication_profile_id + application id.  Duplicates are not allowed.  See if we need to remove
		// an existing code for this user/application.
		VerificationCodeBean existingCode = SecurityUtilities.getVerificationCode( user.getActiveVerificationCode().getAuthenticationProfileId(),
																				   user.getActiveVerificationCode().getApplicationId(),
																				   connection );
		if ( existingCode != null )
			SecurityUtilities.deleteVerificationCode( existingCode.getVerificationCodeId(), connection );

		SecurityUtilities.addNewVerificationCode( user, connection );
	}
	
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/login.jsp";
		String buttonPressed =  request.getParameter( "LoginAction" );
		boolean continueToOrganizationPage = false;
		if ( "Login".equalsIgnoreCase( buttonPressed ) )
		{
			HttpSession session = request.getSession( false );
			if ( session == null ) // start over
			{
				System.out.println( "Session was dead.  Getting a new one." );
				session = request.getSession();
			}
	//		session.setMaxInactiveInterval( 120 ); // 120 seconds = 2 minutes 
			session.setMaxInactiveInterval( 1800 ); // 30 minutes
			String username = request.getParameter( "username" );
			String password = request.getParameter( "password" );
			UserBean user = new UserBean();
			request.getSession().setAttribute( "UserBean", user );
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					user.getErrors().addAll( SecurityUtilities.validateLogin(  username, password ) );
	
					if ( user.getErrors().size() == 0 )
					{
						// Call a REST service to authenticate.
						// Authenticate with user name and password, 
						// return the profile is successful.  Otherwise, 
						// return [AUTHENTICATION_FAILED].
						String loginResultJson = SecurityUtilities.getSecureLogin( username, password );
						user.setLoginAuthenticationProfile( SecurityUtilities.loadAuthenticationProfileBean( loginResultJson ) );
						
						if ( ! "AUTHENTICATION SUCCESSFUL".equals( user.getLoginAuthenticationProfile().getClientResult() ) )
						{
							user.getErrors().add( "Bad username or password" );
							System.out.println( "Login error: " + user.getLoginAuthenticationProfile().getClientResult() );
						}
						else
						{
							// Get user roles here.
							String userRolesJson = SecurityUtilities.getRoles( username, Constants.APPLICATION_CODE );
							SecurityUtilities.loadAuthenticationProfileBeanWithRoles( user.getLoginAuthenticationProfile(), userRolesJson );
							boolean userHasRoleSecurity = false;
							for ( Map.Entry<String, ArrayList<String>> entry : user.getLoginAuthenticationProfile().getRolesForDepartment().entrySet() ) 
							{
								String departmentName = entry.getKey();
					            System.out.println("Department: " + departmentName );
					            for ( String roleName : entry.getValue() )
					            {
				            		if ( "SECURITY_MASTER".equals( roleName ) || ( "SECURITY_ORGANIZATION".equals( roleName ) ) )
				            			userHasRoleSecurity = true;
					            }
					        }
							if ( ! userHasRoleSecurity )
								user.getErrors().add( "You do not have the role security to log in to the Watchtower security platform." );
							
							if ( user.getErrors().size() == 0 )
							{
								// Get the 2 factor authentication method here.
								String twoFactorAuthMethod =  user.getLoginAuthenticationProfile().getVerificationCodeMethod();
								if ( twoFactorAuthMethod != null )
								{
									twoFactorAuthentication( user, twoFactorAuthMethod, connection );
									destinationPage = "/JSP/otpVerification.jsp";
								}
								else
									continueToOrganizationPage = true;
							}
						}
					}
					connection.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				if ( connection != null )
				try
				{
					connection.close();
				}
				catch ( Exception e )
				{
				}
			}
		}
		else
		if ( "Validate".equalsIgnoreCase( buttonPressed ) )
		{
			UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
			String passcode = request.getParameter( "passcode" );
			if ( passcode != null )
				passcode = passcode.trim();
			if ( user.getActiveVerificationCode().getVerificationCode().equals( passcode ) )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						SecurityUtilities.deleteVerificationCode( user.getActiveVerificationCode().getVerificationCodeId(), connection );
						user.setActiveVerificationCode( new VerificationCodeBean() );
						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				continueToOrganizationPage = true;
			}
			else
			{
				user.getErrors().add( "Verification failed.  Please try again." );
				destinationPage = "/JSP/otpVerification.jsp";
			}
		}
		else
		if ( "Request New Code".equalsIgnoreCase( buttonPressed ) )
		{
			UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
			String twoFactorAuthMethod =  user.getLoginAuthenticationProfile().getVerificationCodeMethod();
			if ( twoFactorAuthMethod != null )
			{
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.setActiveVerificationCode( new VerificationCodeBean() );
						twoFactorAuthentication( user, twoFactorAuthMethod, connection );
						connection.close();
						request.setAttribute( "userMessage", "New code sent." );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			destinationPage = "/JSP/otpVerification.jsp";
		
		}
		if ( continueToOrganizationPage )
		{
			UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
			if ( user.getErrors().size() == 0 )
			{
				request.getSession().setAttribute( "STATE_LIST", Constants.STATE_LIST );
				request.getSession().setAttribute( "VERIFICATION_METHODS", Constants.VERIFICATION_METHODS );
				
				Connection connection = null;
				try
				{
					connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
					if ( connection != null )
					{
						user.getAllOrganizations().clear();
						ArrayList<OrganizationBean> allOrganizations = SecurityUtilities.getAllOrganizations( connection );
						user.getAllOrganizations().addAll( allOrganizations );
						
						user.getAllRolesForAllApplications().clear();
						ArrayList<RoleBean> allRolesForAllApplications = SecurityUtilities.getAllRolesForAllApplications( connection );
						user.getAllRolesForAllApplications().addAll( allRolesForAllApplications );

						connection.close();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				
				// See if this user is restricted to the login organization:
				System.out.println( "The login organization ID is " + user.getLoginAuthenticationProfile().getOrganizationId() );
				System.out.println( "The login organization name is " + SecurityUtilities.getOrganization( user.getLoginAuthenticationProfile().getOrganizationId(), user.getAllOrganizations() ).getOrganizationName() );
				
				if ( "Watchtower".equalsIgnoreCase( SecurityUtilities.getOrganization( user.getLoginAuthenticationProfile().getOrganizationId(), user.getAllOrganizations() ).getOrganizationName() ) )
				{
					System.out.println( "User unrestricted" );
					user.setRestrictedOrganizationId( 0 );  // User has access to all organizations.
				}
				else
				{
					// If this is not a Watchtower user, need to lock down the organization drop down and restrict this user's access to 
					// their organization.
					System.out.println( "Set the restricted organization ID to " + user.getLoginAuthenticationProfile().getOrganizationId() );
					user.setRestrictedOrganizationId( user.getLoginAuthenticationProfile().getOrganizationId() );  
				}
				
				destinationPage = "/JSP/organization.jsp";
			}
		}

		this.view( request, response, destinationPage );
	}
}
