<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
<meta charset="UTF-8">

<style type="text/css" media="all">
@import "/securityapp/css/navbar.css";
@import "/securityapp/css/pageFormat.css";
</style>

<title>Security Login</title>
</head>
<body>
	<div id="content">
		<div id="inputField">
		    <div style="text-align: center;">
		        <h1>User Login</h1>
		       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
	     			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
	         			<c:out value="${error}"/><br/><br/>
	     			</c:forEach>
   					${sessionScope.UserBean.clearErrors}
		       	</c:if>
		        <form action="/securityapp/SecurityServlet" method="post">
		            <label for="username">Username:</label>
		            <input type="text" id="username" name="username" required><br><br>
		            <label for="password">Password:</label>
		            <input type="password" id="password" name="password" required><br><br>
		            <input type="submit" value="Login" name="LoginAction">
		        </form>
		    </div>
	    </div>
	</div>    	    
</body>
</html>