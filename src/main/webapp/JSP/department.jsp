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
<title>Maintain Authentication Profiles</title>
</head>
<body>
	<%@ include file="header.jsp" %>
	<div id="content">
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<ul>
	  		<li><a href="?OrganizationAction=yes">Organization</a></li>
	  		<li><a href="?AuthenticationProfileAction=yes">AuthenticationProfile</a></li>
		  	<li><a href="#here">Department</a></li>
		  	<li><a href="?ApplicationAction=yes">Application</a></li>
	  		<li><a href="?RoleAction=yes">Role</a></li>
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
		<h2 align="center">Department</h2>
		<div id="inputField">
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName == 'Watchtower'}">
				<p>
					<strong>
					<c:if test="${sessionScope.UserBean.activeDepartment.organizationId == 0}">
						Please select an organization to continue
					</c:if>
					<c:if test="${sessionScope.UserBean.activeDepartment.organizationId != 0}">
						Selected Organization: ${myfn:getOrganization( sessionScope.UserBean.activeDepartment.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</c:if>
					</strong>
			    </p>
			    <p>
			    <label for="organizationId">Select the organization:</label>
				<select name="organizationId" id="organizationId">
					<option value="SELECT">SELECT</option>
					<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
						<c:if test="${sessionScope.UserBean.activeDepartment.organizationId == organization.organizationId}">
							<option selected value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
						<c:if test="${sessionScope.UserBean.activeDepartment.organizationId != organization.organizationId}">
							<option value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
					</c:forEach>
				</select>
				<button type="submit" value="selectOrganization" name="DepartmentAction">Select Organization</button>
				</p>
			</c:if>
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName != 'Watchtower'}">
				<p>
				Organization: ${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
		    	</p>
			</c:if>
		    <p>
			<label for="departmentName">Department Name:</label>
		    <input type="text" id="departmentName" name="departmentName" value="${sessionScope.UserBean.activeDepartment.departmentName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="departmentCode">Department Code:</label>
		    <input type="text" id="departmentCode" name="departmentCode" value="${sessionScope.UserBean.activeDepartment.departmentCode}" size="20" maxlength="20"><br>
		    </p>
		    <p>
			<label for="departmentDescription">Department Description:</label>
		    <textarea id="departmentDescription" name="departmentDescription" rows="5" cols="60" maxlength="5000" placeholder="Description of the department">${sessionScope.UserBean.activeDepartment.departmentDescription}</textarea><br>
		    </p>
		    <p>
		    <button type="submit" value="addDepartment" name="DepartmentAction">Add Department</button>
		    </p>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.allDepartments) > 0}">
				<table class="center">
					<tr>
						<th style="width:10%">
						</th>
						<th style="width:30%">
							Organization
						</th>
						<th style="width:20%">
							Dept. Code
						</th>
						<th style="width:40%">
							Dept. Name
						</th>
					</tr>
					<c:forEach var="department" items="${sessionScope.UserBean.allDepartments}">
						<tr>
							<td style="width:10%">
								<button type="submit" value="delete_${department.departmentId}" name="DepartmentAction">del</button>
								<button type="submit" value="edit_${department.departmentId}" name="DepartmentAction">Edit</button>
							</td>
							<td style="width:30%">
								${myfn:getOrganization( department.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
							</td>
							<td style="width:20%">
								${department.departmentCode}
							</td>
							<td style="width:40%">
								${department.departmentName}
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>		
		</form>
		
    </div>
	
</body>
</html>