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
<title>Edit a Department</title>
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
		<h2 align="center">Department</h2>
		<div id="inputField">

			<table class="center">
				<tr>
					<th>
					    Organization:
					</th>
					<td>
						${myfn:getOrganization( sessionScope.UserBean.activeDepartment.organizationId, sessionScope.UserBean.allOrganizations ).organizationName}
					</td>
				</tr>
				<tr>
					<th>
						<label for="departmentName">Department Name:</label>
					</th>
					<td>
					    <input type="text" id="departmentName" name="departmentName" value="${sessionScope.UserBean.activeDepartment.departmentName}" size="30" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="departmentCode">Department Code:</label>
					</th>
					<td>
					    <input type="text" id="departmentCode" name="departmentCode" value="${sessionScope.UserBean.activeDepartment.departmentCode}" size="20" maxlength="20"><br>
					</td>
				</tr>
				<tr>
					<th>
						<label for="departmentDescription">Department Description:</label>
					</th>
					<td>
		    			<textarea id="departmentDescription" name="departmentDescription" rows="5" cols="60" maxlength="5000" placeholder="Description of the department">${sessionScope.UserBean.activeDepartment.departmentDescription}</textarea><br>
					</td>
				</tr>
			</table>
		    <p>
		    <c:if test="${sessionScope.UserBean.function == 'deleteDepartment'}">
		    	<button type="submit" value="confirmDelete" name="DepartmentAction">Confirm Delete</button>
		    </c:if>	
		    <c:if test="${sessionScope.UserBean.function == 'editDepartment'}">
		    	<button type="submit" value="saveEdit" name="DepartmentAction">Save</button>
		    </c:if>	
		    <button type="submit" value="cancelEditDelete" name="DepartmentAction">Cancel</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>