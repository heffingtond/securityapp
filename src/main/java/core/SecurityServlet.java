// package security;
package core;

import java.io.IOException;

import actions.Action;
import actions.ApplicationAction;
import actions.AuthenticationProfileAction;
import actions.DepartmentAction;
import actions.LoginAction;
import actions.LogoutAction;
import actions.OrganizationAction;
import actions.RoleAction;
import actions.RoleAssignmentAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SecurityServlet
 */
@WebServlet("/SecurityServlet")
public class SecurityServlet extends HttpServlet
{
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost( request, response );
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		boolean timeout = false;
		try
		{
			timeout = SecurityUtilities.isTimeout( request, response );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		if ( ! timeout )
		{
			Action action = null;
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "LoginAction" ) ) != null )
				action = new LoginAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "LogoutAction" ) ) != null )
				action = new LogoutAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "OrganizationAction" ) ) != null )
				action = new OrganizationAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "AuthenticationProfileAction" ) ) != null )
				action = new AuthenticationProfileAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "DepartmentAction" ) ) != null )
				action = new DepartmentAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "ApplicationAction" ) ) != null )
				action = new ApplicationAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "RoleAction" ) ) != null )
				action = new RoleAction();
			else
			if ( SecurityUtilities.replaceSpecialCharacters( request.getParameter( "RoleAssignmentAction" ) ) != null )
				action = new RoleAssignmentAction();
			
			if ( action != null )
				action.execute( request, response );
			else
				System.out.println( "No action found in servlet!" );
		}
	}
}
