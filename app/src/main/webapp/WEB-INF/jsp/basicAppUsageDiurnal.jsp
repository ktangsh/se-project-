<%-- 
    Document   : basicAppUsageDiurnal
    Created on : Oct 4, 2015, 5:08:54 PM
    Author     : famous
--%>

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
            $(document).ready(function () {

                var data = {
                    labels: [],
                    datasets: [
                        {
                            fillColor: "rgba(220,220,220,0.2)",
                            strokeColor: "rgba(108,230,133,1)",
                            pointColor: "rgba(37,86,46,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(220,220,220,1)",
                            data: []
                        }
                    ]
                }
                
                var genderFilter = '<%=request.getParameter("gender")%>';
                var yearFilter = '<%=request.getParameter("year")%>';
                var schoolFilter = '<%=request.getParameter("school")%>';
                
                if(genderFilter === ''){
                    genderFilter = 'NA';
                }
                
                if(yearFilter === ''){
                    yearFilter = 'NA';
                }
                
                if(schoolFilter === ''){
                    schoolFilter = 'NA';
                }
                var ctx = $("#myChart").get(0).getContext("2d");
                var str = 'json/basic-diurnalpattern-report?date=<%=request.getParameter("date")%>&genderfilter='+genderFilter+'&yearfilter='+yearFilter+'&schoolfilter='+schoolFilter+'&token=smuaAPP';

                $.getJSON(str, function (results) {
                    results.breakdown.forEach(function (area) {
                        data.labels.push(area['period']); //add new item to an end of the array
                        data.datasets[0].data.push(area['duration']);

                    });
                    // This will get the first returned node in the jQuery collection.
                    var myNewChart = new Chart(ctx).Line(data);
                });
            });
        </script>
        <title>JSP Page</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <%            
            String date = request.getParameter("date");
            String selectedGender = request.getParameter("gender");
            String selectedSchool = request.getParameter("school");
            String selectedYear = request.getParameter("year");

            if (date == null) {
                date = "YYYY-MM-DD";
            }
        %>

        <%!
            public String select(String selected, String value) {
                if(selected!=null && selected.equals(value)){
                    return " selected";
                }
                return "";
            }
        %>
        
        <%            
            String[] years = {"2011", "2012", "2013", "2014", "2015"};
            String[] school = {"sis", "law", "business", "socsc", "economics", "accountancy"};
            String[] gender = {"M", "F"};
        %>
        <h1>Diurnal Usage Report</h1>
        <form action="" method="POST">
            Date: <input type="date" name="date" value="<%=date%>"/>
            Gender: 
            <select name="gender">
                <option value=""></option>
                <%for (String gen : gender) {%>
                <option value=<%=gen%> <%=select(selectedGender,gen)%>><%=gen%></option>
                <%}%>
            </select>
            School 
            <select name="school">
                <option value=""></option>
                <%for (String sch : school) {%>
                <option value=<%=sch%> <%=select(selectedSchool,sch)%>><%=sch%></option>
                <%}%>
            </select>
            Year: 
            <select name="year">
                <option value=""></option>
                <%for (String year : years) {%>
                <option value=<%=year%> <%=select(selectedYear,year)%>><%=year%></option>
                <%}%>
            </select>
            <input type="submit" value="SUBMIT"/>
        </form>
        <%
            String errorMsg = (String) request.getAttribute("error");
            if (errorMsg != null) {
        %>
        <label><%=errorMsg%></label>
        <%}%>
        <%
            LinkedHashMap<String, Integer> result = (LinkedHashMap<String, Integer>) request.getAttribute("basicAppDiurnalResult");

            if (result != null) {
                Iterator<String> iter = result.keySet().iterator();
        %>
        <table border = "1">
            <tr><th> Period </th> <th> Average Usage Time (in seconds)</th></tr>
                    <%
                        while (iter.hasNext()) {
                            String key = iter.next();
                            String value = result.get(key).toString();

                    %>
            <tr>
                <td><%=key%></td><td><%=value%></td>

            </tr>
            <%}
                }%>
        </table>
        <canvas id="myChart" width="1200" height="400"></canvas>
        
        <%@include file="footer.jsp" %>
    </body>
</html>
