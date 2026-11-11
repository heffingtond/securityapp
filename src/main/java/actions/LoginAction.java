package actions;

import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import beans.OrganizationBean;
import beans.RoleBean;
import beans.UserBean;
import core.Constants;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginAction extends Action
{
	
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		String destinationPage = "/JSP/login.jsp";
		String buttonPressed =  request.getParameter( "LoginAction" );
		boolean continueToOrganization = false;
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
			user.setUserName( username );
			request.getSession().setAttribute( "UserBean", user );
			user.getErrors().addAll( SecurityUtilities.validateLogin(  username, password ) );
	
			if ( user.getErrors().size() == 0 )
			{
				// Call a REST service to authenticate.
				// Authenticate with user name and password, 
				// return the profile if successful.  Otherwise, 
				// return [AUTHENTICATION_FAILED].
				String loginResultJson = SecurityUtilities.getSecureLogin( username, password );
				user.setLoginAuthenticationProfile( SecurityUtilities.loadAuthenticationProfileBean( loginResultJson ) );
				
				if ( "WAIT FOR 2 FACTOR AUTHENTICATION".equals( user.getLoginAuthenticationProfile().getClientResult() ) )
				{
					System.out.println( "Wait for 2FA" );
					destinationPage = "/JSP/otpVerification.jsp";
				}
				else
				if ( "INCORRECT PASSWORD".equals( user.getLoginAuthenticationProfile().getClientResult() )
				  || "USER NOT FOUND".equals( user.getLoginAuthenticationProfile().getClientResult() )	)
				{
					user.getErrors().add( "Bad username or password" );
					System.out.println( "Login error: " + user.getLoginAuthenticationProfile().getClientResult() );
					destinationPage = "/JSP/login.jsp";
				}
				else
				{
					// Get user roles here.
					SecurityUtilities.loadUserRoles( user );
					continueToOrganization = true;
					destinationPage = "/JSP/organization.jsp";
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
			String validationResultJson = SecurityUtilities.validateCode( user.getLoginAuthenticationProfile().getAuthenticationProfileId(), passcode );
			String validationResult = null;
			JSONParser parser = new JSONParser();
			try
			{
				JSONArray jsonArray = ( JSONArray ) parser.parse( validationResultJson );
				for ( Object obj : jsonArray ) 
				{
					JSONObject jsonObject = ( JSONObject ) obj; // Each element is a JSONObject
					validationResult = ( String ) jsonObject.get( "validationResult" );
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			
			// Possible Results:
			// VALIDATION SUCCESSFUL or VALIDATION FAILED
			if ( validationResult != null )
			{
				if ( "VALIDATION SUCCESSFUL".equals( validationResult ) )
				{
					SecurityUtilities.loadUserRoles( user );
					continueToOrganization = true;
					destinationPage = "/JSP/organization.jsp";
				}
				else
				{
					user.getErrors().add( "Passcode validation failed.  Please try again." );
					destinationPage = "/JSP/otpVerification.jsp";
				}
			}
			else
			{
				user.getErrors().add( "Passcode validation failed.  Please try again.  Contact administrator if problem persists." );
				destinationPage = "/JSP/otpVerification.jsp";
			}
		}
		else
		if ( "Request New Code".equalsIgnoreCase( buttonPressed ) )
		{
			UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
			String verificationCodeMethod =  user.getLoginAuthenticationProfile().getVerificationCodeMethod();
			String mobilePhone =  user.getLoginAuthenticationProfile().getMobilePhone();
			String email =  user.getLoginAuthenticationProfile().getEmail();
			String newCodeResult = "NEW CODE FAILED";
			int authenticationProfileId = user.getLoginAuthenticationProfile().getAuthenticationProfileId();
			if ( verificationCodeMethod != null )
			{
				String newVarificationCodeResultJson = SecurityUtilities.getNewVerificationCode( authenticationProfileId, 
						                                                                         verificationCodeMethod,
						                                                                         mobilePhone,
						                                                                         email );
				
				JSONParser parser = new JSONParser();
				try
				{
					JSONArray jsonArray = ( JSONArray ) parser.parse( newVarificationCodeResultJson );
					for ( Object obj : jsonArray ) 
					{
						JSONObject jsonObject = ( JSONObject ) obj; // Each element is a JSONObject
						newCodeResult = ( String ) jsonObject.get( "newCodeResult" );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			else
				user.getErrors().add( "Verification code send method not defined on profile." );
			if ( ! "WAIT FOR 2 FACTOR AUTHENTICATION".equals( newCodeResult ) )
				user.getErrors().add( "New code request failed: " + newCodeResult + ".  If problem persists, contact administrator." );
			else
				request.setAttribute( "userMessage", "New code sent successfully." );
			destinationPage = "/JSP/otpVerification.jsp";
		}
		if ( continueToOrganization )
		{
			UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );
			if ( user.getErrors().size() > 0 )
				destinationPage = "/JSP/login.jsp";
			else
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
				
				if ( "Watchtower".equalsIgnoreCase( SecurityUtilities.getOrganization( user.getLoginAuthenticationProfile().getOrganizationId(), user.getAllOrganizations() ).getOrganizationName() ) )
				{
					System.out.println( "User unrestricted" );
					user.setRestrictedOrganizationId( 0 );  // User has access to all organizations.
				}
				else
				{
					// If this is not a Watchtower user, need to lock down the organization drop down and restrict this user's access to 
					// their organization.
					user.setRestrictedOrganizationId( user.getLoginAuthenticationProfile().getOrganizationId() );  
				}
				
				destinationPage = "/JSP/organization.jsp";
			}
		}

		this.view( request, response, destinationPage );
	}
}
