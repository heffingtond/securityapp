package core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import beans.OrganizationBean;
import beans.RoleBean;
import beans.UserBean;
import beans.VerificationCodeBean;
import beans.ApplicationBean;
import beans.ApplicationUserDepartmentRoleBean;
import beans.AuthenticationProfileBean;
import beans.DepartmentBean;
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

	public static void validateAuthenticationProfile( UserBean user, Connection connection )
	{
		if ( user.getActiveAuthenticationProfile().getOrganizationId() == 0 )
			user.getErrors().add( "Please select the organization this user belongs to." );

		if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getFirstName() ) )
			user.getErrors().add( "The First Name name is required." );
		if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getLastName() ) )
			user.getErrors().add( "The Last Name name is required." );

		if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getMobilePhone() ) )
			user.getErrors().add( "Mobile Phone is required." );
		else
		if ( user.getActiveAuthenticationProfile().getMobilePhone().length() < 10 || user.getActiveAuthenticationProfile().getMobilePhone().length() > 12 )
			user.getErrors().add( "Mobile Phone must be 10 to 12 characters in length." );
		
		boolean needPasswordValidation = true;
		if ( user.getActiveAuthenticationProfile().getAuthenticationProfileId() > 0 ) // If this is from the edit function
			if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getTextPassword() ) ) // Password should be left alone
				needPasswordValidation = false;
		if ( needPasswordValidation )
		{
			if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getTextPassword() ) )
				user.getErrors().add( "Password required." );
			else
				if ( ! containsNumber( user.getActiveAuthenticationProfile().getTextPassword() ) )
					user.getErrors().add( "password must contain at least one number." );
			if ( user.getActiveAuthenticationProfile().getTextPassword().length() < 8 || user.getActiveAuthenticationProfile().getTextPassword().length() > 50 )
				user.getErrors().add( "Password must be 8 to 50 characters in length." );
			if ( user.getActiveAuthenticationProfile().getTextPassword().contains( " " ) )
				user.getErrors().add( "Password cannot contain whitespace." );
		}
		
		if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getUserId() ) )
			user.getErrors().add( "User ID is required." );
		else
		if ( user.getActiveAuthenticationProfile().getUserId().length() < 8 || user.getActiveAuthenticationProfile().getUserId().length() > 50 )
			user.getErrors().add( "User ID must be 8 to 50 characters in length." );
		
		if ( SecurityUtilities.isEmpty( user.getActiveAuthenticationProfile().getVerificationCodeMethod() ) || "SELECT".equals( user.getActiveAuthenticationProfile().getVerificationCodeMethod() ) )
			user.getErrors().add( "Please select a method for verification code delivery." );
		
		if ( user.getErrors().size() == 0 )
		{
			// If this is a save from from the edit function, we need to see if the userId is being changed.
			// If the userId is being changed, need to make sure the userId change is not a duplicate.
			boolean needDuplicateTest = true;
			if ( user.getActiveAuthenticationProfile().getAuthenticationProfileId() > 0 ) // If this is from the edit function
			{
				AuthenticationProfileBean original = null;
				String originalUserId = null;
				try
				{
					original = SecurityUtilities.getAuthenticationProfile( user.getActiveAuthenticationProfile().getAuthenticationProfileId(), connection );
					originalUserId = original.getUserId();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( user.getActiveAuthenticationProfile().getUserId().equalsIgnoreCase( originalUserId ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
			{
				// User ID cannot be a duplicate across the entire database.  Therefore, if a user works for multiple organizations,
				// that is fine, however they must have a unique user ID for each organization.
				try
				{
					if ( SecurityUtilities.getAuthenticationProfile( user.getActiveAuthenticationProfile().getUserId(), connection ) != null )
						user.getErrors().add( "The user " + user.getActiveAuthenticationProfile().getUserId() + " already exists.  Duplicates not allowed.  If user works for multiple organizations, a unique user ID is required for each organization." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void validateDepartment( UserBean user, Connection connection )
	{
		if ( user.getActiveDepartment().getOrganizationId() == 0 )
			user.getErrors().add( "Please select the organization this department belongs to." );

		if ( SecurityUtilities.isEmpty( user.getActiveDepartment().getDepartmentName() ) )
			user.getErrors().add( "The Department name is required." );

		if ( SecurityUtilities.isEmpty( user.getActiveDepartment().getDepartmentCode() ) )
			user.getErrors().add( "A unique department code (within the organization) is required." );
		else
		if ( user.getActiveDepartment().getDepartmentCode().length() > 20 )
			user.getErrors().add( "Department Code cannot exceed 20 characters in length." );
		
		if ( SecurityUtilities.isEmpty( user.getActiveDepartment().getDepartmentDescription() ) )
			user.getErrors().add( "Please enter a description for this department." );
		
		if ( user.getErrors().size() == 0 )
		{
			// If this is a save from from the edit function, we need to see if the department code is being changed.
			// If the department code is being changed, need to make sure the department code change is not a duplicate.
			boolean needDuplicateTest = true;
			if ( user.getActiveDepartment().getDepartmentId() > 0 ) // If this is from the edit function
			{
				DepartmentBean original = null;
				String originalDepartmentCode = null;
				try
				{
					original = SecurityUtilities.getDepartment( user.getActiveDepartment().getDepartmentId(), connection );
					originalDepartmentCode = original.getDepartmentCode();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( user.getActiveDepartment().getDepartmentCode().equalsIgnoreCase( originalDepartmentCode ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
			{
				// Department Code cannot be a duplicate within an organization
				try
				{
					if ( SecurityUtilities.getDepartment( user.getActiveDepartment().getDepartmentCode(), user.getActiveDepartment().getOrganizationId(), connection ) != null )
						user.getErrors().add( "The department code " + user.getActiveDepartment().getDepartmentCode() + " already exists.  Duplicates not allowed." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void validateApplication( UserBean user, Connection connection )
	{
		if ( user.getActiveApplication().getOrganizationId() == 0 )
			user.getErrors().add( "Please select the organization this application belongs to." );

		if ( SecurityUtilities.isEmpty( user.getActiveApplication().getApplicationName() ) )
			user.getErrors().add( "The Application name is required." );

		if ( SecurityUtilities.isEmpty( user.getActiveApplication().getApplicationCode() ) )
			user.getErrors().add( "A unique application code (across the entire database) is required." );
		else
		if ( user.getActiveApplication().getApplicationCode().length() > 50 )
			user.getErrors().add( "Application Code cannot exceed 50 characters in length." );
		
		if ( SecurityUtilities.isEmpty( user.getActiveApplication().getApplicationDescription() ) )
			user.getErrors().add( "Please enter a description for this application." );

		if ( SecurityUtilities.isEmpty( user.getActiveApplication().getDomainName() ) )
			user.getErrors().add( "Please enter a domain name for this application." );
		
		if ( SecurityUtilities.isEmpty( user.getActiveApplication().getOwner() ) )
			user.getErrors().add( "Please enter the owner of this application." );

		if ( user.getErrors().size() == 0 )
		{
			// If this is a save from from the edit function, we need to see if the application code is being changed.
			// If the application code is being changed, need to make sure the application code change is not a duplicate
			// within the database.
			boolean needDuplicateTest = true;
			if ( user.getActiveApplication().getApplicationId() > 0 ) // If this is from the edit function
			{
				ApplicationBean original = null;
				String originalApplicationCode = null;
				try
				{
					original = SecurityUtilities.getApplication( user.getActiveApplication().getApplicationId(), connection );
					originalApplicationCode = original.getApplicationCode();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( user.getActiveApplication().getApplicationCode().equalsIgnoreCase( originalApplicationCode ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
			{
				// Application Code must be unique across the entire database
				try
				{
					if ( SecurityUtilities.getApplication( user.getActiveApplication().getApplicationCode(), connection ) != null )
						user.getErrors().add( "The application code " + user.getActiveApplication().getApplicationCode() + " already exists.  Application code must be unique across the entire database." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void validateRole( UserBean user, Connection connection )
	{
		if ( user.getActiveRole().getOrganizationId() == 0 )
			user.getErrors().add( "Please select the the organization." );

		if ( user.getActiveRole().getApplicationId() == 0 )
			user.getErrors().add( "Please select the application this role belongs to." );

		if ( SecurityUtilities.isEmpty( user.getActiveRole().getRoleName() ) )
			user.getErrors().add( "A role name is required (must be unique for the selected application)." );
		else
		if ( user.getActiveRole().getRoleName().length() > 128 )
			user.getErrors().add( "Role name cannot exceed 128 characters in length." );

		if ( SecurityUtilities.isEmpty( user.getActiveRole().getRoleDescription() ) )
			user.getErrors().add( "A role description is required." );
		
		if ( user.getErrors().size() == 0 )
		{
			// If this is a save from from the edit function, we need to see if the role name is being changed.
			// If the role name is being changed, need to make sure the role name change is not a duplicate
			// for the selected application this role belongs to.
			boolean needDuplicateTest = true;
			if ( user.getActiveRole().getRoleId() > 0 ) // If this is from the edit function
			{
				RoleBean original = null;
				String originalRoleName = null;
				try
				{
					original = SecurityUtilities.getRole( user.getActiveRole().getRoleId(), connection );
					originalRoleName = original.getRoleName();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if ( user.getActiveRole().getRoleName().equalsIgnoreCase( originalRoleName ) )
					needDuplicateTest = false;
			}
			
			if ( needDuplicateTest )
			{
				// Role Name must be unique for the selected application the role belongs to.
				try
				{
					if ( SecurityUtilities.getRole( user.getActiveRole().getRoleName(), user.getActiveRole().getApplicationId(), connection ) != null )
						user.getErrors().add( "The Role Name " + user.getActiveRole().getRoleName() + " already exists for the selected application.  Role Name must be unique for the selected application." );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void validateRoleAssignment( UserBean user, Connection connection )
	{
		if ( user.getActiveRoleAssignment().getAuthenticationProfileId() == 0 )
			user.getErrors().add( "Please select the user for this role assignment." );

		if ( user.getActiveRoleAssignment().getApplicationId() == 0 )
			user.getErrors().add( "Please select the application for this role assignment." );

		if ( user.getActiveRoleAssignment().getDepartmentId() == 0 )
			user.getErrors().add( "Please select the department for this role assignment." );

		if ( user.getActiveRoleAssignment().getRoleId() == 0 )
			user.getErrors().add( "Please select the role for this role assignment." );
		
		if ( user.getErrors().size() == 0 )
		{	// Make sure this is not a duplicate assignment.
			try
			{
				if ( SecurityUtilities.roleAssignmentExists( user.getActiveRoleAssignment().getAuthenticationProfileId(),
															 user.getActiveRoleAssignment().getApplicationId(),
															 user.getActiveRoleAssignment().getDepartmentId(),
															 user.getActiveRoleAssignment().getRoleId(),
															 connection ) )
					user.getErrors().add( "The selected role assignment already exists." );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public static boolean roleAssignmentExists( int authenticationProfileId, int applicationId, int departmentId, int roleId, Connection connection )
	{
		boolean exists = false;
		
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.APPLICATION_USER_DEPARTMENT_ROLE "
		  + "where authentication_profile_id = ? "
		  + "and   application_id = ? "
		  + "and   department_id = ? "
		  + "and   role_id = ? ";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, authenticationProfileId );
	        preparedStatement.setInt( 2, applicationId );
	        preparedStatement.setInt( 3, departmentId );
	        preparedStatement.setInt( 4, roleId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        	exists = true;
	        
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return exists;
	}

	// This method gets an existing Verification Code for the natural PK: authentication_profile_id + application id
	public static VerificationCodeBean getVerificationCode( int authenticationProfileId,
			   												int applicationId,
			   												Connection connection ) throws Exception
	{
		String sql = 
		"select * from APPLICATION_SECURITY.VERIFICATION_CODE "
	  + "where authentication_profile_id = ? "
	  + "and   application_id = ? ";
		PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, authenticationProfileId );
        preparedStatement.setInt( 2, applicationId );
        resultSet = preparedStatement.executeQuery();
        
        VerificationCodeBean verificationCode = null;
        if ( resultSet.next() )
        {
        	verificationCode = new VerificationCodeBean();
        	verificationCode.setVerificationCodeId( resultSet.getInt("verification_code_id") );
        	verificationCode.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
			verificationCode.setApplicationId( resultSet.getInt("application_id") );
			verificationCode.setVerificationCode( resultSet.getString("verification_cd") );
			verificationCode.setCreatedTimestamp( resultSet.getString("created_ts") );
        }
        resultSet.close();
        preparedStatement.close();
        
        return verificationCode;
	}

	public static ArrayList<ApplicationUserDepartmentRoleBean> getAssignedRolesForUser( int authenticationProfileId, Connection connection )
	{
		ArrayList<ApplicationUserDepartmentRoleBean> rolesForUser = new ArrayList<ApplicationUserDepartmentRoleBean>();
		
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.APPLICATION_USER_DEPARTMENT_ROLE "
		  + "where authentication_profile_id = ? "
		  + "order by application_id, department_id, role_id ";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, authenticationProfileId );
	        resultSet = preparedStatement.executeQuery();
	        
	        ApplicationUserDepartmentRoleBean roleForUser = null;
			while ( resultSet.next() )
			{
				roleForUser = new ApplicationUserDepartmentRoleBean();
				roleForUser.setApplicationUserDepartmentRoleId( resultSet.getInt("application_user_department_role_id") );
				roleForUser.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
				roleForUser.setApplicationId( resultSet.getInt("application_id") );
				roleForUser.setDepartmentId( resultSet.getInt("department_id") );
				roleForUser.setRoleId( resultSet.getInt("role_id") );
				rolesForUser.add( roleForUser );
			}
	        
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return rolesForUser;
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

	public static String testIt(String dummy)
	{
		return( "Tested!" );
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
	
	public static DepartmentBean getDepartment( int departmentId, ArrayList<DepartmentBean> allDepartments )
	{
        DepartmentBean department = null;
        
        int i = 0;
        boolean found = false;
        while ( i < allDepartments.size() && ! found )
        {
        	department = allDepartments.get( i );
        	if ( department.getDepartmentId() == departmentId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return department;
        else
        	return null;
	}
	
	public static ApplicationBean getApplication( int applicationId, ArrayList<ApplicationBean> allApplications )
	{
        ApplicationBean application = null;
        
        int i = 0;
        boolean found = false;
        while ( i < allApplications.size() && ! found )
        {
        	application = allApplications.get( i );
        	if ( application.getApplicationId() == applicationId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return application;
        else
        	return null;
	}
	
	public static ApplicationUserDepartmentRoleBean getAssignedRoleForUser( int applicationUserDepartmentRoleId, ArrayList<ApplicationUserDepartmentRoleBean> assignedRolesForUser )
	{
		ApplicationUserDepartmentRoleBean assignedRoleForUser = null;
        
        int i = 0;
        boolean found = false;
        while ( i < assignedRolesForUser.size() && ! found )
        {
        	assignedRoleForUser = assignedRolesForUser.get( i );
        	if ( assignedRoleForUser.getApplicationUserDepartmentRoleId() == applicationUserDepartmentRoleId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return assignedRoleForUser;
        else
        	return null;
	}
	
	public static RoleBean getRole( int roleId, ArrayList<RoleBean> allRoles )
	{
        RoleBean role = null;
        
        int i = 0;
        boolean found = false;
        while ( i < allRoles.size() && ! found )
        {
        	role = allRoles.get( i );
        	if ( role.getRoleId() == roleId )
        		found = true;
        	else
        		i++;
        }
        
        if ( found )
        	return role;
        else
        	return null;
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
	
	public static AuthenticationProfileBean getAuthenticationProfile( String userId, Connection connection )
	{
		AuthenticationProfileBean authenticationProfile = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.AUTHENTICATION_PROFILE "
		  + "where user_id = ? ";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setString( 1, userId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	authenticationProfile = new AuthenticationProfileBean();
	        	authenticationProfile.setAuthenticationProfileId( resultSet.getInt("authentication_profile_id") );
	        	authenticationProfile.setOrganizationId( resultSet.getInt("organization_id") );
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
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return authenticationProfile;
	}
	
	public static DepartmentBean getDepartment( String departmentCode, int organizationId, Connection connection )
	{
		DepartmentBean department = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.DEPARTMENT "
		  + "where department_code = ? "
		  + "and organization_id = ?";
			System.out.println( "sql is " + sql );
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setString( 1, departmentCode );
	        preparedStatement.setInt( 2, organizationId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	department = new DepartmentBean();
	        	department.setDepartmentId( resultSet.getInt( "department_id" ) );
	        	department.setOrganizationId( resultSet.getInt("organization_id") );
	        	department.setDepartmentName( resultSet.getString("department_name") );
	        	department.setDepartmentCode( resultSet.getString("department_code") );
	        	department.setDepartmentDescription( resultSet.getString("department_description") );
	        }
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return department;
	}
	
	public static RoleBean getRole( String roleName, int applicationId, Connection connection )
	{
		RoleBean role = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.ROLE "
		  + "where role_name = ? "
		  + "and application_id = ?";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setString( 1, roleName );
	        preparedStatement.setInt( 2, applicationId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	role = new RoleBean();
	        	role.setRoleId( resultSet.getInt( "role_id" ) );
	        	role.setApplicationId( resultSet.getInt("application_id") );
	        	role.setRoleName( resultSet.getString("role_name") );
	        	role.setRoleDescription( resultSet.getString("role_description") );
	        }
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return role;
	}
	
	public static AuthenticationProfileBean getAuthenticationProfile( int authenticationProfileId, Connection connection )
	{
		AuthenticationProfileBean authenticationProfile = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.AUTHENTICATION_PROFILE "
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
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return authenticationProfile;
	}
	
	public static DepartmentBean getDepartment( int departmentId, Connection connection )
	{
		DepartmentBean department = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.DEPARTMENT "
		  + "where department_id = ? ";
			System.out.println( "sql is " + sql );
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, departmentId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	department = new DepartmentBean();
	        	department.setDepartmentId( resultSet.getInt( "department_id" ) );
	        	department.setOrganizationId( resultSet.getInt("organization_id") );
	        	department.setDepartmentName( resultSet.getString("department_name") );
	        	department.setDepartmentCode( resultSet.getString("department_code") );
	        	department.setDepartmentDescription( resultSet.getString("department_description") );
	        }
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return department;
	}
	
	public static ApplicationBean getApplication( int applicationId, Connection connection )
	{
		ApplicationBean application = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.APPLICATION "
		  + "where application_id = ? ";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, applicationId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	application = new ApplicationBean();
	        	application.setApplicationId( resultSet.getInt( "application_id" ) );
	        	application.setOrganizationId( resultSet.getInt("organization_id") );
	        	application.setApplicationName( resultSet.getString("application_name") );
	        	application.setApplicationCode( resultSet.getString("application_cd") );
	        	application.setApplicationDescription( resultSet.getString("application_description") );
	        	application.setDomainName( resultSet.getString("domain_name") );
	        	application.setOwner( resultSet.getString("owner") );
	        }
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return application;
	}
	
	public static RoleBean getRole( int roleId, Connection connection )
	{
		RoleBean role = null;
		try
		{
			String sql = 
			"select * from APPLICATION_SECURITY.ROLE "
		  + "where role_id = ? ";
			PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;
	        preparedStatement = connection.prepareStatement( sql );
	        preparedStatement.setInt( 1, roleId );
	        resultSet = preparedStatement.executeQuery();
	        
	        if ( resultSet.next() )
	        {
	        	role = new RoleBean();
	        	role.setRoleId( resultSet.getInt( "role_id" ) );
	        	role.setApplicationId( resultSet.getInt( "application_id" ) );
	        	role.setRoleName( resultSet.getString("role_name") );
	        	role.setRoleDescription( resultSet.getString("role_description") );
	        }
	        resultSet.close();
	        preparedStatement.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return role;
	}
	
	public static OrganizationBean getOrganization( String organizationName, Connection connection ) throws SQLException
	{
		String sql = "select * from APPLICATION_SECURITY.ORGANIZATION "
				   + "where UPPER(organization_name) = ?";
		PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setString( 1, organizationName.toUpperCase() );
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

	public static void updateAuthenticationProfile( AuthenticationProfileBean authentication, Connection connection ) throws SQLException
	{
		String sql = "update APPLICATION_SECURITY.AUTHENTICATION_PROFILE set "
				   + " authentication_profile_id=?, "
				   + " organization_id=?,"
				   + " user_id=?,"
				   + " password=?,"
				   + " salt=?,"
				   + " first_name=?,"
				   + " last_name=?,"
				   + " mobile_phone=?, "
				   + " office_phone=?, "
				   + " office_phone_ext=?, "
				   + " home_phone=?, "
				   + " failed_login_attempts=?, "
				   + " verification_code_method=? "
				   + " where authentication_profile_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, authentication.getAuthenticationProfileId() );
        preparedStatement.setInt( 2, authentication.getOrganizationId() );
        preparedStatement.setString( 3, authentication.getUserId() );
        preparedStatement.setString( 4, authentication.getPassword() );
        preparedStatement.setString( 5, authentication.getSalt() );
        preparedStatement.setString( 6, authentication.getFirstName() );
        preparedStatement.setString( 7, authentication.getLastName() );
        preparedStatement.setString( 8, authentication.getMobilePhone() );
        preparedStatement.setString( 9, authentication.getOfficePhone() );
        preparedStatement.setString( 10, authentication.getOfficePhoneExt() );
        preparedStatement.setString( 11, authentication.getHomePhone() );
        preparedStatement.setInt( 12, authentication.getFailedLoginAttempts() );
        preparedStatement.setString( 13, authentication.getVerificationCodeMethod() );
        preparedStatement.setInt( 14, authentication.getAuthenticationProfileId() );
        preparedStatement.executeUpdate();
        
        preparedStatement.close();
	}

	public static void updateDepartment( DepartmentBean department, Connection connection ) throws SQLException
	{
		String sql = "update APPLICATION_SECURITY.DEPARTMENT set "
				   + " department_id=?, "
				   + " organization_id=?,"
				   + " department_name=?,"
				   + " department_code=?,"
				   + " department_description=?"
				   + " where department_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, department.getDepartmentId() );
        preparedStatement.setInt( 2, department.getOrganizationId() );
        preparedStatement.setString( 3, department.getDepartmentName() );
        preparedStatement.setString( 4, department.getDepartmentCode() );
        preparedStatement.setString( 5, department.getDepartmentDescription() );
        preparedStatement.setInt( 6, department.getDepartmentId() );
        preparedStatement.executeUpdate();
        
        preparedStatement.close();
	}

	public static void updateApplication( ApplicationBean application, Connection connection ) throws SQLException
	{
		String sql = "update APPLICATION_SECURITY.APPLICATION set "
				   + " application_id=?, "
				   + " organization_id=?,"
				   + " application_cd=?,"
				   + " application_name=?,"
				   + " domain_name=?,"
				   + " application_description=?,"
				   + " owner=?"
				   + " where application_id = ?";
		
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, application.getApplicationId() );
        preparedStatement.setInt( 2, application.getOrganizationId() );
        preparedStatement.setString( 3, application.getApplicationCode() );
        preparedStatement.setString( 4, application.getApplicationName() );
        preparedStatement.setString( 5, application.getDomainName() );
        preparedStatement.setString( 6, application.getApplicationDescription() );
        preparedStatement.setString( 7, application.getOwner() );
        preparedStatement.setInt( 8, application.getApplicationId() );
        preparedStatement.executeUpdate();
        
        preparedStatement.close();
	}

	public static void updateRole( RoleBean role, Connection connection ) throws SQLException
	{
		String sql = "update APPLICATION_SECURITY.ROLE set "
				   + " role_id=?, "
				   + " application_id=?,"
				   + " role_name=?,"
				   + " role_description=?"
				   + " where role_id = ?";
		
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, role.getRoleId() );
        preparedStatement.setInt( 2, role.getApplicationId() );
        preparedStatement.setString( 3, role.getRoleName() );
        preparedStatement.setString( 4, role.getRoleDescription() );
        preparedStatement.setInt( 5, role.getRoleId() );
        preparedStatement.executeUpdate();
        
        preparedStatement.close();
	}
	
	public static void deleteOrganization( int organizationId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.ORGANIZATION "
				   + "where organization_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}

	public static void deleteVerificationCode( int verificationCodeId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.VERIFICATION_CODE "
				   + "where verification_code_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, verificationCodeId );
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

	public static ArrayList<AuthenticationProfileBean> getAllAuthenticationProfiles( int organizationId, Connection connection ) throws SQLException
	{
		ArrayList<AuthenticationProfileBean> allAuthenticationProfiles = new ArrayList<AuthenticationProfileBean>();
		String sql = "select * from APPLICATION_SECURITY.AUTHENTICATION_PROFILE "
				   + "where organization_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
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

	public static ArrayList<DepartmentBean> getAllDepartments( int organizationId, Connection connection ) throws SQLException
	{
		ArrayList<DepartmentBean> allDepartments = new ArrayList<DepartmentBean>();
		String sql = "select * from APPLICATION_SECURITY.DEPARTMENT "
				   + "where organization_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
        resultSet = preparedStatement.executeQuery();
		DepartmentBean department = null;
		while ( resultSet.next() )
		{
			department = new DepartmentBean();
			department.setDepartmentId( resultSet.getInt("department_id") );
			department.setOrganizationId( resultSet.getInt("organization_id") );
			department.setDepartmentName( resultSet.getString("department_name") );
			department.setDepartmentCode( resultSet.getString("department_code") );
			department.setDepartmentDescription( resultSet.getString("department_description") );
			allDepartments.add( department );
		}
		resultSet.close();
		preparedStatement.close();
		return allDepartments;
	}

	public static ArrayList<ApplicationBean> getAllApplications( int organizationId, Connection connection ) throws SQLException
	{
		ArrayList<ApplicationBean> allApplications = new ArrayList<ApplicationBean>();
		String sql = "select * from APPLICATION_SECURITY.APPLICATION "
				   + "where organization_id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, organizationId );
		resultSet = preparedStatement.executeQuery();
		ApplicationBean application = null;
		while ( resultSet.next() )
		{
			application = new ApplicationBean();
        	application.setApplicationId( resultSet.getInt( "application_id" ) );
        	application.setOrganizationId( resultSet.getInt("organization_id") );
        	application.setApplicationName( resultSet.getString("application_name") );
        	application.setApplicationCode( resultSet.getString("application_cd") );
        	application.setApplicationDescription( resultSet.getString("application_description") );
        	application.setDomainName( resultSet.getString("domain_name") );
        	application.setOwner( resultSet.getString("owner") );
			allApplications.add( application );
		}
		resultSet.close();
		preparedStatement.close();
		return allApplications;
	}

	public static ApplicationBean getApplication( String applicationCode, Connection connection ) throws SQLException
	{
		String sql = "select * from APPLICATION_SECURITY.APPLICATION "
				   + "where application_cd = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setString( 1, applicationCode );
		resultSet = preparedStatement.executeQuery();
		ApplicationBean application = null;
		if ( resultSet.next() )
		{
			application = new ApplicationBean();
        	application.setApplicationId( resultSet.getInt( "application_id" ) );
        	application.setOrganizationId( resultSet.getInt("organization_id") );
        	application.setApplicationName( resultSet.getString("application_name") );
        	application.setApplicationCode( resultSet.getString("application_cd") );
        	application.setApplicationDescription( resultSet.getString("application_description") );
        	application.setDomainName( resultSet.getString("domain_name") );
        	application.setOwner( resultSet.getString("owner") );
		}
		resultSet.close();
		preparedStatement.close();
		return application;
	}

	public static ArrayList<RoleBean> getAllRolesForApplication( int applicationId, ArrayList<RoleBean> allRolesForAllApplications )
	{
		ArrayList<RoleBean> allRolesForApplication = new ArrayList<RoleBean>();
		
		for ( RoleBean role : allRolesForAllApplications )
			if ( role.getApplicationId() == applicationId )
				allRolesForApplication.add( role );
		
		return allRolesForApplication;
	}

	public static ArrayList<RoleBean> getAllRolesForAllApplications( Connection connection ) throws SQLException
	{
		ArrayList<RoleBean> allRoles = new ArrayList<RoleBean>();
		String sql = "select * from APPLICATION_SECURITY.ROLE ";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection.prepareStatement( sql );
		resultSet = preparedStatement.executeQuery();
		RoleBean role = null;
		while ( resultSet.next() )
		{
			role = new RoleBean();
        	role.setRoleId( resultSet.getInt( "role_id" ) );
        	role.setApplicationId( resultSet.getInt( "application_id" ) );
        	role.setRoleName( resultSet.getString("role_name") );
        	role.setRoleDescription( resultSet.getString("role_description") );
			allRoles.add( role );
		}
		resultSet.close();
		preparedStatement.close();
		return allRoles;
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

	public static void deleteDepartment( int departmentId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.DEPARTMENT "
				   + "where department_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, departmentId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}

	public static void deleteApplication( int applicationId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.APPLICATION "
				   + "where application_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, applicationId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}

	public static void deleteRole( int roleId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.ROLE "
				   + "where role_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, roleId );
        preparedStatement.executeUpdate();
        preparedStatement.close();
	}

	public static void deleteRoleAssignment( int applicationUserDepartmentRoleId, Connection connection ) throws SQLException
	{
		String sql = "delete from APPLICATION_SECURITY.APPLICATION_USER_DEPARTMENT_ROLE "
				   + "where application_user_department_role_id = ?";
		PreparedStatement preparedStatement = connection.prepareStatement( sql );
        preparedStatement.setInt( 1, applicationUserDepartmentRoleId );
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

	public static void addNewDepartment( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.DEPARTMENT "
				   + "("
				   + "organization_id, "
				   + "department_name,"
				   + "department_code,"
				   + "department_description"
				   + ") "
				   + "values( ?,?,?,? )";
		System.out.println( "sql is " + sql );
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
		System.out.println( "OrganizationId is " + user.getActiveDepartment().getOrganizationId() );
        preparedStatement.setInt( 1, user.getActiveDepartment().getOrganizationId() );
        preparedStatement.setString( 2, user.getActiveDepartment().getDepartmentName() );
        preparedStatement.setString( 3, user.getActiveDepartment().getDepartmentCode() );
        preparedStatement.setString( 4, user.getActiveDepartment().getDepartmentDescription() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        System.out.println( "New primary key: " + newPrimaryKey );
	        user.getActiveDepartment().setDepartmentId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void addNewApplication( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.APPLICATION "
				   + "("
				   + "organization_id, "
				   + "application_cd, "
				   + "application_name, "
				   + "domain_name, "
				   + "application_description, "
				   + "owner"
				   + ") "
				   + "values( ?,?,?,?,?,? )";

		
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setInt( 1, user.getActiveApplication().getOrganizationId() );
        preparedStatement.setString( 2, user.getActiveApplication().getApplicationCode() );
        preparedStatement.setString( 3, user.getActiveApplication().getApplicationName() );
        preparedStatement.setString( 4, user.getActiveApplication().getDomainName() );
        preparedStatement.setString( 5, user.getActiveApplication().getApplicationDescription() );
        preparedStatement.setString( 6, user.getActiveApplication().getOwner() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        user.getActiveApplication().setApplicationId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void addNewRole( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.ROLE "
				   + "("
				   + "application_id, "
				   + "role_name, "
				   + "role_description "
				   + ") "
				   + "values( ?,?,? )";

		
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setInt( 1, user.getActiveRole().getApplicationId() );
        preparedStatement.setString( 2, user.getActiveRole().getRoleName() );
        preparedStatement.setString( 3, user.getActiveRole().getRoleDescription() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        user.getActiveRole().setRoleId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void addNewVerificationCode( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.VERIFICATION_CODE "
				   + "("
				   + "authentication_profile_id, "
				   + "application_id, "
				   + "verification_cd, "
				   + "created_ts "
				   + ") "
				   + "values( ?,?,?,? )";

		
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setInt( 1, user.getActiveVerificationCode().getAuthenticationProfileId() );
        preparedStatement.setInt( 2, user.getActiveVerificationCode().getApplicationId() );
        preparedStatement.setString( 3, user.getActiveVerificationCode().getVerificationCode() );
        String currentTimestamp = SecurityUtilities.getCurrentTimestamp();
        user.getActiveVerificationCode().setCreatedTimestamp( currentTimestamp );
        preparedStatement.setString( 4, user.getActiveVerificationCode().getCreatedTimestamp() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        user.getActiveVerificationCode().setVerificationCodeId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void addNewRoleAssignment( UserBean user, Connection connection ) throws SQLException
	{
		String sql = "insert into APPLICATION_SECURITY.APPLICATION_USER_DEPARTMENT_ROLE "
				   + "("
				   + "authentication_profile_id, "
				   + "application_id, "
				   + "department_id, "
				   + "role_id "
				   + ") "
				   + "values( ?,?,?,? )";

		
		PreparedStatement preparedStatement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setInt( 1, user.getActiveRoleAssignment().getAuthenticationProfileId() );
        preparedStatement.setInt( 2, user.getActiveRoleAssignment().getApplicationId() );
        preparedStatement.setInt( 3, user.getActiveRoleAssignment().getDepartmentId() );
        preparedStatement.setInt( 4, user.getActiveRoleAssignment().getRoleId() );
        preparedStatement.executeUpdate();
        
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if ( rs.next() )
        {
	        int newPrimaryKey = rs.getInt( 1 );
	        user.getActiveRoleAssignment().setApplicationUserDepartmentRoleId( newPrimaryKey );
        }
        rs.close();
        preparedStatement.close();
	}

	public static void createSecurePasswordAndSalt( AuthenticationProfileBean authentication ) throws Exception
	{
        // 1. Generate a random salt
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes( saltBytes );
        String salt = Base64.getEncoder().encodeToString( saltBytes );

        // 2. Hash the password with the salt
        String hashedPassword = hashPassword( authentication.getTextPassword(), saltBytes );

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

	
	public static String getSecureLogin( String username, String password )
	{
		// Return JSON representation of the ticker profile.
		String returnList = null;
        try 
        {
            // 1. Define the URL of the REST endpoint
        	String endpointUrl = "http://localhost/securityservices/rest/security/authentication";
        	System.out.println( "SecurityUtilities: endpoint: " + endpointUrl );
            @SuppressWarnings("deprecation")
			URL url = new URL( endpointUrl );

            // 2. Open a connection
            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();

            // 3. Set the request method (e.g., GET, POST, PUT, DELETE)
            connection.setRequestMethod( "GET" );

            // 4. Set request headers (optional, but often necessary for content type, authorization, etc.)
            connection.setRequestProperty("Accept", "application/json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "userId", username );
            jsonObject.put( "password", password );
            
            connection.setRequestProperty( "input", jsonObject.toJSONString() );
            
            // 5. Get the response code
            System.out.println( "SecurityUtilities.gateSucureLogin" );
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 6. Read the response
            if ( responseCode == HttpURLConnection.HTTP_OK ) 
            {
                BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ( ( inputLine = in.readLine() ) != null ) 
                {
                    content.append( inputLine );
                }
                in.close();
                returnList = content.toString();
//                System.out.println( "Response Body: " + content.toString() );
            } 
            else 
            {
                System.out.println( "Error in GET request: " + responseCode );
            }

            // 7. Disconnect the connection
            connection.disconnect();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		return returnList;
	}
	
	public static String sendText( UserBean user )
	{
		String response = null;
        try 
        {
            // 1. Define the URL of the REST endpoint
        	String endpointUrl = "http://localhost/securityservices/rest/security/text";
        	System.out.println( "SecurityUtilities: Security utilities endpoint that calls third party rest service for text: " + endpointUrl );
            @SuppressWarnings("deprecation")
			URL url = new URL( endpointUrl );

            // 2. Open a connection
            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();

            // 3. Set the request method (e.g., GET, POST, PUT, DELETE)
            connection.setRequestMethod( "POST" );

            // 4. Set request headers (optional, but often necessary for content type, authorization, etc.)
            connection.setRequestProperty("Accept", "application/json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "phoneNumber", user.getLoginAuthenticationProfile().getMobilePhone() );
            jsonObject.put( "textMessage", "Enter the 6 digit code sent to you on the code entry page provided."  );
            jsonObject.put( "endpointUrl", "https://sendtext"  );
            
            connection.setRequestProperty( "input", jsonObject.toJSONString() );
            
            // 5. Get the response code
            System.out.println( "SecurityUtilities.sendText" );
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 6. Read the response
            if ( responseCode == HttpURLConnection.HTTP_OK ) 
            {
                BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ( ( inputLine = in.readLine() ) != null ) 
                {
                    content.append( inputLine );
                }
                in.close();
                response = content.toString();
                System.out.println( "Response Body: " + content.toString() );
            } 
            else 
            {
                System.out.println( "Error in GET request: " + responseCode );
            }

            // 7. Disconnect the connection
            connection.disconnect();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		return response;
	}

	public static String getRoles( String userId, String applicationCode )
	{
		// Return JSON representation of the ticker profile.
		String returnList = null;
        try 
        {
            // 1. Define the URL of the REST endpoint
        	String endpointUrl = "http://localhost/securityservices/rest/security/roles";
        	System.out.println( "endpoint: " + endpointUrl );
            @SuppressWarnings("deprecation")
			URL url = new URL( endpointUrl );

            // 2. Open a connection
            HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();

            // 3. Set the request method (e.g., GET, POST, PUT, DELETE)
            connection.setRequestMethod( "GET" );

            // 4. Set request headers (optional, but often necessary for content type, authorization, etc.)
            connection.setRequestProperty("Accept", "application/json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "userId", userId );
            jsonObject.put( "applicationCode", applicationCode );
            
            connection.setRequestProperty( "input", jsonObject.toJSONString() );
            
            // 5. Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 6. Read the response
            if ( responseCode == HttpURLConnection.HTTP_OK ) 
            {
                BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ( ( inputLine = in.readLine() ) != null ) 
                {
                    content.append( inputLine );
                }
                in.close();
                returnList = content.toString();
//                System.out.println( "Response Body: " + content.toString() );
            } 
            else 
            {
                System.out.println( "Error in GET request: " + responseCode );
            }

            // 7. Disconnect the connection
            connection.disconnect();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		return returnList;
	}
	
	public static AuthenticationProfileBean loadAuthenticationProfileBean( String authenticationProfileJson )
	{
		
		AuthenticationProfileBean profile = null;
		
		try
		{
			JSONParser parser = new JSONParser();
			JSONArray jsonArray = ( JSONArray ) parser.parse( authenticationProfileJson );
			for ( Object obj : jsonArray ) 
			{
				profile = new AuthenticationProfileBean();
				
				JSONObject jsonObject = ( JSONObject ) obj; // Each element is a JSONObject

				long authenticationProfileId = ( long ) jsonObject.get("authenticationProfileId");
				profile.setAuthenticationProfileId( ( int ) authenticationProfileId );
				long organizationId = ( long ) jsonObject.get("organizationId");
				profile.setOrganizationId( ( int ) organizationId );
				profile.setUserId( ( String ) jsonObject.get( "userId" ) );
				profile.setFirstName( ( String ) jsonObject.get( "firstName" ) );
				profile.setLastName( ( String ) jsonObject.get( "lastName" ) );
				profile.setMobilePhone( ( String ) jsonObject.get( "mobilePhone" ) );
				System.out.println( "MOBILE: " + profile.getMobilePhone() );
				profile.setOfficePhone( ( String ) jsonObject.get( "officePhone" ) );
				profile.setOfficePhoneExt( ( String ) jsonObject.get( "officePhoneExt" ) );
				profile.setHomePhone( ( String ) jsonObject.get( "homePhone" ) );
				profile.setVerificationCodeMethod( ( String ) jsonObject.get( "verificationCodeMethod" ) );
				long failedLoginAttempts = ( long ) jsonObject.get("failedLoginAttempts");
				profile.setFailedLoginAttempts( ( int ) failedLoginAttempts );
				profile.setClientResult( ( String ) jsonObject.get( "clientResult" ) );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return profile;
	}
	
	public static void loadAuthenticationProfileBeanWithRoles( AuthenticationProfileBean profile, String rolesJson )
	{
		try
		{
			profile.getRolesForDepartment().clear();
			JSONParser parser = new JSONParser();
			JSONArray departmentArray = ( JSONArray ) parser.parse( rolesJson );
			for ( Object obj : departmentArray ) 
			{
				ArrayList<String> tempRoleArray = new ArrayList<String>();
				JSONObject jsonDepartmentObject = ( JSONObject ) obj; // Each element is a JSONObject
				String department = ( String ) jsonDepartmentObject.get( "department" );
				
				JSONArray roleArray = (JSONArray) jsonDepartmentObject.get( "roles" );
				for ( Object roleObj : roleArray )
				{
					JSONObject jsonRoleObject = ( JSONObject ) roleObj;
					String roleName = ( String ) jsonRoleObject.get( "role" );
					tempRoleArray.add( roleName );
				}
				profile.getRolesForDepartment().put( department, tempRoleArray );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public static String generateAuthenticationCode()
	{
		SecureRandom random = new SecureRandom();
        // Generate a number between 0 and 999999
        int number = random.nextInt( 1000000 ); 
        
        // Format to ensure 6 digits with leading zeros if necessary
        String code = String.format("%06d", number);
        
        System.out.println("Your 6-digit code: " + code);
        return code;
	}
	
	public static void sendCodeEmail( UserBean user )
	{
		System.out.println( "EMAIL: authentication code is " + user.getActiveVerificationCode().getVerificationCode() );
	}

	public static void sendCodeText( UserBean user )
	{
		System.out.println( "TEXT: authentication code is " + user.getActiveVerificationCode().getVerificationCode() );
		System.out.println( "TEXT: send text to " + user.getActiveAuthenticationProfile().getMobilePhone() );
		if ( SecurityUtilities.isEmpty( user.getLoginAuthenticationProfile().getMobilePhone() ) )
			user.getErrors().add( "FATAL ERROR: No mobile phone on the profile." );
		else
			SecurityUtilities.sendText( user );
	}
	
	public static String getCurrentTimestamp()
	{
		Instant instant = Instant.now();
		ZoneId desiredZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = instant.atZone(desiredZone);
		
        System.out.println("ZonedDateTime in " + desiredZone + ": " + zonedDateTime);

		String convertedTimestamp = zonedDateTime.toString();
		return convertedTimestamp;
	}
}
