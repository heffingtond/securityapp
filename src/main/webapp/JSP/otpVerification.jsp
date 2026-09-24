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
@import "/securityapp/css/header.css";
</style>

<title>One Time Pass Code Verification</title>
</head>
<body>

	<%@ include file="header.jsp" %>

	<div id="content">
		<div id="inputField">
		    <div style="text-align: center;">
		        <h1>User Login</h1>
				<div id="errors">
			       	<c:if test="${fn:length(sessionScope.UserBean.errors) > 0}">
		     			<c:forEach var="error" items="${sessionScope.UserBean.errors}">
		         			<c:out value="${error}"/><br/><br/>
		     			</c:forEach>
	   					${sessionScope.UserBean.clearErrors}
			       	</c:if>
			    </div>   	
		        <form action="/securityapp/SecurityServlet" method="post">
		            <label for="passcode">Enter You Pass Code Here:</label>
		            <input type="text" id="passcode" name="passcode" size="8" maxlength="6"><br><br>
		            <input type="submit" value="Validate" name="LoginAction">
		            <p>
		            If no code received within 5 minutes, request a new code here. 
		            <input type="submit" value="Request New Code" name="LoginAction">
		            </p>
		            <p>
		            ${requestScope.userMessage}
		            </p>
		        </form>
		    </div>
	    </div>
	</div>    	    
</body>
</html>