<%-- 
    Document   : basicAppUsageTime
    Created on : Oct 3, 2015, 1:35:25 PM
    Author     : ChuQian
--%>

<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
         <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <script src="js/Chart.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">
        <title>Basic App Usage Time</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        
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
        <form action="/basicAppUsageTime" method="post">

            <h1>Basic App Report</h1>
            <table>
                <tr><td>Start Date: </td><td><input type="date" name="startDate" value="<%=startDate%>"></td></tr>
                <tr><td>End Date: </td><td><input type="date" name="endDate" value="<%=endDate%>"></td></tr>
            </table>
            <input type="submit" name="submit" value="search">

        </form>
        <%            String errorMsg = (String) request.getAttribute("error");
            if (errorMsg != null) {
        %>
        <%=errorMsg%>
        <%}%>
        <br><br>

        <%

            HashMap<String, Integer> usageCategoryHashMap = (HashMap<String, Integer>) request.getAttribute("usageCategoryList");
            if (usageCategoryHashMap != null) {
                int numIntense = 0;
                int numNormal = 0;
                int numMild = 0;

                for (String category : usageCategoryHashMap.keySet()) {
                    int numberOfUser = usageCategoryHashMap.get(category);
                    if (category.equals("Intense")) {
                        numIntense = numberOfUser;
                    } else if (category.equals("Normal")) {
                        numNormal = numberOfUser;
                    } else {
                        numMild = numberOfUser;
                    }
                }

                int totalUser = numIntense + numNormal + numMild;
                int intensePercent = (int) (Math.round(100.0 * numIntense / totalUser));
                int normalPercent = (int) (Math.round(100.0 * numNormal / totalUser));
                int mildPercent = (int) (Math.round(100.0 * numMild / totalUser));


        %>
        <table border="1">
            <tr>
                <th>Category</th>
                <th>Number Of User</th>
                <th>Percentage</th>
            </tr>

            <tr>
                <td>Intense</td>
                <td><%=numIntense%></td>
                <td><%=intensePercent%></td>
            </tr>

            <tr>
                <td>Normal</td>
                <td><%=numNormal%></td>
                <td><%=normalPercent%></td>
            </tr>

            <tr>
                <td>Mild</td>
                <td><%=numMild%></td>
                <td><%=mildPercent%></td>
            </tr>

        </table>
        <%
            }
        %>
        <div class="right"><canvas id="myChart" width="300" height="300"></canvas></div>
             <%@include file="footer.jsp" %>
        <script>
            var dataUsage = [];
            $(document).ready(function () {
                var data = [
                    {
                        value: 0,
                        color: "#F7464A",
                        highlight: "#FF5A5E",
                        label: "Intense"
                    },
                    {
                        value: 0,
                        color: "#46BFBD",
                        highlight: "#5AD3D1",
                        label: "Normal"
                    },
                    {
                        value: 0,
                        color: "#FDB45C",
                        highlight: "#FFC870",
                        label: "Mild"
                    }
                ];

                // Get context with jQuery - using jQuery's .get() method.
                var ctx = $("#myChart").get(0).getContext("2d");
                var str = 'json/basic-usetime-report?startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&token=smuaAPP';

                $.getJSON(str, function (results) {
                    results.breakdown.forEach(function (area) {

                        if (area['intense-percent'] === 0) {
                            dataUsage.push(0);
                        } else if (area['intense-percent']) {
                            dataUsage.push(area['intense-percent']);
                        }

                        if (area['normal-percent']) {
                            dataUsage.push(area['normal-percent']);
                        } else if (area['normal-percent']) {
                            dataUsage.push(area['normal-percent']);
                        }



                        if (area['mild-percent']) {
                            dataUsage.push(area['mild-percent']);

                        } else if (area['mild-percent']) {
                            dataUsage.push(area['mild-percent']);
                        }




                    });
                    // This will get the first returned node in the jQuery collection.
                    console.log(dataUsage.length);
                    var i = 0;
                    dataUsage.forEach(function (percent) {
                        
                        data[i++].value = percent;
                        
                    });
                    var myNewChart = new Chart(ctx).Pie(data);
                });

            });
        </script>
       
    </body>
</html>