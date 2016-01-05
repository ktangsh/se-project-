<%-- 
    Document   : smartphoneOveruse
    Created on : Oct 6, 2015, 4:15:28 PM
    Author     : shtang.2014
--%>

<%@page import="java.util.HashMap"%>
<%@page import="smua.models.JSON.SmartphoneOveruse"%>
<%@page import="smua.models.Entities.App"%>
<%@page import="java.util.List"%>
<%@page import="smua.models.Entities.Demographic"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Smartphone Overuse Report</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <h1>Smartphone Overuse Report</h1>
        <form action="/smartphoneOveruse" method='post'>
            <%
                _currentUser = (Demographic) session.getAttribute("currUser");
                // store current user's email address for personalised smartphone overuse report 
                String tempEmail = _currentUser.getEmail();

                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                if (startDate == null) {
                    startDate = "YYYY-MM-DD";
                }
                if (endDate == null) {
                    endDate = "YYYY-MM-DD";
                }
            %>
            <input type="hidden" name="email" value="<%=tempEmail%>">
            <table>
                <tr><td>Start Date:</td><td><input type='date' name="startDate" value="<%=startDate%>"/></td></tr>
                <tr><td>End Date:</td><td><input type='date' name="endDate" value="<%=endDate%>"/></td></tr>
            </table>
            <input type='submit' value='submit'><br/>
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
                    out.println("<font color='red'>" + errorMessage + "</font><br/>");
                }
                SmartphoneOveruse smartphoneOveruse = (SmartphoneOveruse) request.getAttribute("smartphoneOveruse");
                if (smartphoneOveruse != null && errorMessage == null) {
                    String[] index = {"Light", "Moderate", "Severe"};

                    HashMap<String, Double> metrics = smartphoneOveruse.getMetrics();

                    double gamingCategory = metrics.get("gamingCategory");
                    int gamingCategoryInt = (int) gamingCategory;
                    double gamingDuration = metrics.get("gamingDuration");
                    int gamingDurationInt = (int) gamingDuration;

                    double usageCategory = metrics.get("usageCategory");
                    int usageCategoryInt = (int) usageCategory;
                    double usageDuration = metrics.get("usageDuration");
                    int usageDurationInt = (int) usageDuration;

                    double accessFrequencyCategory = metrics.get("accessFrequencyCategory");
                    int accessCategoryInt = (int) accessFrequencyCategory;
                    double accessFrequency = metrics.get("accessFrequency");

            %>
            <br/>
            <p><b>Overuse Index: <%=smartphoneOveruse.getOveruseIndex()%></b></p></br>
            <table border="1">
                <tr>
                    <th></th>
                    <th>Value</th>
                    <th>Category</th>
                </tr>

                <tr>
                    <td>Average daily smartphone usage duration</td>
                    <td><%=usageDurationInt%></td>
                    <td><%=index[usageCategoryInt]%></td>
                </tr>
                <tr>
                    <td>Average daily gaming duration</td>
                    <td><%=gamingDurationInt%></td>
                    <td><%=index[gamingCategoryInt]%></td>
                </tr>
                <tr>
                    <td>Smartphone Access Frequency</td>
                    <td><%=accessFrequency%></td>
                    <td><%=index[accessCategoryInt]%></td>
                </tr>
                <%
                    }
                %>
            </table>
        </form>
        <%@include file="footer.jsp" %>
    </body>
</html>
