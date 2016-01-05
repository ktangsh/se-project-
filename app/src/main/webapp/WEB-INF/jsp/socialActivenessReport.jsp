<%-- 
    Document   : SocialActivenessReport
    Created on : Oct 15, 2015, 3:56:36 PM
    Author     : famous
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <%
            String macAddress = _currentUser.getMacAddress();
            String date = request.getParameter("date");
            if (date == null) {
                date = "YYYY-MM-DD";
            }
        %>
        <form action="/socialActivenessReport" method="POST">
            Date: <input type="date" name="date" value="<%=date%>"/>
            <input type ="hidden" value="<%=macAddress%>" name="macAddress"/>
            <input type="submit" name="submit"/>

        </form>
        <br/>

        <%
            LinkedHashMap<String, Integer> socialActivenessReport = (LinkedHashMap<String, Integer>) request.getAttribute("socialActivenessReport");
            TreeMap<String, Integer> socialAppUsageTime = (TreeMap<String, Integer>) request.getAttribute("socialAppUsageTime");
            System.out.println("ARE THEY NULL ? : " + (socialActivenessReport == null) + " " + (socialAppUsageTime == null));
            String errorMsg = (String) request.getAttribute("errorMsg");
            if (socialActivenessReport != null && socialAppUsageTime != null) {
                int totalAppUsage = 0;
                for (int i : socialAppUsageTime.values()) {
                    totalAppUsage += i;
                }

        %>

        <label>Total Social App Usage Time :<%=totalAppUsage%> seconds</label>
        <%
            if (!socialAppUsageTime.isEmpty()) {
        %>
        <table border="1">

            <tr>
                <th>App Name</th>
                <th>Percent</th>
            </tr>
            <%
                Iterator<String> socialAppIter = socialAppUsageTime.keySet().iterator();
                while (socialAppIter.hasNext()) {
                    String catName = socialAppIter.next();
                    int duration = socialAppUsageTime.get(catName);
                    int percentage = Math.round(1.0f * duration / totalAppUsage * 100);

            %>
            <tr><td><%=catName%></td><td><%=percentage%>%</td></tr>
            <%  }
            }%>
        </table>
        </br>
        <%
            int totalTimeInSIS = socialActivenessReport.get("total-time-spent-in-sis");
            int groupPercent = socialActivenessReport.get("group-percent");
            int soloPercent = socialActivenessReport.get("solo-percent");
        %>
        <label>Total Time Spent In SIS : <%=totalTimeInSIS%> seconds</label>
        <table border="1">
            <tr>
                <th>Category</th>
                <th>Percent</th>
            </tr>
            <tr><td>Group Percent</td><td><%=groupPercent%>%</td></tr>
            <tr><td>Solo Percent</td><td><%=soloPercent%>%</td></tr>
        </table>
        <%} else if (errorMsg != null) {%>
        <%=errorMsg%>
        <%}%>
        <%@include file="footer.jsp" %>
    </body>
</html>
