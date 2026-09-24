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
<title>Edit an Application</title>
</head>
<body>
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
		<h2 align="center">Application</h2>
		<div id="inputField">

			<table class="center">
				<tr>
					<th>
					    Organization:</label>
					</th>
					<td>
						${myfn:getOrganization( sessionScope.UserBean.activeApplication.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</td>
				</tr>
				<tr>
					<th>
						<label for="applicationCode">Application Code:</label>
					</th>
					<td>
					    <input type="text" id="applicationCode" name="applicationCode" value="${sessionScope.UserBean.activeApplication.applicationCode}" size="50" maxlength="50"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="applicationName">Application Name:</label>
					</th>
					<td>
					    <input type="text" id="applicationName" name="applicationName" value="${sessionScope.UserBean.activeApplication.applicationName}" size="50" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="domainName">Domain Name:</label>
					</th>
					<td>
					    <input type="text" id="domainName" name="domainName" value="${sessionScope.UserBean.activeApplication.domainName}" size="50" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="owner">Owner:</label>
					</th>
					<td>
					    <input type="text" id="owner" name="owner" value="${sessionScope.UserBean.activeApplication.owner}" size="50" maxlength="1024"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="applicationDescription">Application Description:</label>
					</th>
					<td>
					    <textarea id="applicationDescription" name="applicationDescription" rows="5" cols="60" maxlength="1028" placeholder="Description of the application">${sessionScope.UserBean.activeApplication.applicationDescription}</textarea><br>
					</td>
				</tr>
			</table>
		    <p>
		    <c:if test="${sessionScope.UserBean.function == 'deleteApplication'}">
		    	<button type="submit" value="confirmDelete" name="ApplicationAction">Confirm Delete</button>
		    </c:if>	
		    <c:if test="${sessionScope.UserBean.function == 'editApplication'}">
		    	<button type="submit" value="saveEdit" name="ApplicationAction">Save</button>
		    </c:if>	
		    <button type="submit" value="cancelEditDelete" name="ApplicationAction">Cancel</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>