<%-- 
    Authors: Amos & Shing Hei
    File: navbar.jsp
    Desc:
        The protect file will validate session, as well as include the respective imports
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<link href="../css/main.css" rel="stylesheet" type="text/css"/>
<link href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
<%@page import="smua.models.Entities.Demographic"%>
<%
    boolean _isAdmin = false;
    Demographic _currentUser = (Demographic) session.getAttribute("currUser");
    //Checks for existing session, return to login if session not found
    if (_currentUser == null) {
        response.sendRedirect("login");
        return;
    }
    //check if instance of _currentUser is an Admin or a User
    if (_currentUser.getEmail().equals("admin")) {
        _isAdmin = true;
    }

    //user protect
%>

<%    
    String _user = _currentUser.getName();
    if (_isAdmin) {
        _user = "Admin";
    }
%>
<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="navbar-header navbar-right">
            <ul class="nav navbar-nav">
                <%
                    if(_isAdmin) {
                %>
                <li role="presentation">

                    <a class="dropdown-toggle" data-toggle="dropdown"role="button" aria-haspopup="true"
                       aria-expanded="false">Admin<span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="/admin">Bootstrap/Add Data</a></li>
                        <li><a href="/admin/deleteLocation">Delete Location Data</a></li>
                        
                    </ul>
                </li>
                <%
                    }
                %>
                
                
                <li role="presentation">
                    <ul class="nav navbar-nav">
                        <li><a href="/smartphoneHeatmap">Smartphone Usage Heatmap</a></li>
                    </ul>
                </li>
                <li role="presentation">

                    <a class="dropdown-toggle" data-toggle="dropdown"role="button" aria-haspopup="true"
                       aria-expanded="false">Top-K Report<span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="/topKApp">Top K App</a></li>
                        <li><a href="/topKStudent">Top K Student</a></li>
                        <li><a href="/topKSchool">Top K School</a></li>
                    </ul>
                </li>
                <li role="presentation">
                    <a class="dropdown-toggle" data-toggle="dropdown"role="button" aria-haspopup="true"
                       aria-expanded="false">Basic App Usage Report<span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="/basicAppUsageTime">Basic App Usage Time</a></li>
                        <li><a href="/basicAppUsageTimeBreakdown">Basic App Usage Time Breakdown</a></li>
                        <li><a href="/basicAppUsageDiurnal">Diurnal Pattern Of App Usage Time</a></li>
                        <li><a href="/basicAppUsageAppCat">Breakdown by App Category</a></li>
                    </ul>
                </li>
                <%if(!_isAdmin){%>
                <li role="presentation">
                    <a class="dropdown-toggle" data-toggle="dropdown"role="button" aria-haspopup="true"
                       aria-expanded="false">Smartphone Overuse Report<span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="/smartphoneOveruse">Basic</a></li>
                        <li><a href="/advancedSmartphoneOveruse">Advanced</a></li>
                    </ul>
                </li>

                <li role="presentation">
                    <ul class="nav navbar-nav">
                        <li><a href="/socialActivenessReport">Social Activeness Report</a></li>
                    </ul>
                </li>
                <%}%>
                
                

                <li role="presentation">

                    <p class ="navbar-text"><b> Welcome, <%=_user%></b></p>
                    <ul class="nav navbar-nav">
                        <form action="/logout" method="get" class="navbar-form">
                            <input type="submit" value="Logout" class="btn btn-default">
                        </form>
                    </ul>
                </li>
                
               
            </ul>
        </div>

    </div>
</nav>
