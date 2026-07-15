package core;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import beans.OrganizationBean;
import beans.UserBean;
import beans.AuthenticationProfileBean;
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
	
	private static boolean containsNumber( String str ) 
	{
		boolean containsNumber = false;
	    if ( ! SecurityUtilities.isEmpty( str ) )
		    for ( char c : str.toCharArray() ) 
		    {
		        if ( Character.isDigit( c ) ) 
		            containsNumber = true;
		    }
	    return containsNumber;
	}
	
	public static ArrayList<String> validateLogin( String username, String password )
	{
		
		ArrayList<String> errors = new ArrayList<String>();
		if ( username.length() < 7 || username.length() > 100 )
			errors.add( "User Name must be between 7 and 100 characters in length." );
		if ( password.length() < 8 || password.length() > 50 )
			errors.add( "Password must be between 8 and 50 characters in length." );
		return errors;
	}

	public static ArrayList<String> validateAuthenticationProfile( AuthenticationProfileBean authentication, UserBean user, Connection connection )
	{
		ArrayList<String> errors = new ArrayList<String>();
		
		if ( authentication.getOrganizationId() == 0 )
			errors.add( "Please select the organization this user belongs to." );

		if ( SecurityUtilities.isEmpty( authentication.getFirstName() ) )
			errors.add( "The First Name name is required." );
		if ( SecurityUtilities.isEmpty( authentication.getLastName() ) )
			errors.add( "The Last Name name is required." );

		if ( SecurityUtilities.isEmpty( authentication.getMobilePhone() ) )
			errors.add( "Mobile Phone is required." );
		else
		if ( authentication.getMobilePhone().length() < 10 || authentication.getMobilePhone().length() > 12 )
			errors.add( "Mobile Phone must be 10 to 12 characters in length." );
		
		if ( SecurityUtilities.isEmpty( authentication.getTextPassword() ) )
			errors.add( "Password required." );
		else
			if ( ! containsNumber( authentication.getTextPassword() ) )
				errors.add( "password must contain at least one number." );
		if ( authentication.getTextPassword().length() < 8 || authentication.getTextPassword().length() > 50 )
			errors.add( "Password must be 8 to 50 characters in length." );
		if ( authentication.getTextPassword().contains( " " ) )
			errors.add( "Password cannot contain whitespace." );
		
		if ( SecurityUtilities.isEmpty( authentication.getUserId() ) )
			errors.add( "User ID is required." );
		else
		if ( authentication.getUserId().length() < 8 || authentication.getUserId().length() > 50 )
			errors.add( "User ID must be 8 to 50 characters in length." );
		
		if ( SecurityUtilities.isEmpty( authentication.getVerificationCodeMethod() ) || "SELECT".equals( authentication.getVerificationCodeMethod() ) )
			errors.add( "Please select a method for verification code delivery." );
		
		if ( errors.size() == 0 )
		{
			// If this is a save from from the edit function, we need to see if the user id is being changed.
			// If the user id is being changed, need to make sure the user id change is not a duplicate.
			boolean needDuplicateTest = true;
			if ( authentication.getAuthenticationProfileId() > 0 ) // If this is from the edit function
			{
				AuthenticationProfileBean original = null;
				String originalUserId = null;
				try
				{
					original = SecurityUtilities.getAuthenticationProfile( authentication.getAuthenticationProfileId(), connection );
					originalUserId = original.getUserId();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( authentication.getUserId().equalsIgnoreCase( originalUserId ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
			{
				// User ID cannot be a duplicate within an organization
				try
				{
					if ( SecurityUtilities.getAuthenticationProfile( authentication.getUserId(), authentication.getOrganizationId(), connection ) != null )
						errors.add( "The user " + authentication.getUserId() + " already exists.  Duplicates not allowed." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		
		return errors;
		
	}

	public static ArrayList<String> validateOrganization( OrganizationBean organization, Connection connection )
	{
		ArrayList<String> errors = new ArrayList<String>();
		// If the organizationId is zero, we are adding a new organization
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
			// If this is a save from from the edit function, we need to see if the name is being changed.
			// If the name is being changed, need to make sure the name change is not a duplicate.
			boolean needDuplicateTest = true;
			if ( organization.getOrganizationId() > 0 ) // If this is from the edit function
			{
				OrganizationBean original = null;
				String originalOrganizationName = null;
				try
				{
					original = getOrganization( organization.getOrganizationId(), connection );
					originalOrganizationName = original.getOrganizationName();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( organization.getOrganizationName().equalsIgnoreCase( originalOrganizationName ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
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
		System.out.println( "datasourceName is " + datasourceName );
		Context initContext = new InitialContext();
	    DataSource ds = ( DataSource ) initContext.lookup( "java:jboss/datasources/" + datasourceName);
	    Connection connection = ds.getConnection();
	    return connection;
	}
	
	public static AuthenticationProfileBean getAuthenticationProfile( int authenticationProfileId, ArrayList<AuthenticationProfileBean> allAuthenticationProfiles )
	{
        AuthenticationProfileBean authentication = null;
        
        int i = 0;
        boolean found = false;
        while ( i < allAuthenticationProfiles.size() && ! found )
        {
        	authentication = allAuthenticationProfiles.get( i );
        	if ( authentication.getAuthenticationProfileId() == authenticationProfileId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return authentication;
        else
        	return null;
	}
	
	public static AuthenticationProfileBean getAuthenticationProfile( String userId, int organizationId, Connection connection )
	{
		AuthenticationProfileBean authenticationProfile = null;
		try
		{
			String sql = 
			"select * from AUTHENTICATION_PROFILE "
		  + "where user_id = ? "
		  + "and organization_id = ?";
			System.out.println( "sql is " + sql );
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setString( 1, userId );
	        preparedStatement.setInt( 2, organizationId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	authenticationProfile = new AuthenticationProfileBean();
	        	authenticationProfile.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
	        	authenticationProfile.setOrganizationId( organizationId );
	        	authenticationProfile.setUserId( userId );
	        	authenticationProfile.setPassword( resultSet.getString("password") );
	        	authenticationProfile.setSalt( resultSet.getString("salt") );
	        	authenticationProfile.setFirstName( resultSet.getString("first_name") );
	        	authenticationProfile.setLastName( resultSet.getString("last_name") );
	        	authenticationProfile.setMobilePhone( resultSet.getString("mobile_phone") );
	        	authenticationProfile.setOfficePhone( resultSet.getString("office_phone") );
	        	authenticationProfile.setOfficePhoneExt( resultSet.getString("office_phone_ext") );
	        	authenticationProfile.setHomePhone( resultSet.getString("home_phone") );
	        	authenticationProfile.setFailedLoginAttempts( resultSet.getInt("failed_login_attempts") );
	        	authenticationProfile.setVerificationCodeMethod( resultSet.getString("verification_code_method") );
	        }
	        resultSet.close();
	        preparedStatement.close();
	        connection.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return authenticationProfile;
	}
	
	public static AuthenticationProfileBean getAuthenticationProfile( int authenticationProfileId, Connection connection )
	{
		AuthenticationProfileBean authenticationProfile = null;
		try
		{
			String sql = 
			"select * from AUTHENTICATION_PROFILE "
		  + "where authentication_profile_id = ? ";
			System.out.println( "sql is " + sql );
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, authenticationProfileId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	authenticationProfile = new AuthenticationProfileBean();
	        	authenticationProfile.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
	        	authenticationProfile.setOrganizationId( resultSet.getInt("organization_id") );
	        	authenticationProfile.setUserId( resultSet.getString("user_id") );
	        	authenticationProfile.setPassword( resultSet.getString("password") );
	        	authenticationProfile.setSalt( resultSet.getString("salt") );
	        	authenticationProfile.setFirstName( resultSet.getString("first_name") );
	        	authenticationProfile.setLastName( resultSet.getString("last_name") );
	        	authenticationProfile.setMobilePhone( resultSet.getString("mobile_phone") );
	        	authenticationProfile.setOfficePhone( resultSet.getString("office_phone") );
	        	authenticationProfile.setOfficePhoneExt( resultSet.getString("office_phone_ext") );
	        	authenticationProfile.setHomePhone( resultSet.getString("home_phone") );
	        	authenticationProfile.setFailedLoginAttempts( resultSet.getInt("failed_login_attempts") );
	        	authenticationProfile.setVerificationCodeMethod( resultSet.getString("verification_code_method") );
	        }
	        resultSet.close();
	        preparedStatement.close();
	        connection.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return authenticationProfile;
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
	
	
	public static OrganizationBean getOrganization( int organizationId, Connection connection ) throws SQLException
	{
		String sql = "select * from APPLICATION_SECURITY.ORGANIZATION "
				   + "where organization_id = ?";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
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

	public static void addNewOrganization( UserBean user, Connection connection ) throws SQLException
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
        preparedStatement.setString( 1, user.getActiveOrganization().getOrganizationName() );
        preparedStatement.setString( 2, user.getActiveOrganization().getOrganizationAddress() );
        preparedStatement.setString( 3, user.getActiveOrganization().getOrganizationCity() );
        preparedStatement.setString( 4, user.getActiveOrganization().getOrganizationState() );
        preparedStatement.setString( 5, user.getActiveOrganization().getOrganizationZip() );
        preparedStatement.setString( 6, user.getActiveOrganization().getOrganizationZipExt() );
        preparedStatement.setString( 7, user.getActiveOrganization().getPrimaryUrl() );
        preparedStatement.setString( 8, user.getActiveOrganization().getOrganizationDescription() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        System.out.println( "New primary key: " + newPrimaryKey );
	        user.getActiveOrganization().setOrganizationId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void updateOrganization( OrganizationBean organization, Connection connection ) throws SQLException
	{
		String sql = "update APPLICATION_SECURITY.ORGANIZATION set "
				   + " organization_name=?, "
				   + " organization_address=?,"
				   + " organization_city=?,"
				   + " organization_state=?,"
				   + " organization_zip=?,"
				   + " organization_zip_ext=?,"
				   + " primary_url=?,"
				   + " organization_description=? "
				   + " where organization_id = ?";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
		System.out.println( "saving name " + organization.getOrganizationName() );
        preparedStatement.setString( 1, organization.getOrganizationName() );
        preparedStatement.setString( 2, organization.getOrganizationAddress() );
        preparedStatement.setString( 3, organization.getOrganizationCity() );
        preparedStatement.setString( 4, organization.getOrganizationState() );
        preparedStatement.setString( 5, organization.getOrganizationZip() );
        preparedStatement.setString( 6, organization.getOrganizationZipExt() );
        preparedStatement.setString( 7, organization.getPrimaryUrl() );
        preparedStatement.setString( 8, organization.getOrganizationDescription() );
        preparedStatement.setInt( 9, organization.getOrganizationId() );
		System.out.println( "saving organizationId " + organization.getOrganizationId() );
        preparedStatement.executeUpdate();
        
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

	public static ArrayList<AuthenticationProfileBean> getAllAuthenticationProfiles( Connection connection ) throws SQLException
	{
		ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = new ArrayList<AuthenticationProfileBean>();
		String sql = "select * from APPLICATION_SECURITY.AUTHENTICATION_PROFILE ";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
		resultSet = preparedStatement.executeQuery();
		AuthenticationProfileBean authentication = null;
		while ( resultSet.next() )
		{
			authentication = new AuthenticationProfileBean();
			authentication.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
			authentication.setOrganizationId( resultSet.getInt("organization_id") );
			authentication.setUserId( resultSet.getString("user_id") );
			authentication.setPassword( resultSet.getString("password") );
			authentication.setSalt( resultSet.getString("salt") );
			authentication.setFirstName( resultSet.getString("first_name") );
			authentication.setLastName( resultSet.getString("last_name") );
			authentication.setMobilePhone( resultSet.getString("mobile_phone") );
			authentication.setOfficePhone( resultSet.getString("office_phone") );
			authentication.setOfficePhoneExt( resultSet.getString("office_phone_ext") );
			authentication.setHomePhone( resultSet.getString("home_phone") );
			authentication.setFailedLoginAttempts( resultSet.getInt("failed_login_attempts") );
			authentication.setVerificationCodeMethod( resultSet.getString("verification_code_method") );
			allAuthenticationProfiles.add( authentication );
		}
		resultSet.close();
		preparedStatement.close();
		return allAuthenticationProfiles;
	}
	
	public static void deleteAuthenticationProfile( int authenticationProfileId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.AUTHENTICATION_PROFILE "
				   + "where authentication_profile_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, authenticationProfileId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}

	public static void addNewAuthenticationProfile( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.AUTHENTICATION_PROFILE "
				   + "("
				   + "organization_id, "
				   + "user_id,"
				   + "password,"
				   + "salt,"
				   + "first_name,"
				   + "last_name,"
				   + "mobile_phone,"
				   + "office_phone,"
				   + "office_phone_ext,"
				   + "home_phone,"
				   + "failed_login_attempts,"
				   + "verification_code_method"
				   + ") "
				   + "values( ?,?,?,?,?,?,?,?,?,?,?,? )";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
		System.out.println( "OrganizationId is " + user.getActiveAuthenticationProfile().getOrganizationId() );
        preparedStatement.setInt( 1, user.getActiveAuthenticationProfile().getOrganizationId() );
        preparedStatement.setString( 2, user.getActiveAuthenticationProfile().getUserId() );
        preparedStatement.setString( 3, user.getActiveAuthenticationProfile().getPassword() );
        preparedStatement.setString( 4, user.getActiveAuthenticationProfile().getSalt() );
        preparedStatement.setString( 5, user.getActiveAuthenticationProfile().getFirstName() );
        preparedStatement.setString( 6, user.getActiveAuthenticationProfile().getLastName() );
        preparedStatement.setString( 7, user.getActiveAuthenticationProfile().getMobilePhone() );
        preparedStatement.setString( 8, user.getActiveAuthenticationProfile().getOfficePhone() );
        preparedStatement.setString( 9, user.getActiveAuthenticationProfile().getOfficePhoneExt() );
        preparedStatement.setString( 10, user.getActiveAuthenticationProfile().getHomePhone() );
        preparedStatement.setInt( 11, user.getActiveAuthenticationProfile().getFailedLoginAttempts() );
        preparedStatement.setString( 12, user.getActiveAuthenticationProfile().getVerificationCodeMethod() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        System.out.println( "New primary key: " + newPrimaryKey );
	        user.getActiveAuthenticationProfile().setAuthenticationProfileId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void createSecurePasswordAndSalt( AuthenticationProfileBean authentication, String plainTextPassword ) throws Exception
	{
        // 1. Generate a random salt
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes( saltBytes );
        String salt = Base64.getEncoder().encodeToString( saltBytes );

        // 2. Hash the password with the salt
        String hashedPassword = hashPassword( plainTextPassword, saltBytes );

        System.out.println( "Hashed password: " + hashedPassword );
        System.out.println( "Salt: " + salt );
        // 3. Save both to profile bean
        authentication.setPassword( hashedPassword );
        authentication.setSalt( salt );
	}
	
	private static String hashPassword(String password, byte[] salt) throws Exception
	{
		final int ITERATIONS = 10000;
	    final int KEY_LENGTH = 256;
	    final String ALGORITHM = "PBKDF2WithHmacSHA256";
	    PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), salt, ITERATIONS, KEY_LENGTH );
        SecretKeyFactory skf = SecretKeyFactory.getInstance( ALGORITHM );
        byte[] hash = skf.generateSecret( spec ).getEncoded();
        return Base64.getEncoder().encodeToString( hash );
    }
}
