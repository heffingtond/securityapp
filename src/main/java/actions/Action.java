package actions;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class Action
{
	protected boolean isTimeout( HttpServletRequest request,
								 HttpServletResponse response )
	{
		HttpSession session = request.getSession();
		if ( session.isNew() )
		{
			this.view( request, response, "../JSP/login.jsp" );
			return true;
		}
		else
			return false;
	}
	
	protected void view( HttpServletRequest request, 
			             HttpServletResponse response,
			             String path )
	{
		RequestDispatcher view = null;
		view = request.getRequestDispatcher( path );
		try
		{
			view.forward( request, response );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public abstract void execute( HttpServletRequest request,
			                      HttpServletResponse response );
}
