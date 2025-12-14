package actions;

import java.sql.Connection;
import java.util.ArrayList;

import beans.OrganizationBean;
import beans.UserBean;
import core.SecurityUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OrganizationAction extends Action
{
	public void execute( HttpServletRequest request,
            			 HttpServletResponse response )
	{
		System.out.println( "Hit OrganizationAction" );
		String destinationPage = "/JSP/organization.jsp";
		UserBean user = ( UserBean ) request.getSession().getAttribute( "UserBean" );

		String buttonPressed =  request.getParameter( "OrganizationAction" );
		System.out.println( "NEW button pressed is " + buttonPressed );
		if ( "addOrganization".equalsIgnoreCase( buttonPressed ) )
		{
			OrganizationBean organization = new OrganizationBean();
			System.out.println( "Save the new organization here." );
			String organizationName = request.getParameter( "organizationName" );
			organization.setOrganizationName( organizationName );
			String organizationAddress = request.getParameter( "organizationAddress" );
			organization.setOrganizationAddress( organizationAddress );
			String organizationCity = request.getParameter( "organizationCity" );
			organization.setOrganizationCity( organizationCity );
			String organizationState = request.getParameter( "organizationState" );
			organization.setOrganizationState( organizationState );
			String organizationZip = request.getParameter( "organizationZip" );
			organization.setOrganizationZip( organizationZip );
			String organizationZipExt = request.getParameter( "organizationZipExt" );
			organization.setOrganizationZipExt( organizationZipExt );
			String primaryUrl = request.getParameter( "primaryUrl" );
			organization.setPrimaryUrl( primaryUrl );
			String organizationDescription = request.getParameter( "organizationDescription" );
			organization.setOrganizationDescription( organizationDescription );
			user.setActiveOrganization( organization );
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					user.getErrors().addAll( SecurityUtilities.validateOrganization( organization, connection ) );
					if ( user.getErrors().size() == 0 )
					{
						SecurityUtilities.addNewOrganization( organization, connection );
						user.getAllOrganizations().clear();
						ArrayList<OrganizationBean> allOrganizations = SecurityUtilities.getAllOrganizations( connection );
						user.getAllOrganizations().addAll( allOrganizations );
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
			// Parse the organizationId
			String organizationIdStr = buttonPressed.substring( buttonPressed.indexOf( '_' ) + 1 );
			int organizationId = Integer.parseInt( organizationIdStr );
			// Get and set active organization.
			OrganizationBean activeOrganization = SecurityUtilities.getOrganization( organizationId, user.getAllOrganizations() );
			user.setActiveOrganization( activeOrganization );
			System.out.println( "Deleting organizationId " + organizationId );
			Connection connection = null;
			try
			{
				connection = SecurityUtilities.getJndiConnection( "SECURITY_MYSQL_DB" );
				if ( connection != null )
				{
					SecurityUtilities.deleteOrganization( organizationId, connection );
					user.getAllOrganizations().clear();
					ArrayList<OrganizationBean> allOrganizations = SecurityUtilities.getAllOrganizations( connection );
					user.getAllOrganizations().addAll( allOrganizations );
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
