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
<title>Maintain Organizations</title>
</head>
<body>
	<%@ include file="header.jsp" %>
	<div id="content">
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<ul>
	  		<li><a href="#here">Organization</a></li>
	  		<li><a href="?AuthenticationProfileAction=yes">Authentication Profile</a></li>
	  		<li><a href="?DepartmentAction=yes">Department</a></li>
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
		<h2 align="center">Organization</h2>
		<div id="inputField">
		<c:if test="${sessionScope.UserBean.restrictedOrganizationId == 0}">
			<p>
			<label for="organizationName">Organization Name:</label>
		    <input type="text" id="organizationName" name="organizationName" value="${sessionScope.UserBean.activeOrganization.organizationName}" size="80" maxlength="128"><br>
		    </p>
			<p>
			<label for="organizationAddress">Organization Address:</label>
		    <input type="text" id="organizationAddress" name="organizationAddress" value="${sessionScope.UserBean.activeOrganization.organizationAddress}" size="80" maxlength="128"><br>
		    </p>
			<p>
			<label for="organizationCity">Organization City:</label>
		    <input type="text" id="organizationCity" name="organizationCity" value="${sessionScope.UserBean.activeOrganization.organizationCity}" size="80" maxlength="128"><br>
		    </p>
			<p>
		    <label for="organizationState">Choose a state:</label>
			<select name="organizationState" id="organizationState">
				<c:forEach var="state" items="${sessionScope.STATE_LIST}">
					<c:if test="${sessionScope.UserBean.activeOrganization.organizationState == state.value}">
						<option selected value="${state.value}">${state.key}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeOrganization.organizationState != state.value}">
						<option value="${state.value}">${state.key}</option>
					</c:if>
				</c:forEach>
			</select>
		    <label for="organizationZip">Zip:</label>
		    <input type="text" id="organizationZip" name="organizationZip" value="${sessionScope.UserBean.activeOrganization.organizationZip}" size="5" maxlength="5">
		    <label for="organizationZip">Zip Ext:</label>
		    <input type="text" id="organizationZipExt" name="organizationZipExt" value="${sessionScope.UserBean.activeOrganization.organizationZipExt}" size="4" maxlength="4"><br>
		    </p>
			<p>
			<label for="primaryUrl">Primary URL:</label>
		    <input type="text" id="primaryUrl" name="primaryUrl" value="${sessionScope.UserBean.activeOrganization.primaryUrl}" size="80" maxlength="128"><br>
		    </p>
			<p>
			<label for="organizationDescription">Organization Description:</label>
		    <textarea id="organizationDescription" name="organizationDescription" rows="5" cols="60" maxlength="1024" placeholder="Description of the organization">${sessionScope.UserBean.activeOrganization.organizationDescription}</textarea><br>
		    </p>
		    <p>
		    <button type="submit" value="addOrganization" name="OrganizationAction">Add Organization</button>
		    </p>
		</c:if>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.allOrganizations) > 0}">
				<table class="center">
					<tr>
						<th style="width:10%">
						</th>
						<th style="width:80%">
							Organization Name
						</th>
					</tr>
					<c:if test="${sessionScope.UserBean.restrictedOrganizationId == 0}">
						<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
							<tr>
								<td style="width:10%">
									<button type="submit" value="delete_${organization.organizationId}" name="OrganizationAction">del</button>
									<button type="submit" value="edit_${organization.organizationId}" name="OrganizationAction">Edit</button>
								</td>
								<td style="width:80%">
									${organization.organizationName}
								</td>
							</tr>
						</c:forEach>
					</c:if>	
					<c:if test="${sessionScope.UserBean.restrictedOrganizationId > 0}">
						<tr>
							<td style="width:10%">
								<button type="submit" value="delete_${sessionScope.UserBean.loginAuthenticationProfile.organizationId}" name="OrganizationAction">del</button>
								<button type="submit" value="edit_${sessionScope.UserBean.loginAuthenticationProfile.organizationId}" name="OrganizationAction">Edit</button>
							</td>
							<td style="width:80%">
								${myfn:getOrganization( sessionScope.UserBean.loginAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
							</td>
						</tr>
					</c:if>
				</table>
			</c:if>	
		</form>
		
    </div>
	
</body>
</html>