<%-- 
    Document   : advancedSmartphoneOveruse
    Created on : Oct 23, 2015, 7:56:17 PM
    Author     : r128
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Advanced Smartphone Overuse</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>

        <%            String macAddress = _currentUser.getMacAddress();
        %>
        <%
            String errorMsg = (String) request.getAttribute("errorMsg");
            String overuseIndex = (String) request.getAttribute("overuseIndex");
            String classTime = (String) request.getAttribute("classTime");
            String smallGroupTime = (String) request.getAttribute("smallGroupTime");
            String unproductiveAppUsage = (String) request.getAttribute("unproductiveAppUsage");
        %>
        <%
            if (errorMsg != null) {
        %>
        <div class="alert alert-danger" role="alert"><%=errorMsg%></div>
        <%}%>
        <h1>Advanced Smartphone Overuse Report</h1>
        <%
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            if(startDate==null){
                startDate = "YYYY-MM-DD";
            }
            if(endDate==null){
                endDate = "YYYY-MM-DD";
            }
        %>
        <form action="" method="POST">
            <table>
                <input type="hidden" name="macAddress" value="<%=macAddress%>"/>
                <tr>
                    <td>Start Date: </td>
                    <td><input type="date" name="startDate" value="<%=startDate%>"/></td>
                </tr>
                <tr>
                    <td>End Date: </td>
                    <td><input type="date" name="endDate" value="<%=endDate%>"/></td>
                </tr>
            </table>
            <input type="submit" value="Go!"/>
        </form>
        <br/>

        <%if (overuseIndex != null) {%>

        <table border="1">
            <th>Name</th><th>Value</th>
            <tr><td>Overuse Index</td><td><%=overuseIndex%></td></tr>
            <tr><td>In-Class Duration</td><td><%=classTime%></td></tr>
            <tr><td>Small Group Duration</td><td><%=smallGroupTime%></td></tr>
            <tr><td>Non-Productive App Usage Duration</td><td><%=unproductiveAppUsage%></td></tr>
        </table>
        <%}%>
        <%@include file="footer.jsp" %>
    </body>

</html>
