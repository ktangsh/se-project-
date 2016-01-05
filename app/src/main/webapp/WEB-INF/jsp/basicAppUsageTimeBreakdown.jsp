<%-- 
    Document   : basicAppUsageTimeBreakdown
    Created on : Oct 3, 2015, 3:29:18 PM
    Author     : famous
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.List"%>
<%@page import="smua.models.JSON.BasicAppUsage"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <script src="js/Chart.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">
        <title>JSP Page</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>

        <%            
        String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String[] selectedList = request.getParameterValues("category");

            if (startDate == null) {
                startDate = "YYYY-MM-DD";
            }
            if (endDate == null) {
                endDate = "YYYY-MM-DD";
            }
        %>

        <%!
            public String select(int index, String[] selectedList, String value) {
                if (selectedList == null) {
                    return "";
                }else if(selectedList[index].equals(value)){
                    return "selected";
                }
                return "";
            }
        %>

        <form action="/basicAppUsageTimeBreakdown" method="post">

            <h1>Basic App Report Breakdown</h1>
            <table>
                <tr><td>Start Date: </td><td><input type="date" name="startDate" value="<%=startDate%>"></td></tr>
                <tr><td>End Date: </td><td><input type="date" name="endDate" value="<%=endDate%>"></td></tr>
                <tr><td>Categories: </td>
                    <td>
                        <select name="category">

                            <option value="" <%=select(0,selectedList,"")%>></option>
                            <option value="year" <%=select(0,selectedList,"year")%>>Year</option>
                            <option value="gender" <%=select(0,selectedList,"gender")%>>Gender</option>
                            <option value="school" <%=select(0,selectedList,"school")%>>School</option>
                            <option value="cca" <%=select(0,selectedList,"cca")%>>CCA</option>
                        </select>

                        <select name="category">
                            <option value="" <%=select(1,selectedList,"")%>></option>
                            <option value="year" <%=select(1,selectedList,"year")%>>Year</option>
                            <option value="gender" <%=select(1,selectedList,"gender")%>>Gender</option>
                            <option value="school" <%=select(1,selectedList,"school")%>>School</option>
                            <option value="cca" <%=select(1,selectedList,"cca")%>>CCA</option>
                        </select>

                        <select name="category">
                                <option value="" <%=select(2,selectedList,"")%>></option>
                            <option value="year" <%=select(2,selectedList,"year")%>>Year</option>
                            <option value="gender" <%=select(2,selectedList,"gender")%>>Gender</option>
                            <option value="school" <%=select(2,selectedList,"school")%>>School</option>
                            <option value="cca" <%=select(2,selectedList,"cca")%>>CCA</option>
                        </select>

                        <select name="category">
                            <option value="" <%=select(3,selectedList,"")%>></option>
                            <option value="year" <%=select(3,selectedList,"year")%>>Year</option>
                            <option value="gender" <%=select(3,selectedList,"gender")%>>Gender</option>
                            <option value="school" <%=select(3,selectedList,"school")%>>School</option>
                            <option value="cca" <%=select(3,selectedList,"cca")%>>CCA</option>
                        </select>
                    </td>
            </table>


            <input type="submit" name="submit" value="search">

        </form>

        <%            ArrayList<String> errorList = (ArrayList<String>) request.getAttribute("errorList");
            if (errorList == null) {%>

        <%
            String output = "";
            BasicAppUsage basicAppUsage = (BasicAppUsage) request.getAttribute("basicAppUsage");
            if (basicAppUsage != null) {
                List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
                for (BasicAppUsage levelOne : basicAppUsageList) {
                    List<BasicAppUsage> levelOneList = levelOne.getBasicAppUsageList();

                    output += levelOne.toString() + "<ul>";

                    if (levelOne.getCategoryListResult() != null) {
                        output += "<li>";
                        List<LinkedHashMap<String, Integer>> result = levelOne.getCategoryListResult();
                        LinkedHashMap<String, Integer> intense = result.get(0);
                        output += "Intense User:" + intense.get("intense-count") + "(" + intense.get("intense-percent") + ")";
                        LinkedHashMap<String, Integer> normal = result.get(1);
                        output += "Normal User:" + normal.get("normal-count") + "(" + normal.get("normal-percent") + ")";
                        LinkedHashMap<String, Integer> mild = result.get(2);
                        output += "Mild User:" + mild.get("mild-count") + "(" + mild.get("mild-percent") + ")";
                        output += "</li>";
                    } else {
                        for (BasicAppUsage levelTwo : levelOneList) {
                            List<BasicAppUsage> levelTwoList = levelTwo.getBasicAppUsageList();
                            output += levelTwo.toString() + "<ul>";
                            if (levelTwo.getCategoryListResult() != null) {
                                output += "<li>";
                                List<LinkedHashMap<String, Integer>> result = levelTwo.getCategoryListResult();
                                LinkedHashMap<String, Integer> intense = result.get(0);
                                output += "Intense User:" + intense.get("intense-count") + "(" + intense.get("intense-percent") + ")";
                                LinkedHashMap<String, Integer> normal = result.get(1);
                                output += "Normal User:" + normal.get("normal-count") + "(" + normal.get("normal-percent") + ")";
                                LinkedHashMap<String, Integer> mild = result.get(2);
                                output += "Mild User:" + mild.get("mild-count") + "(" + mild.get("mild-percent") + ")";
                                output += "</li>";
                            } else {
                                for (BasicAppUsage levelThree : levelTwoList) {
                                    output += levelThree.toString() + "<ul>";
                                    List<BasicAppUsage> levelThreeList = levelThree.getBasicAppUsageList();
                                    if (levelThree.getCategoryListResult() != null) {
                                        output += "<li>";
                                        List<LinkedHashMap<String, Integer>> result = levelThree.getCategoryListResult();
                                        LinkedHashMap<String, Integer> intense = result.get(0);
                                        output += "Intense User:" + intense.get("intense-count") + "(" + intense.get("intense-percent") + ")";
                                        LinkedHashMap<String, Integer> normal = result.get(1);
                                        output += "Normal User:" + normal.get("normal-count") + "(" + normal.get("normal-percent") + ")";
                                        LinkedHashMap<String, Integer> mild = result.get(2);
                                        output += "Mild User:" + mild.get("mild-count") + "(" + mild.get("mild-percent") + ")";
                                        output += "</li>";
                                    } else {
                                        for (BasicAppUsage levelFour : levelThreeList) {
                                            output += levelFour.toString() + "<ul>";
                                            if (levelFour.getCategoryListResult() != null) {
                                                output += "<li>";
                                                List<LinkedHashMap<String, Integer>> result = levelFour.getCategoryListResult();
                                                LinkedHashMap<String, Integer> intense = result.get(0);
                                                output += "Intense User:" + intense.get("intense-count") + "(" + intense.get("intense-percent") + ")";
                                                LinkedHashMap<String, Integer> normal = result.get(1);
                                                output += "Normal User:" + normal.get("normal-count") + "(" + normal.get("normal-percent") + ")";
                                                LinkedHashMap<String, Integer> mild = result.get(2);
                                                output += "Mild User:" + mild.get("mild-count") + "(" + mild.get("mild-percent") + ")";
                                                output += "</li>";
                                            }
                                            output += "</ul>";

                                        }

                                    }
                                    output += "</ul>";
                                }

                            }
                            output += "</ul>";
                        }

                    }
                    output += "</ul>";
                }
            }
        %>

        <%if (output != null) {%>
        <%=output%>
        <%}%>

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


        <%}
        %>
        <%} else {
            for (String errorMsg : errorList) {
        %>
        <%=errorMsg + "<br>"%>
        <%
                }
            }
        %>
        <br/>
        <div class ="furthest-right">
            <div id="chartDDL">
                Click Me to Breakdown <br/>
                <button value="breakdown" onclick="breakdownfunction()">Breakdown</button>
            </div>
            <canvas id="myChart" width="300" height="300"></canvas>
        </div>

        <%@include file="footer.jsp" %>
        <script>
            var dataUsage = [];
            var orderArr = [];
            var data = [];
            var label = [];
            var ccaArr = [];
            $(document).ready(function () {

            <%
                String order = "";
                String[] orderArr = request.getParameterValues("category");
                if (orderArr != null) {
                    for (String curOrder : orderArr) {
                        if (!curOrder.equals("")) {
                            order += curOrder + ",";
                        }
                    }
                    if (order.length() > 0) {
                        order = order.substring(0, order.length() - 1);
                    }
                }
                String value = "\"<%=" + " label " + "%" + ">, Count: <%" + "= value" + " %" + ">,Percentage: <%" + "= Math.round(circumference / 6.283 * 100)" + "%" + "> %\"";
            %>


                var order = '<%=order%>';
                orderArr = '<%=orderArr%>';
                var str = 'json/basic-usetime-demographics-report?startdate=<%=request.getParameter("startDate")%>&enddate=<%=request.getParameter("endDate")%>&order=' + order + '&token=smuaAPP';
                var i = 0;
                $.getJSON(str, function (results) {
                    results.breakdown.forEach(function (area) {
                        var concat = "";
                        var obj = area;
                        concat = obj[Object.keys(area)[0]];
                        createData(concat, data, label, area);
                    });

                    var schoolArr = ['sis', 'socsc', 'business', 'law', 'accountancy', 'economics'];
                    var yearArr = ['2011', '2012', '2013', '2014', '2015'];
                    var genderArr = ['M', 'F'];

                    orderArr = order.split(",");

                    //populate the values into dropdownlist based on the user's category input
                    for (i = 0; i < orderArr.length; i++) {
                        if (orderArr[i] === 'school') {
                            var school = document.createElement("select");
                            school.setAttribute("id", "school");
                            document.getElementById("chartDDL").appendChild(school);
                            var schDropDownList = document.getElementById("school");

                            if (i !== 0) {
                                schoolArr.splice(0, 0, "");
                            }
                            for (var s = 0; s < schoolArr.length; s++) {
                                schDropDownList[schDropDownList.length] = new Option(schoolArr[s], schoolArr[s]);
                            }


                        } else if (orderArr[i] === 'gender') {
                            var gender = document.createElement("select");
                            gender.setAttribute("id", "gender");
                            document.getElementById("chartDDL").appendChild(gender);
                            var genderDropDownList = document.getElementById("gender");

                            if (i !== 0) {
                                genderArr.splice(0, 0, "");
                            }

                            for (var s = 0; s < genderArr.length; s++) {
                                genderDropDownList[genderDropDownList.length] = new Option(genderArr[s], genderArr[s]);
                            }

                        } else if (orderArr[i] === 'cca') {
                            var cca = document.createElement("select");
                            cca.setAttribute("id", "cca");
                            document.getElementById("chartDDL").appendChild(cca);
                            var ccaDropDownList = document.getElementById("cca");
                            console.log(ccaArr);
                            if (i !== 0) {
                                ccaArr.splice(0, 0, "");
                            }
                            for (var s = 0; s < ccaArr.length; s++) {
                                ccaDropDownList[ccaDropDownList.length] = new Option(ccaArr[s], ccaArr[s]);
                            }
                        } else if (orderArr[i] === 'year') {
                            var year = document.createElement("select");
                            year.setAttribute("id", "year");
                            document.getElementById("chartDDL").appendChild(year);
                            var yearDropDownList = document.getElementById("year");
                            if (i !== 0) {
                                yearArr.splice(0, 0, "");
                            }
                            for (var s = 0; s < yearArr.length; s++) {
                                yearDropDownList[yearDropDownList.length] = new Option(yearArr[s], yearArr[s]);
                            }
                        }
                    }
                });

                //re-currsive, call itself to continue to breakdown further
                function createData(concat, data, label, breakdown) {
                    if (breakdown['breakdown'][0]['breakdown'] === undefined) {
                        var curLevel = breakdown['breakdown'];
                        var obj = breakdown;

                        //the inner most breakdown level
                        if ($.inArray(obj[Object.keys(breakdown)[0]], ccaArr) === -1 && Object.keys(breakdown)[0] === 'cca') {
                            ccaArr.push(obj[Object.keys(breakdown)[0]]);
                        }
                        data[concat] = [];
                        label[concat] = [];
                        var dataArr = [];
                        var labelArr = [];
                        dataArr.push(curLevel[0]['intense-count']);
                        labelArr.push('intense-count');
                        dataArr.push(curLevel[1]['normal-count']);
                        labelArr.push('normal-count');
                        dataArr.push(curLevel[2]['mild-count']);
                        labelArr.push('mild-count');
                        data[concat] = dataArr;
                        label[concat] = labelArr;
                    }
                    //continue to break down further
                    else {
                        var size = breakdown['breakdown'].length;
                        data[concat] = [];
                        var dataArr = [];
                        var labelArr = [];

                        for (var i = 0; i < size; i++) {
                            var obj = breakdown['breakdown'][i];

                            if ($.inArray(obj[Object.keys(breakdown['breakdown'][i])[0]], ccaArr) === -1 && Object.keys(breakdown['breakdown'][i])[0] === 'cca') {
                                ccaArr.push(obj[Object.keys(breakdown['breakdown'][i])[0]]);
                            }
                            var newconcat = concat + "" + obj[Object.keys(breakdown['breakdown'][i])[0]];
                            dataArr.push(breakdown['breakdown'][i]['count']);
                            labelArr.push(obj[Object.keys(breakdown['breakdown'][i])[0]]);
                            createData(newconcat, data, label, obj);
                        }
                        data[concat] = dataArr;
                        label[concat] = labelArr;
                    }
                }
            });

            function breakdownfunction() {
                console.log(ccaArr);
                var concat = "";
                console.log(orderArr);
                for (i = 0; i < orderArr.length; i++) {
                    if (orderArr[i] === 'school') {
                        console.log("HELLOO SCHOOL");
                        var schoolOption = document.getElementById("school");
                        concat += schoolOption.options[schoolOption.selectedIndex].value;
                    } else if (orderArr[i] === 'gender') {
                        var genderOption = document.getElementById("gender");
                        concat += genderOption.options[genderOption.selectedIndex].value;
                    } else if (orderArr[i] === 'cca') {
                        var ccaOption = document.getElementById("cca");
                        concat += ccaOption.options[ccaOption.selectedIndex].value;
                    } else if (orderArr[i] === 'year') {
                        var yearOption = document.getElementById("year");
                        concat += yearOption.options[yearOption.selectedIndex].value;
                    }
                }
                var chartdata = [];
                for (var i = 0; i < data[concat].length; i++) {
                    var obj = {value: data[concat][i],
                        color: "#00dc9b",
                        highlight: "#FF5A5E",
                        label: label[concat][i]};
                    chartdata.push(obj);
                }
                $('#myChart').replaceWith("<canvas id='myChart' width='400' height='400'></canvas>");
                var ctx = document.getElementById("myChart").getContext("2d");
                var myNewChart = new Chart(ctx).Pie(chartdata, {
                    tooltipTemplate:<%=value%>
                });
            }

        </script>
    </body>
</html>
