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
<title>Maintain Roles</title>
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
		  	<li><a href="#here">Role</a></li>
	  		<li><a href="?RoleAssignmentAction=yes">Role Assignment</a></li>
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
		<h2 align="center">Role</h2>
		<div id="inputField">
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName != 'Watchtower'}">
				<p>
				Organization: ${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
		    	</p>
			</c:if>
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName == 'Watchtower'}">
				<p>
					<strong>
					<c:if test="${sessionScope.UserBean.activeRole.organizationId == 0}">
						Please select an organization to continue
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRole.organizationId != 0}">
						Selected Organization: ${myfn:getOrganization( sessionScope.UserBean.activeRole.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</c:if>
					</strong>
			    </p>
			    <p>
			    <label for="organizationId">Select the organization:</label>
				<select name="organizationId" id="organizationId">
					<option value="SELECT">SELECT</option>
					<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
						<c:if test="${sessionScope.UserBean.activeRole.organizationId == organization.organizationId}">
							<option selected value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
						<c:if test="${sessionScope.UserBean.activeRole.organizationId != organization.organizationId}">
							<option value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
					</c:forEach>
				</select>
				<button type="submit" value="selectOrganization" name="RoleAction">Select Organization</button>
				</p>
			</c:if>
			<p>
				<strong>
				<c:if test="${sessionScope.UserBean.activeRole.applicationId == 0}">
					Please select the application to continue
				</c:if>
				<c:if test="${sessionScope.UserBean.activeRole.applicationId != 0}">
					Selected Application: ${myfn:getApplication( sessionScope.UserBean.activeRole.applicationId, sessionScope.UserBean.allApplications ).applicationCode}-${myfn:getApplication( sessionScope.UserBean.activeRole.applicationId, sessionScope.UserBean.allApplications ).applicationName}
				</c:if>
				</strong>
		    </p>
		    <p>
		    <label for="applicationId">Select the application:</label>
			<select name="applicationId" id="applicationId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="application" items="${sessionScope.UserBean.allApplications}">
					<c:if test="${sessionScope.UserBean.activeRole.applicationId == application.applicationId}">
						<option selected value="${application.applicationId}">${application.applicationCode} - ${application.applicationName}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeRole.organizationId != organization.organizationId}">
						<option value="${application.applicationId}">${application.applicationCode} - ${application.applicationName}</option>
					</c:if>
				</c:forEach>
			</select>
			<button type="submit" value="selectApplication" name="RoleAction">Select Application</button>
			</p>

		    <p>
			<label for="roleName">Role Name:</label>
		    <input type="text" id="roleName" name="roleName" value="${sessionScope.UserBean.activeRole.roleName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="roleDescription">Role Description:</label>
		    <textarea id="roleDescription" name="roleDescription" rows="5" cols="60" maxlength="5000" placeholder="Description for this role">${sessionScope.UserBean.activeRole.roleDescription}</textarea><br>
		    </p>
		    <p>
		    <button type="submit" value="addRole" name="RoleAction">Add Role</button>
		    </p>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.allRolesForApplication) > 0}">
				<table class="center">
					<tr>
						<th style="width:10%">
						</th>
						<th style="width:30%">
							Application Code
						</th>
						<th style="width:60%">
							Role Name
						</th>
					</tr>
					<c:forEach var="role" items="${sessionScope.UserBean.allRolesForApplication}">
						<tr>
							<td style="width:10%">
								<button type="submit" value="delete_${role.roleId}" name="RoleAction">del</button>
								<button type="submit" value="edit_${role.roleId}" name="RoleAction">Edit</button>
							</td>
							<td style="width:30%">
								${myfn:getApplication( role.applicationId, sessionScope.UserBean.allApplications ).applicationCode}
							</td>
							<td style="width:60%">
								${role.roleName}
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>		
		</form>
		
    </div>
	
</body>
</html>