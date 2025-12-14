package actions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LogoutAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		System.out.println( "top of LogoutAction" );
	    HttpSession session = request.getSession(false);
	    // Invalidate the current session
	    if (session != null) 
	    {
			System.out.println( "LogoutAction invalidate the session" );
	        session.invalidate();
	    }

	    // Redirect to the login page or home page
	    try
	    {
			System.out.println( "redirecting" );
	    	response.sendRedirect("/securityapp"); // redirect to login page.
	    }
	    catch( Exception e )
	    {
	    	e.printStackTrace();
	    }
	}
}
