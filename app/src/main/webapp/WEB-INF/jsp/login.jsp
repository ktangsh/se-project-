<%-- 
    Document   : login
    Created on : Sep 24, 2015, 9:03:09 PM
    Author     : famous
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="../css/main.css" rel="stylesheet" type="text/css"/>
        <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
              rel="stylesheet">
        
        <title>Welcome to SMUA</title>
    </head>
    <body>

        <div class="container top-buffer10">
            <div class="row">
                <div class="col-md-4"></div>
                <div class="col-md-4"><h1 class="text-center">Welcome to SMUA!</h1></div>
                <div class="col-md-4"></div>
            </div>
            <div class="row top-buffer3">
                <div class="col-md-4"></div>
                <div class="col-md-4">
                    <form action="/login" method="post" class="form-signin">
                        <h5 class="form-signin-heading">Please sign in</h5>
                        <label for="inputEmail" class="sr-only">Email address</label>
                        <input type="text" name ="email" class="form-control" placeholder="Email address" required autofocus>
                        <label for="inputPassword" class="sr-only">Password</label>
                        <input type="password" name="password" id="inputPassword" class="form-control" placeholder="Password" required>
                        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
                    </form>
                    <c:if test="${not empty msg}">
                        <div class="alert alert-danger top-buffer3" role="alert">${msg}</div>
                    </c:if>
                </div>
                <div class="col-md-4"></div>
            </div>
        </div> <!-- /container -->



        <%@include file="footer.jsp" %>
    </body>
</html>
