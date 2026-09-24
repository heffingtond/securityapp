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
<title>Edit a Role</title>
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
		<h2 align="center">Role</h2>
		<div id="inputField">

			<table class="center">
				<tr>
					<th>
					    Organization:
					</th>
					<td>
						${myfn:getOrganization( sessionScope.UserBean.activeRole.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</td>
				</tr>
				<tr>
					<th>
					    Application:
					</th>
					<td>
						${myfn:getApplication( sessionScope.UserBean.activeRole.applicationId, sessionScope.UserBean.allApplications ).applicationCode}-${myfn:getApplication( sessionScope.UserBean.activeRole.applicationId, sessionScope.UserBean.allApplications ).applicationName}
					</td>
				</tr>
				<tr>
					<th>
						<label for="roleName">Role Name:</label>
					</th>
					<td>
					    <input type="text" id="roleName" name="roleName" value="${sessionScope.UserBean.activeRole.roleName}" size="50" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="roleDescription">Role Description:</label>
					</th>
					<td>
					    <textarea id="roleDescription" name="roleDescription" rows="5" cols="60" maxlength="5000" placeholder="Description for this role">${sessionScope.UserBean.activeRole.roleDescription}</textarea><br>
					</td>
				</tr>
			</table>
		    <p>
		    <c:if test="${sessionScope.UserBean.function == 'deleteRole'}">
		    	<button type="submit" value="confirmDelete" name="RoleAction">Confirm Delete</button>
		    </c:if>	
		    <c:if test="${sessionScope.UserBean.function == 'editRole'}">
		    	<button type="submit" value="saveEdit" name="RoleAction">Save</button>
		    </c:if>	
		    <button type="submit" value="cancelEditDelete" name="RoleAction">Cancel</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>