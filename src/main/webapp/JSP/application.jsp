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
<title>Maintain Applications</title>
</head>
<body>
	<%@ include file="header.jsp" %>
	<div id="content">
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<ul>
	  		<li><a href="?OrganizationAction=yes">Organization</a></li>
	  		<li><a href="?AuthenticationProfileAction=yes">AuthenticationProfile</a></li>
	  		<li><a href="?DepartmentAction=yes">Department</a></li>
		  	<li><a href="#here">Application</a></li>
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
		<h2 align="center">Application</h2>
		<div id="inputField">
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName == 'Watchtower'}">
				<p>
					<strong>
					<c:if test="${sessionScope.UserBean.activeApplication.organizationId == 0}">
						Please select an organization to continue
					</c:if>
					<c:if test="${sessionScope.UserBean.activeApplication.organizationId != 0}">
						Selected Organization: ${myfn:getOrganization( sessionScope.UserBean.activeApplication.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</c:if>
					</strong>
			    </p>
			    <p>
			    <label for="organizationId">Select the organization:</label>
				<select name="organizationId" id="organizationId">
					<option value="SELECT">SELECT</option>
					<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
						<c:if test="${sessionScope.UserBean.activeApplication.organizationId == organization.organizationId}">
							<option selected value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
						<c:if test="${sessionScope.UserBean.activeApplication.organizationId != organization.organizationId}">
							<option value="${organization.organizationId}">${organization.organizationName}</option>
						</c:if>
					</c:forEach>
				</select>
				<button type="submit" value="selectOrganization" name="ApplicationAction">Select Organization</button>
				</p>
			</c:if>
			<c:if test="${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName != 'Watchtower'}">
				<p>
				Organization: ${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
		    	</p>
			</c:if>
		    <p>
			<label for="applicationCode">Application Code:</label>
		    <input type="text" id="applicationCode" name="applicationCode" value="${sessionScope.UserBean.activeApplication.applicationCode}" size="50" maxlength="50"><br>
		    </p>
		    <p>
			<label for="applicationName">Application Name:</label>
		    <input type="text" id="applicationName" name="applicationName" value="${sessionScope.UserBean.activeApplication.applicationName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="domainName">Domain Name:</label>
		    <input type="text" id="domainName" name="domainName" value="${sessionScope.UserBean.activeApplication.domainName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="owner">Owner:</label>
		    <input type="text" id="owner" name="owner" value="${sessionScope.UserBean.activeApplication.owner}" size="50" maxlength="1024"><br>
		    </p>
		    <p>
			<label for="applicationDescription">Application Description:</label>
		    <textarea id="applicationDescription" name="applicationDescription" rows="5" cols="60" maxlength="128" placeholder="Description of the application">${sessionScope.UserBean.activeApplication.applicationDescription}</textarea><br>
		    </p>
		    <p>
		    <button type="submit" value="addApplication" name="ApplicationAction">Add Application</button>
		    </p>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.allApplications) > 0}">
				<table class="center">
					<tr>
						<th style="width:10%">
						</th>
						<th style="width:30%">
							Organization
						</th>
						<th style="width:20%">
							Application Code
						</th>
						<th style="width:40%">
							Application Name
						</th>
					</tr>
					<c:forEach var="application" items="${sessionScope.UserBean.allApplications}">
						<tr>
							<td style="width:10%">
								<button type="submit" value="delete_${application.applicationId}" name="ApplicationAction">del</button>
								<button type="submit" value="edit_${application.applicationId}" name="ApplicationAction">Edit</button>
							</td>
							<td style="width:30%">
								${myfn:getOrganization( application.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
							</td>
							<td style="width:20%">
								${application.applicationCode}
							</td>
							<td style="width:40%">
								${application.applicationName}
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>		
		</form>
		
    </div>
	
</body>
</html>