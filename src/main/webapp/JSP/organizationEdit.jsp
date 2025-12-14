<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>

<style type="text/css" media="all">
@import "/securityapp/css/pageFormat.css";
</style>

<meta charset="UTF-8">
<title>Edit an organization</title>
</head>
<body>
	<div id="content">
       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
   			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
       			<c:out value="${error}"/><br/><br/>
   			</c:forEach>
   			${sessionScope.UserBean.clearErrors}
       	</c:if>
	
		<form name="form1" action="/securityapp/SecurityServlet" method="POST">
		<h2 align="center">Organization</h2>
		<div id="inputField">

			<table class="center">
				<tr>
					<th>
						Organization Name
					</th>
					<td>
	    				<input type="text" value="Hoser" id="organizationName" name="organizationName" size="80" maxlength="128"><br>
					</td>
				</tr>
			</table>

			<p>
			<label for="organizationName">Organization Name:</label>
		    <input type="text" id="organizationName" name="organizationName" size="80" maxlength="128" required><br>
		    </p>
			<p>
			<label for="organizationAddress">Organization Address:</label>
		    <input type="text" id="organizationAddress" name="organizationAddress" size="80" maxlength="128" required><br>
		    </p>
			<p>
			<label for="organizationCity">Organization City:</label>
		    <input type="text" id="organizationCity" name="organizationCity" size="80" maxlength="128" required><br>
		    </p>
			<p>
		    <label for="organizationState">Choose a state:</label>
			<select name="organizationState" id="organizationState">
				<c:forEach var="state" items="${sessionScope.STATE_LIST}">
					<option value="${state.value}">${state.key}</option>
				</c:forEach>
			</select>
		    <label for="organizationZip">Zip:</label>
		    <input type="text" id="organizationZip" name="organizationZip" size="5" maxlength="5" required>
		    <label for="organizationZip">Zip Ext:</label>
		    <input type="text" id="organizationZipExt" name="organizationZipExt" size="4" maxlength="4"><br>
		    </p>
			<p>
			<label for="primaryUrl">Primary URL:</label>
		    <input type="text" id="primaryUrl" name="primaryUrl" size="80" maxlength="128" required><br>
		    </p>
			<p>
			<label for="organizationDescription">Organization Description:</label>
		    <textarea id="organizationDescription" name="organizationDescription" rows="5" cols="60" maxlength="1024" placeholder="Description of the organization"></textarea><br>
		    </p>
		    <p>
		    <button type="submit" value="addOrganization" name="OrganizationAction">Add Organization</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>