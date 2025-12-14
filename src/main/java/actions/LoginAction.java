package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.OrganizationBean;
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
		HttpSession session = request.getSession( false );
		if ( session == null ) // start over
		{
			System.out.println( "Session was dead.  Getting a new one." );
			session = request.getSession();
		}
//		session.setMaxInactiveInterval( 120 ); // 120 seconds = 2 minutes? = 
		session.setMaxInactiveInterval( 1800 ); // 30 minutes
		String username = request.getParameter( "username" );
		String password = request.getParameter( "password" );
		System.out.println( "username: " + username );
		System.out.println( "password: " + password );
		UserBean user = new UserBean();
		request.getSession().setAttribute( "UserBean", user );
		Connection connection = null;
		try
		{
			System.out.println( "Get conn" );
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
				}

				if ( user.getErrors().size() == 0 )
				{
					request.getSession().setAttribute( "STATE_LIST", Constants.STATE_LIST );
					user.getAllOrganizations().clear();
					ArrayList<OrganizationBean> allOrganizations = SecurityUtilities.getAllOrganizations( connection );
					user.getAllOrganizations().addAll( allOrganizations );
					destinationPage = "/JSP/organization.jsp";
				}
				connection.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		this.view( request, response, destinationPage );
	}
}
