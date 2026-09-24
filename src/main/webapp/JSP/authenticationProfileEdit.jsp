<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://securityapp.com" prefix="myfn" %>

<html>
<head>

<style type="text/css" media="all">
@import "/securityapp/css/pageFormat.css";
@import "/securityapp/css/header.css";
</style>

<meta charset="UTF-8">
<title>Edit an Authentication Profile</title>
</head>
<body>
	<%@ include file="header.jsp" %>
	<div id="content">
		<div id="errors">
	       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
	   			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
	       			<c:out value="${error}"/><br/><br/>
	   			</c:forEach>
	   			${sessionScope.UserBean.clearErrors}
	       	</c:if>
	    </div>   	
	
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<h2 align="center">Authentication Profile</h2>
		<div id="inputField">

			<table class="center">
				<tr>
					<th>
					    Organization:
					</th>
					<td>
						${myfn:getOrganization( sessionScope.UserBean.activeAuthenticationProfile.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</td>
				</tr>
				<tr>
					<th>
						<label for="userId">User ID:</label>
					</th>
					<td>
					    <input type="text" id="userId" name="userId" value="${sessionScope.UserBean.activeAuthenticationProfile.userId}" size="30" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="textPassword">Password:</label>
					</th>
					<td>
		    			<input type="text" id="textPassword" name="textPassword" value="${sessionScope.UserBean.activeAuthenticationProfile.textPassword}" size="30" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="firstName">First Name:</label>
					</th>
					<td>
		    			<input type="text" id="firstName" name="firstName" value="${sessionScope.UserBean.activeAuthenticationProfile.firstName}" size="50" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="lastName">Last Name:</label>
					</th>
					<td>
		    			<input type="text" id="lastName" name="lastName" value="${sessionScope.UserBean.activeAuthenticationProfile.lastName}" size="50" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="mobilePhone">Mobile Phone:</label>
					</th>
					<td>
		    			<input type="text" id="mobilePhone" name="mobilePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.mobilePhone}" size="12" maxlength="12"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="officePhone">Office Phone:</label>
					</th>
					<td>
		    			<input type="text" id="officePhone" name="officePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.officePhone}" size="12" maxlength="12"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="officePhoneExt">Office Phone Extension:</label>
					</th>
					<td>
		    			<input type="text" id="officePhoneExt" name="officePhoneExt" value="${sessionScope.UserBean.activeAuthenticationProfile.officePhoneExt}" size="10" maxlength="10"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="homePhone">Home Phone:</label>
					</th>
					<td>
		    			<input type="text" id="homePhone" name="homePhone" value="${sessionScope.UserBean.activeAuthenticationProfile.homePhone}" size="12" maxlength="12"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="failedLoginAttempts">Failed Login Attempts</label>
					</th>
					<td>
		    			<input type="text" id="failedLoginAttempts" name="failedLoginAttempts" value="${sessionScope.UserBean.activeAuthenticationProfile.failedLoginAttempts}" size="1" maxlength="1"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="verificationCodeMethod">Verification Code Method:</label>
					</th>
					<td>
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
					</td>
				</tr>
			</table>
		    <p>
		    <c:if test="${sessionScope.UserBean.function == 'deleteAuthenticationProfile'}">
		    	<button type="submit" value="confirmDelete" name="AuthenticationProfileAction">Confirm Delete</button>
		    </c:if>	
		    <c:if test="${sessionScope.UserBean.function == 'editAuthenticationProfile'}">
		    	<button type="submit" value="saveEdit" name="AuthenticationProfileAction">Save</button>
		    </c:if>	
		    <button type="submit" value="cancelEditDelete" name="AuthenticationProfileAction">Cancel</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>