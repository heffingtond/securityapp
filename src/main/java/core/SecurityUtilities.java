package core;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import beans.OrganizationBean;
import beans.SecurityProfileBean;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SecurityUtilities
{
	public static boolean isTimeout( HttpServletRequest request,
			 						 HttpServletResponse response ) throws Exception
	{
		System.out.println( "CHECKING FOR TIMEOUT NEW" );
		HttpSession session = request.getSession( false );
		boolean isTimeout = false;
		if( session == null )
		{
			System.out.println( "IS TIMEOUT" );
			isTimeout = true;
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
		return isTimeout;
	}
	
	public static String replaceSpecialCharacters( String inString )
	{
		final String SINGLE_QUOTE = "&#39;";
		final String DOUBLE_QUOTE = "&#34;";
		final String LESS_THAN = "&lt;";
		final String GREATER_THAN = "&gt;";
		StringBuffer sb = null;
		if ( inString != null )
		{
			sb = new StringBuffer( inString );
			int totalLength = sb.length();
			for ( int i = totalLength - 1; i >= 0; i-- )
			{
				if ( sb.charAt( i ) == '\'' )
				{
					sb.deleteCharAt( i );
					sb.insert( i, SINGLE_QUOTE );
				}
				else
				if ( sb.charAt( i ) == '"' )
				{
					sb.deleteCharAt( i );
					sb.insert( i, DOUBLE_QUOTE );
				}

			}
			return sb.toString();
		}
		return null;
	}
	
	public static ArrayList<String> validateLogin( String username, String password )
	{
		ArrayList<String> errors = new ArrayList<String>();
		// Must validate username is unique
		if ( username.length() < 7 || username.length() > 100 )
			errors.add( "User Name must be between 7 and 100 characters in length." );
		if ( password.length() < 8 || password.length() > 50 )
			errors.add( "Password must be between 8 and 50 characters in length." );
		// if (  )
		return errors;
	}
	
	public static ArrayList<String> validateOrganization( OrganizationBean organization, Connection connection )
	{
		ArrayList<String> errors = new ArrayList<String>();
		// If the organizationId is zero, we are adding a new organization
		if ( organization.getOrganizationId() == 0 )
		{
			if ( SecurityUtilities.isEmpty( organization.getOrganizationName() ) )
				errors.add( "The organization name is required." );
			if ( SecurityUtilities.isEmpty( organization.getOrganizationAddress() ) )
				errors.add( "The organization address is required." );
			if ( SecurityUtilities.isEmpty( organization.getOrganizationCity() ) )
				errors.add( "The organization city is required." );
			if ( SecurityUtilities.isEmpty( organization.getOrganizationState() ) )
				errors.add( "The organization state is required." );
			if ( SecurityUtilities.isEmpty( organization.getOrganizationZip() ) )
				errors.add( "The organization zip code is required." );
			if ( SecurityUtilities.isEmpty( organization.getPrimaryUrl() ) )
				errors.add( "The primary URL is required." );
			
			if ( errors.size() == 0 )
			{
				// Organization name cannot be a duplicate
				try
				{
					if ( getOrganization( organization.getOrganizationName(), connection ) != null )
						errors.add( "The organization " + organization.getOrganizationName() + " already exists.  Duplicates not allowed." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		return errors;
	}
	
	public static boolean isEmpty( String inString )
	{
		boolean isEmpty = false;
		if ( inString == null )
			isEmpty = true;
		else
		{
			inString = inString.trim();
			if ( inString.length() == 0 )
				isEmpty = true;
		}
		return isEmpty;
	}
	
	public static Connection getJndiConnection( String datasourceName ) throws Exception
	{
		Context initContext = new InitialContext();
	    DataSource ds = ( DataSource ) initContext.lookup( "java:jboss/datasources/" + datasourceName);
	    Connection connection = ds.getConnection();
	    return connection;
	}

	
	public static String getSecurityProfile( SecurityProfileBean profile )
	{
		String status = null;
		System.out.println( "TOP OF getSecurityProfile()" );
		Connection connection = null;
		try
		{
			connection = getJndiConnection( "SECURITY_MYSQL_DB" );
			if ( connection != null )
			{
				String sql = "select * from AUTHENTICATION_PROFILE";
				System.out.println( "sql is " + sql );
				PreparedStatement preparedStatement = null;
		        ResultSet resultSet = null;
		        preparedStatement = connection.prepareStatement( sql );
		        resultSet = preparedStatement.executeQuery();
		        if ( resultSet.next() )
		        {
		        	String userId = resultSet.getString("userId");
		        	System.out.println( "USER ID: " + userId );
		        }
		        resultSet.close();
		        preparedStatement.close();
		        connection.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return status;
	}
	
	
	public static OrganizationBean getOrganization( String organizationName, Connection connection ) throws SQLException
	{
		String sql = "select * from APPLICATION_SECURITY.ORGANIZATION "
				   + "where organization_name = ?";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setString( 1, organizationName );
        resultSet = preparedStatement.executeQuery();
        OrganizationBean organization = null;
        if ( resultSet.next() )
        {
        	organization = new OrganizationBean();
        	organization.setOrganizationId( resultSet.getInt("organization_id") );
        	organization.setOrganizationName( resultSet.getString("organization_name") );
        	organization.setOrganizationAddress( resultSet.getString("organization_address") );
        	organization.setOrganizationCity( resultSet.getString("organization_city") );
        	organization.setOrganizationState( resultSet.getString("organization_state") );
        	organization.setOrganizationZip( resultSet.getString("organization_zip") );
        	organization.setOrganizationZipExt( resultSet.getString("organization_zip_ext") );
        	organization.setPrimaryUrl( resultSet.getString("primary_url") );
        	organization.setOrganizationDescription( resultSet.getString("organization_description") );
        }
        resultSet.close();
        preparedStatement.close();
		
		return organization;
	}
	
	public static OrganizationBean getOrganization( int organizationId, ArrayList<OrganizationBean> allOrganizations )
	{
        OrganizationBean organization = null;
        
        int i = 0;
        boolean found = false;
        while ( i < allOrganizations.size() && ! found )
        {
        	organization = allOrganizations.get( i );
        	if ( organization.getOrganizationId() == organizationId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return organization;
        else
        	return null;
	}

	public static void addNewOrganization( OrganizationBean organization, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.ORGANIZATION "
				   + "(organization_name, "
				   + "organization_address,"
				   + "organization_city,"
				   + "organization_state,"
				   + "organization_zip,"
				   + "organization_zip_ext,"
				   + "primary_url,"
				   + "organization_description) "
				   + "values( ?,?,?,?,?,?,?,? )";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setString( 1, organization.getOrganizationName() );
        preparedStatement.setString( 2, organization.getOrganizationAddress() );
        preparedStatement.setString( 3, organization.getOrganizationCity() );
        preparedStatement.setString( 4, organization.getOrganizationState() );
        preparedStatement.setString( 5, organization.getOrganizationZip() );
        preparedStatement.setString( 6, organization.getOrganizationZipExt() );
        preparedStatement.setString( 7, organization.getPrimaryUrl() );
        preparedStatement.setString( 8, organization.getOrganizationDescription() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        System.out.println( "New primary key: " + newPrimaryKey );
	    	organization.setOrganizationId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}


	public static void deleteOrganization( int organizationId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.ORGANIZATION "
				   + "where organization_id = ?";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}
	

	public static ArrayList<OrganizationBean> getAllOrganizations( Connection connection ) throws SQLException
	{
		ArrayList<OrganizationBean> allOrganizations = new ArrayList<OrganizationBean>();
		String sql = "select * from APPLICATION_SECURITY.ORGANIZATION ";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
		resultSet = preparedStatement.executeQuery();
		OrganizationBean organization = null;
		while ( resultSet.next() )
		{
			organization = new OrganizationBean();
			organization.setOrganizationId( resultSet.getInt("organization_id") );
			organization.setOrganizationName( resultSet.getString("organization_name") );
			organization.setOrganizationAddress( resultSet.getString("organization_address") );
			organization.setOrganizationCity( resultSet.getString("organization_city") );
			organization.setOrganizationState( resultSet.getString("organization_state") );
			organization.setOrganizationZip( resultSet.getString("organization_zip") );
			organization.setOrganizationZipExt( resultSet.getString("organization_zip_ext") );
			organization.setPrimaryUrl( resultSet.getString("primary_url") );
			organization.setOrganizationDescription( resultSet.getString("organization_description") );
			allOrganizations.add( organization );
		}
		resultSet.close();
		preparedStatement.close();
		return allOrganizations;
	}
}
