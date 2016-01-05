<%-- 
    Document   : basicAppUsageAppCat
    Created on : Oct 7, 2015, 12:07:37 PM
    Author     : ChuQian
--%>

<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <script src="js/Chart.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">
        <script>

            var dataUsage = [];
            $(document).ready(function () {
                var data = {
                    labels: [],
                    datasets: [
                        {
                            fillColor: "rgba(220,220,220,0.5)",
                            strokeColor: "rgba(220,220,220,0.8)",
                            highlightFill: "rgba(220,220,220,0.75)",
                            highlightStroke: "rgba(220,220,220,1)",
                            data: []
                        }
                    ]
                };
                // Get context with jQuery - using jQuery's .get() method.
                var ctx = $("#myChart").get(0).getContext("2d");
                var str = 'json/basic-appcategory-report?startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&token=smuaAPP';

                $.getJSON(str, function (results) {
                    results.breakdown.forEach(function (area) {
                        data.labels.push(area['category-name']); //add new item to an end of the array
                        data.datasets[0].data.push(area['category-duration']);

                    });
                    // This will get the first returned node in the jQuery collection.
                    var myNewChart = new Chart(ctx).Bar(data);


                    var pieData = [
                        {
                            value: 0,
                            color: "#F7464A",
                            highlight: "#FF5A5E",
                            label: "books"
                        },
                        {
                            value: 0,
                            color: "#46BFBD",
                            highlight: "#5AD3D1",
                            label: "education"
                        },
                        {
                            value: 0,
                            color: "#FDB45C",
                            highlight: "#FFC870",
                            label: "entertainment"
                        },
                        {
                            value: 0,
                            color: "#ADFF2F",
                            highlight: "#FFC870",
                            label: "fitness"
                        },
                        {
                            value: 0,
                            color: "#A52A2A",
                            highlight: "#FFC870",
                            label: "games"
                        },
                        {
                            value: 0,
                            color: "#FFFF00",
                            highlight: "#FFC870",
                            label: "information"
                        },
                        {
                            value: 0,
                            color: "#2F4F4F",
                            highlight: "#FFC870",
                            label: "library"
                        },
                        {
                            value: 0,
                            color: "#FF00FF",
                            highlight: "#FFC870",
                            label: "local"
                        },
                        {
                            value: 0,
                            color: "#9932CC",
                            highlight: "#FFC870",
                            label: "others"
                        },
                        {
                            value: 0,
                            color: "#DC143C",
                            highlight: "#FFC870",
                            label: "social"
                        },
                        {
                            value: 0,
                            color: "#3CB371",
                            highlight: "#FFC870",
                            label: "tools"
                        }
                    ];

                    // Get context with jQuery - using jQuery's .get() method.
                    var pieCtx = $("#myPieChart").get(0).getContext("2d");

                    $.getJSON(str, function (results) {
                        results.breakdown.forEach(function (area) {

                            if (area['category-percent'] === 0) {
                                dataUsage.push(0);
                            } else if (area['category-percent']) {
                                dataUsage.push(area['category-percent']);
                            }




                        });
                        // This will get the first returned node in the jQuery collection.
                        console.log(dataUsage.length);
                        var i = 0;
                        dataUsage.forEach(function (percent) {

                            pieData[i++].value = percent;

                        });
                        var myNewPieChart = new Chart(pieCtx).Pie(pieData);
                    });

                });
            });
        </script>

        <title>Basic App Usage by App Category</title>
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
        
        <form action="/basicAppUsageAppCat" method="post">
            <h1>Basic App Usage by App Category</h1>
            <table>
                <tr><td>Start Date: </td><td><input type="date" name="startDate" value="<%=startDate%>"></td></tr>
                <tr><td>End Date: </td><td><input type="date" name="endDate" value="<%=endDate%>"></td></tr>
            </table>
            <input type="submit" name="submit" value="search">
            <br>
            
            <%        String errorMsg = (String) request.getAttribute("error");
                if (errorMsg != null) {
            %>
            <label><%=errorMsg%></label>
            <%}%>
        </form>

        <%
            LinkedHashMap<String, Double> result = (LinkedHashMap<String, Double>) request.getAttribute("categoryList");

            if (result != null) {
                Iterator<String> iter = result.keySet().iterator();
                ArrayList<Double> valueList = new ArrayList<Double>(result.values());
                double sum = 0;
                for (double value : valueList) {
                    sum += value;
                }
        %>
        <table border = "1">
            <tr><th>App Category </th> <th>Average Usage Time (in seconds) </th> <th>Percentage</th></tr>
                    <%
                        while (iter.hasNext()) {
                            String category = iter.next();
                            int usageTime = (int) Math.round(result.get(category));
                            int percentage = (int) Math.round(100.0 * result.get(category) / sum);

                    %>
            <tr>
                <td><%=category%></td><td><%=usageTime%></td><td><%=percentage%></td>

            </tr>
            <%}
                }%>
        </table>
        <div>
            <canvas id="myChart" width="400" height="350"></canvas>
            <canvas id="myPieChart" width="300" height="350"></canvas>
        </div>
        <%@include file="footer.jsp" %>
    </body>


</html>
