<%-- 
    Document   : topK
    Created on : Oct 2, 2015, 12:12:27 PM
    Author     : shtang.2014
--%>

<%@page import="java.util.Collections"%>
<%@page import="smua.models.JSON.TopKApp"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="smua.models.Entities.App"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <script src="js/Chart.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">
        <script>
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
                var str = 'json/top-k-most-used-apps?k=<%=request.getParameter("topKValue")%>&startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&school=<%=request.getParameter("school")%>&token=smuaAPP';

                $.getJSON(str, function (results) {
                    results.results.forEach(function (area, i) {
                        data.labels.push(area['app-name']); //add new item to an end of the array
                        data.datasets[0].data.push(area['duration']);

                    });
                    // This will get the first returned node in the jQuery collection.
                    var myNewChart = new Chart(ctx).Bar(data);
                });
            });
        </script>
        <title>Top K App</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <h1>Top-K most used apps (given a school)</h1>
        <form action="/topKApp" method="post">
            <%
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                if (startDate == null) {
                    startDate = "YYYY-MM-DD";
                }
                if (endDate == null) {
                    endDate = "YYYY-MM-DD";
                }
                String topKValue = request.getParameter("topKValue");
                if (topKValue == null) {
                    topKValue = "3";
                }
            %>
            <table>
                <tr><td>Start Date: </td><td><input type="date" name="startDate" value="<%=startDate%>"/></td></tr>
                <tr><td>End Date: </td><td><input type="date" name="endDate" value="<%=endDate%>"/></td></tr>

                <tr><td>Top-K value: </td>
                    <td>
                        <select name="topKValue">
                            <%
                                //repopulating the k value if previously selected   
                                for (int i = 1; i <= 10; i++) {
                                    String selected = "";
                                    if (Integer.parseInt(topKValue) == i) {
                                        selected = " selected";

                                    }
                                    out.println("<option value=" + i + selected + ">" + i + "</option>");
                                }
                            %>
                        </select>
                        School: 
                        <select name="school">
                            <%
                                String category = request.getParameter("school");

                                String[] schools = {"sis", "business", "economics", "accountancy", "socsc", "law"};
                                for (String s : schools) {
                                    String selected = "";
                                    if (s.equals(category)) {
                                        selected = " selected";
                                    }
                                    out.println("<option value=\"" + s + "\"" + selected + ">" + s + "</option>");
                                }
                            %>

                        </select>
                    </td>
                </tr>
            </table>
            <input type="submit" name="submit" value="search"><br/>
        </form>
        </br>
        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null) {
                out.println("<font color='red'>" + errorMessage + "</font><br/>");
            }
            ArrayList<TopKApp> list = (ArrayList<TopKApp>) request.getAttribute("appList");
            if (errorMessage == null && list != null && list.size() != 0) {%>
        <table border="1">
            <tr>
                <th>Rank</th>
                <th>Name</th>
                <th>Duration</th>
            </tr>
            <%

                HashMap<Integer, ArrayList<String>> rankMap = new HashMap<Integer, ArrayList<String>>();
                HashMap<Integer, Integer> usageMap = new HashMap<Integer, Integer>();
                for (TopKApp app : list) {
                    int rank = app.getRank();
                    String name = app.getName();
                    int duration = app.getDuration();
                    if (rankMap.containsKey(rank)) {
                        ArrayList<String> appList = rankMap.get(rank);
                        appList.add(name);
                    } else {
                        ArrayList<String> appList = new ArrayList<String>();
                        appList.add(name);
                        rankMap.put(rank, appList);
                        usageMap.put(rank, duration);
                    }
                }

                Iterator<Integer> rankIter = rankMap.keySet().iterator();

                while (rankIter.hasNext()) {
                    String appNames = "";
                    int rank = rankIter.next();
                    ArrayList<String> appList = rankMap.get(rank);
                    Collections.sort(appList);
                    int duration = usageMap.get(rank);
                    for (String s : appList) {
                        appNames += s + ",";
                    }
                    if (appNames.indexOf(',') != -1) {
                        appNames = appNames.substring(0, appNames.length() - 1);
                    }

            %>
            <tr>
                <td><%=rank%></td>
                <td><%=appNames%></td>
                <td><%=duration%></td>
            </tr>
            <%}
                }
            %>
        </table>
        <div>
            <canvas id="myChart" width="350" height="300"></canvas>
        </div>
        <%@include file="footer.jsp" %>
    </body>
</html>
