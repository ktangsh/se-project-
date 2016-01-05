<%-- 
    Document   : topKStudents
    Created on : Oct 3, 2015, 12:05:46 PM
    Author     : jennifer
--%>

<%@page import="smua.models.JSON.TopKStudent"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <script src="js/Chart.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">
        <script>

            var app = {};
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
                var str = '/json/top-k-most-used-students?k=<%=request.getParameter("topKValue")%>&startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&appcategory=<%=request.getParameter("category")%>&token=smuaAPP';
                $.getJSON(str, function (results) {
                    results.results.forEach(function (area) {
                        data.labels.push(area['name']);
                        data.datasets[0].data.push(area['duration']);
                        var id = area['macAddress'];
                        app[id] = area;

                    });
                    // This will get the first returned node in the jQuery collection.
                    var myNewChart = new Chart(ctx).Bar(data);
                });
            });
        </script>
        <title>Top-K Students (given App category)</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <h1>Top-K students with most app usage (given an app category)</h1>
        <form action="/topKStudent" method="post">
            <%                String startDate = request.getParameter("startDate");
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

                <tr>
                    <td>Top-K value: </td>
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

                        Category: 
                        <select name="category">
                            <%
                                String category = request.getParameter("category");
                                String[] categories = {"books", "social", "education", "entertainment", "information", "library", "local", "tools", "fitness", "games", "others"};
                                for (String s : categories) {
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
            <input type="submit" name="submit" value="search">
        </form>
        <br/>
        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null) {
                out.println("<font color='red'>" + errorMessage + "</font><br/>");
            }

            ArrayList<TopKStudent> topKList = (ArrayList<TopKStudent>) request.getAttribute("topKList");
            if (errorMessage == null && topKList != null && topKList.size() != 0) {
        %>
        <table border="1">
            <tr>
                <th>Rank</th>
                <th>Mac Address</th>
                <th>Name</th>
                <th>Duration</th>
            </tr>
            <%
                //all three hashmaps contains rank as the key 
                HashMap<Integer, Integer> durationMap = new HashMap<Integer, Integer>();
                HashMap<Integer, ArrayList<String>> macAddressMap = new HashMap<Integer, ArrayList<String>>();
                HashMap<Integer, ArrayList<String>> nameMap = new HashMap<Integer, ArrayList<String>>();

                for (TopKStudent student : topKList) {
                    int rank = student.getRank();
                    String name = student.getName();
                    String macAddress = student.getMacAddress();

                    if (durationMap.containsKey(rank)) {
                        ArrayList<String> nameList = nameMap.get(rank);
                        ArrayList<String> macAddressList = macAddressMap.get(rank);
                        nameList.add(name);
                        macAddressList.add(macAddress);
                    } else {
                        durationMap.put(rank, student.getDuration());

                        ArrayList<String> nameList = new ArrayList<String>();
                        nameList.add(student.getName());
                        nameMap.put(rank, nameList);

                        ArrayList<String> macAddressList = new ArrayList<String>();
                        macAddressList.add(student.getMacAddress());
                        macAddressMap.put(rank, macAddressList);
                    }
                }

                Iterator<Integer> durationIter = durationMap.keySet().iterator();

                while (durationIter.hasNext()) {
                    String names = "";
                    String macAddresses = "";
                    int rank = durationIter.next();
                    int duration = durationMap.get(rank);
                    ArrayList<String> nameList = nameMap.get(rank);
                    ArrayList<String> macAddressList = macAddressMap.get(rank);
                    for (String s : nameList) {
                        names += s + ",";
                    }

                    names = names.substring(0, names.length() - 1);

                    for (String s : macAddressList) {
                        macAddresses += s + ",";
                    }
                    macAddresses = macAddresses.substring(0, macAddresses.length() - 1);
            %>
            <tr>
                <td><%=rank%></td>
                <td><%=macAddresses%></td>
                <td><%=names%></td>
                <td><%=duration%></td>
            </tr>
            <%
                    }
                }%>
        </table>
        <div>
            <canvas id="myChart" width="350" height="300"></canvas>
        </div>
        <%@include file="footer.jsp" %>
    </body>
</html>
