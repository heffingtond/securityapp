<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://securityapp.com" prefix="myfn" %>

<html>
<head>

<style type="text/css" media="all">
@import "/securityapp/css/navbar.css";
@import "/securityapp/css/pageFormat.css";
@import "/securityapp/css/header.css";
</style>

<meta charset="UTF-8">
<title>Roles Assignment</title>
</head>
<body>
	<%@ include file="header.jsp" %>
	<div id="content">
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<ul>
	  		<li><a href="?OrganizationAction=yes">Organization</a></li>
	  		<li><a href="?AuthenticationProfileAction=yes">AuthenticationProfile</a></li>
	  		<li><a href="?DepartmentAction=yes">Department</a></li>
	  		<li><a href="?ApplicationAction=yes">Application</a></li>
	  		<li><a href="?RoleAction=yes">Role</a></li>
		  	<li><a href="#here">Role Assignment</a></li>
		  	<li><a href="?LogoutAction=yes">Logout</a></li>
		</ul>
		<div id="errors">
	       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
	   			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
	       			<c:out value="${error}"/><br/><br/>
	   			</c:forEach>
	   			${sessionScope.UserBean.clearErrors}
	       	</c:if>
	    </div>   	
		<h2 align="center">Role Assignment</h2>
		<div id="inputField">
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName != 'Watchtower'}">
				<p>
				Organization: ${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
		    	</p>
			</c:if>
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName == 'Watchtower'}">
				<p>
					<strong>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.organizationId == 0}">
						Please select an organization to continue
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.organizationId != 0}">
						Selected Organization: ${myfn:getOrganization( sessionScope.UserBean.activeRoleAssignment.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</c:if>
					</strong>
			    </p>
			    <p>
			    <label for="organizationId">Select the organization:</label>
				<select name="organizationId" id="organizationId">
					<option value="SELECT">SELECT</option>
					<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
						<c:if test="${sessionScope.UserBean.activeRoleAssignment.organizationId == organization.organizationId}">
							<option selected value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
						<c:if test="${sessionScope.UserBean.activeRoleAssignment.organizationId != organization.organizationId}">
							<option value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
					</c:forEach>
				</select>
				<button type="submit" value="selectOrganization" name="RoleAssignmentAction">Select Organization</button>
				</p>
			</c:if>


		    <p>
		    	<strong>
				<c:if test="${sessionScope.UserBean.activeRoleAssignment.authenticationProfileId == 0}">
					Please select a user to continue
				</c:if>
				<c:if test="${sessionScope.UserBean.activeRoleAssignment.authenticationProfileId != 0}">
					Selected User: ${myfn:getAuthenticationProfile( sessionScope.UserBean.activeRoleAssignment.authenticationProfileId, sessionScope.UserBean.allAuthenticationProfiles ).userId}
				</c:if>
				</strong>
		    </p>
			<p>
		    <label for="authenticationProfileId">Select the user:</label>
			<select name="authenticationProfileId" id="authenticationProfileId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="authenticationProfile" items="${sessionScope.UserBean.allAuthenticationProfiles}">
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.authenticationProfileId == authenticationProfile.authenticationProfileId}">
						<option selected value="${authenticationProfile.authenticationProfileId}">${authenticationProfile.lastName}, ${authenticationProfile.firstName} - ${authenticationProfile.userId}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.authenticationProfileId != authenticationProfile.authenticationProfileId}">
						<option value="${authenticationProfile.authenticationProfileId}">${authenticationProfile.lastName}, ${authenticationProfile.firstName} - ${authenticationProfile.userId}</option>
					</c:if>
				</c:forEach>
			</select>
		    <button type="submit" value="selectUser" name="RoleAssignmentAction">Select User</button>
		    </p>

		    <p>
		    	<strong>
				<c:if test="${sessionScope.UserBean.activeRoleAssignment.applicationId == 0}">
					Please select the application to continue
				</c:if>
				<c:if test="${sessionScope.UserBean.activeRoleAssignment.applicationId != 0}">
					Selected Application: ${myfn:getApplication( sessionScope.UserBean.activeRoleAssignment.applicationId, sessionScope.UserBean.allApplications ).applicationCode}-${myfn:getApplication( sessionScope.UserBean.activeRoleAssignment.applicationId, sessionScope.UserBean.allApplications ).applicationName}
				</c:if>
				</strong>
		    </p>
			<p>
		    <label for="applicationId">Select the application:</label>
			<select name="applicationId" id="applicationId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="application" items="${sessionScope.UserBean.allApplications}">
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.applicationId == application.applicationId}">
						<option selected value="${application.applicationId}">${application.applicationCode} ${application.applicationName}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.applicationId != application.applicationId}">
						<option value="${application.applicationId}">${application.applicationCode} ${application.applicationName}</option>
					</c:if>
				</c:forEach>
			</select>
		    <button type="submit" value="selectApplication" name="RoleAssignmentAction">Select Application</button>
		    </p>
			<p>
		    <label for="departmentId">Select the Department:</label>
			<select name="departmentId" id="departmentId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="department" items="${sessionScope.UserBean.allDepartments}">
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.departmentId == department.departmentId}">
						<option selected value="${department.departmentId}">${department.departmentCode} ${department.departmentName}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.applicationId != application.applicationId}">
						<option value="${department.departmentId}">${department.departmentCode} ${department.departmentName}</option>
					</c:if>
				</c:forEach>
			</select>
		    </p>
			<p>
		    <label for="roleId">Select the role(s):</label>
			<select name="roleId" id="roleId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="role" items="${sessionScope.UserBean.allRolesForApplication}">
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.roleId == role.roleId}">
						<option selected value="${role.roleId}">${role.roleName}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRoleAssignment.roleId != role.roleId}">
						<option value="${role.roleId}">${role.roleName}</option>
					</c:if>
				</c:forEach>
			</select>
		    </p>
		    <p>
		    <button type="submit" value="addRoleAssignment" name="RoleAssignmentAction">Add Role</button>
		    </p>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.assignedRolesForUser) > 0}">
				<h2 align="center">Current Role Assignments for ${myfn:getAuthenticationProfile( sessionScope.UserBean.activeRoleAssignment.authenticationProfileId, sessionScope.UserBean.allAuthenticationProfiles ).lastName}, ${myfn:getAuthenticationProfile( sessionScope.UserBean.activeRoleAssignment.authenticationProfileId, sessionScope.UserBean.allAuthenticationProfiles ).firstName}</h2>
				<table class="center">
					<tr>
						<th style="width:5%">
						</th>
						<th style="width:30%">
							User
						</th>
						<th style="width:35%">
							Application
						</th>
						<th style="width:20%">
							Department
						</th>
						<th style="width:10%">
							Role
						</th>
					</tr>
					<c:forEach var="assignedRole" items="${sessionScope.UserBean.assignedRolesForUser}">
						<tr>
							<td style="width:5%">
								<button type="submit" value="delete_${assignedRole.applicationUserDepartmentRoleId}" name="RoleAssignmentAction">del</button>
							</td>
							<td style="width:30%">
								${myfn:getAuthenticationProfile( assignedRole.authenticationProfileId, sessionScope.UserBean.allAuthenticationProfiles ).userId}
							</td>
							<td style="width:35%">
								${myfn:getApplication( assignedRole.applicationId, sessionScope.UserBean.allApplications ).applicationCode}-${myfn:getApplication( assignedRole.applicationId, sessionScope.UserBean.allApplications ).applicationName}
							</td>
							<td style="width:20%">
								${myfn:getDepartment( assignedRole.departmentId, sessionScope.UserBean.allDepartments ).departmentCode}
							</td>
							<td style="width:10%">
								${myfn:getRole( assignedRole.roleId, sessionScope.UserBean.allRolesForAllApplications ).roleName}
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>		
		</form>
		
    </div>
	
</body>
</html>