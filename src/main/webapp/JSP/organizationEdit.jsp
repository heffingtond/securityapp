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
		    			<label for="organizationName">Organization Name</label>
					</th>
					<td>
		    			<input type="text" id="organizationName" name="organizationName" value="${sessionScope.UserBean.activeOrganization.organizationName}" size="80" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationAddress">Organization Address</label>
					</th>
					<td>
		    			<input type="text" id="organizationAddress" name="organizationAddress" value="${sessionScope.UserBean.activeOrganization.organizationAddress}" size="80" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationCity">Organization City</label>
					</th>
					<td>
		    			<input type="text" id="organizationCity" name="organizationCity" value="${sessionScope.UserBean.activeOrganization.organizationCity}" size="80" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationState">Choose a state</label>
					</th>
					<td>
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
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationZip">Organization Zip</label>
					</th>
					<td>
					    <input type="text" id="organizationZip" name="organizationZip" value="${sessionScope.UserBean.activeOrganization.organizationZip}" size="5" maxlength="5">
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationZipExt">Organization Zip Extension</label>
					</th>
					<td>
		    			<input type="text" id="organizationZipExt" name="organizationZipExt" value="${sessionScope.UserBean.activeOrganization.organizationZipExt}" size="4" maxlength="4"><br>
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="primaryUrl">Primary URL</label>
					</th>
					<td>
					    <input type="text" id="primaryUrl" name="primaryUrl" value="${sessionScope.UserBean.activeOrganization.primaryUrl}" size="80" maxlength="128"><br>
					</td>
				</tr>
				<tr>
					<th>
		    			<label for="organizationDescription">Organization Description</label>
					</th>
					<td>
					    <textarea id="organizationDescription" name="organizationDescription" rows="5" cols="60" maxlength="1024" placeholder="Description of the organization">${sessionScope.UserBean.activeOrganization.organizationDescription}</textarea><br>
					</td>
				</tr>
			</table>
		    <p>
		    <c:if test="${sessionScope.UserBean.function == 'deleteOrganization'}">
		    	<button type="submit" value="confirmDelete" name="OrganizationAction">Confirm Delete</button>
		    </c:if>	
		    <c:if test="${sessionScope.UserBean.function == 'editOrganization'}">
		    	<button type="submit" value="saveEdit" name="OrganizationAction">Save</button>
		    </c:if>	
		    <button type="submit" value="cancelEditDelete" name="OrganizationAction">Cancel</button>
		    </p>
	    </div>
		</form>
    </div>
	
</body>
</html>