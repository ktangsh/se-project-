<%-- 
    Document   : topKSchool
    Created on : Oct 6, 2015, 9:29:46 AM
    Author     : shtang.2014
--%>

<%@page import="java.text.ParseException"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="smua.models.JSON.TopKSchool"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.LinkedHashMap"%>
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
                var str = 'json/top-k-most-used-schools?k=<%=request.getParameter("topKValue")%>&startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&appcategory=<%=request.getParameter("category")%>&token=smuaAPP';

                $.getJSON(str, function (results) {
                    results.results.forEach(function (area) {
                        data.labels.push(area['school']); //add new item to an end of the array
                        data.datasets[0].data.push(area['duration']);

                    });
                    // This will get the first returned node in the jQuery collection.
                    var myNewChart = new Chart(ctx).Bar(data);
                });
            });
        </script>
        <title>Top-K Schools (given App category)</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <h1>Top-K schools with most app usage (given an app category)</h1>
        <form action="/topKSchool" method="post">
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
            ArrayList<TopKSchool> topKSchoolList = (ArrayList<TopKSchool>) request.getAttribute("topKSchoolList");
            if (errorMessage == null && topKSchoolList != null && topKSchoolList.size() != 0) {
        %>
        <table border="1">
            <tr>
                <th>Rank</th>
                <th>School</th>
                <th>Duration</th>
            </tr>

            <%
                //hashmaps contains rank as the key 
                HashMap<Integer, Integer> durationMap = new HashMap<Integer, Integer>();
                HashMap<Integer, ArrayList<String>> schoolMap = new HashMap<Integer, ArrayList<String>>(); //key being an arraylist of schools 

                for (TopKSchool school : topKSchoolList) {
                    int rank = school.getRank();
                    String name = school.getSchool();
                    if (durationMap.containsKey(rank)) {
                        ArrayList<String> schoolList = schoolMap.get(rank);
                        schoolList.add(name);
                    } else {
                        durationMap.put(rank, school.getDuration());

                        ArrayList<String> schoolList = new ArrayList<String>();
                        schoolList.add(school.getSchool());
                        schoolMap.put(rank, schoolList);
                    }
                }

                Iterator<Integer> schoolIter = schoolMap.keySet().iterator();

                while (schoolIter.hasNext()) {
                    String schools = "";
                    int rank = schoolIter.next();
                    int duration = durationMap.get(rank);
                    ArrayList<String> nameList = schoolMap.get(rank);
                    for (String s : nameList) {
                        schools += s + ",";
                    }

                    schools = schools.substring(0, schools.length() - 1);
            %>               
            <tr>
                <td><%=rank%></td>
                <td><%=schools%></td>
                <td><%=duration%></td>
            </tr>            
            <%
                    }
                }
            %>
        </table>
        <%@include file="footer.jsp" %>
        <div>
            <canvas id="myChart" width="350" height="300"></canvas>
        </div>
    </body>
</html>
