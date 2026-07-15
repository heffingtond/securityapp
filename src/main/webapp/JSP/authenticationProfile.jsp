<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>

<style type="text/css" media="all">
@import "/securityapp/css/navbar.css";
@import "/securityapp/css/pageFormat.css";
</style>

<meta charset="UTF-8">
<title>Maintain Authentication Profiles</title>
</head>
<body>
	<div id="content">
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<ul>
		  <li><a href="?OrganizationAction=yes">Organization</a></li>
		  <li><a href="#here">Authentication Profile</a></li>
		  <li><a href="?LogoutAction=yes">Logout</a></li>
		</ul>
       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
   			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
       			<c:out value="${error}"/><br/><br/>
   			</c:forEach>
   			${sessionScope.UserBean.clearErrors}
       	</c:if>
		<h2 align="center">Authentication Profile</h2>
		<div id="inputField">
			<p>
		    <label for="organizationId">Select the organization:</label>
			<select name="organizationId" id="organizationId">
				<option value="SELECT">SELECT</option>
				<c:forEach var="organization" items="${sessionScope.UserBean.allOrganizations}">
					<c:if test="${sessionScope.UserBean.activeAuthenticationProfile.organizationId == organization.organizationId}">
						<option selected value="${organization.organizationId}">${organization.organizationName}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeAuthenticationProfile.organizationId != organization.organizationId}">
						<option value="${organization.organizationId}">${organization.organizationName}</option>
					</c:if>
				</c:forEach>
			</select>
		    </p>
		    <p>
			<label for="userId">User ID:</label>
		    <input type="text" id="userId" name="userId" value="${sessionScope.UserBean.activeAuthenticationProfile.userId}" size="30" maxlength="128"><br>
		    </p>
		    <p>
			<label for="password">Password:</label>
		    <input type="text" id="password" name="password" value="${sessionScope.UserBean.activeAuthenticationProfile.password}" size="30" maxlength="128"><br>
		    </p>
		    <p>
			<label for="firstName">First Name:</label>
		    <input type="text" id="firstName" name="firstName" value="${sessionScope.UserBean.activeAuthenticationProfile.firstName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="lastName">Last Name:</label>
		    <input type="text" id="lastName" name="lastName" value="${sessionScope.UserBean.activeAuthenticationProfile.lastName}" size="50" maxlength="128"><br>
		    </p>
		    <p>
			<label for="mobilePhone">Mobile Phone:</label>
		    <input type="text" id="mobilePhone" name="mobilePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.mobilePhone}" size="12" maxlength="12"><br>
		    </p>
		    <p>
			<label for="officePhone">Office Phone:</label>
		    <input type="text" id="officePhone" name="officePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.officePhone}" size="12" maxlength="12"><br>
		    </p>
		    <p>
			<label for="officePhoneExt">Office Phone Extension:</label>
		    <input type="text" id="officePhoneExt" name="officePhoneExt" value="${sessionScope.UserBean.activeAuthenticationProfile.officePhoneExt}" size="10" maxlength="10"><br>
		    </p>
		    <p>
			<label for="homePhone">Home Phone:</label>
		    <input type="text" id="homePhone" name="homePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.homePhone}" size="12" maxlength="12"><br>
		    </p>
		    <p>
			<label for="failedLoginAttempts">Failed Login Attempts</label>
		    <input type="text" id="failedLoginAttempts" name="failedLoginAttempts" value="${sessionScope.UserBean.activeAuthenticationProfile.failedLoginAttempts}" size="1" maxlength="1"><br>
		    </p>
		    <p>
			<label for="verificationCodeMethod">Verification Code Method:</label>
			<select name="verificationCodeMethod" id="verificationCodeMethod">
				<option value="SELECT">SELECT</option>
				<c:forEach var="method" items="${sessionScope.VERIFICATION_METHODS}">
					<c:if test="${sessionScope.UserBean.activeAuthenticationProfile.verificationCodeMethod == method}">
						<option selected value="${method}">${method}</option>
					</c:if>
					<c:if test="${sessionScope.UserBean.activeAuthenticationProfile.verificationCodeMethod != method}">
						<option value="${method}">${method}</option>
					</c:if>
				</c:forEach>
			</select>
		    </p>
		    <p>
		    <button type="submit" value="addAuthenticationProfile" name="AuthenticationProfileAction">Add Authentication Profile</button>
		    </p>
	    </div>
		</form>
		<form name="form2" action="/securityapp/SecurityServlet" method="POST">
			<c:if test="${fn:length(sessionScope.UserBean.allAuthenticationProfiles) > 0}">
				<table class="center">
					<tr>
						<th style="width:10%">
						</th>
						<th style="width:80%">
							User ID
						</th>
					</tr>
					<c:forEach var="authenticationProfile" items="${sessionScope.UserBean.allAuthenticationProfiles}">
						<tr>
							<td style="width:10%">
								<button type="submit" value="delete_${authenticationProfile.authenticationProfileId}" name="AuthenticationProfileAction">del</button>
								<button type="submit" value="edit_${authenticationProfile.authenticationProfileId}" name="AuthenticationProfileAction">Edit</button>
							</td>
							<td style="width:80%">
								${authenticationProfile.userId}
							</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>	
		</form>
		
    </div>
	
</body>
</html>