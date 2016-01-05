<%-- 
    Document   : smartphoneHeatmap
    Created on : Oct 12, 2015, 9:42:56 AM
    Author     : shtang.2014
--%>

<%@page import="smua.models.JSON.UsageHeatmap"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <link rel="stylesheet" href="css/main.css">

        <title>JSP Page</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <%!
            public String selectedHours(String value, String selected) {
                if (value.equals(selected) || (selected == null && value.equals("00"))) {
                    return "selected";
                }
                return "";
            }

            public String selectedMinutes(String value, String selected) {
                if (value.equals(selected) || (selected == null && value.equals("00"))) {
                    return "selected";
                }
                return "";
            }

            public String selectedSeconds(String value, String selected) {
                if (value.equals(selected) || (selected == null && value.equals("00"))) {
                    return "selected";
                }
                return "";
            }

            public String selectedFloor(String value, String selected) {
                if (value.equals(selected) || (selected == null && value.equals("B1"))) {
                    return "selected";
                }
                return "";
            }
        %>
        <%
            String date = request.getParameter("date");
            String hours = request.getParameter("hours");
            String minutes = request.getParameter("minutes");
            String seconds = request.getParameter("seconds");
            String selectedfloor = request.getParameter("floor");

            if (date == null) {
                date = "YYYY-MM-DD";
            }

        %>
        <h1 style="margin-left:10px">Smartphone Usage Heatmap </h1>
        <form action="/smartphoneHeatmap" method='post' style="margin-left:10px">
            <table>
                <tr><td>Date:</td><td><input type='date' name="date" value="<%=date%>"/></td></tr>
                <tr>
                    <td>HH:</td>
                    <td><select name="hours">
                            <option value="00" <%=selectedHours("00", hours)%>>00</option>
                            <option value="01" <%=selectedHours("01", hours)%>>01</option>
                            <option value="02" <%=selectedHours("02", hours)%>>02</option>
                            <option value="03" <%=selectedHours("03", hours)%>>03</option>
                            <option value="04" <%=selectedHours("04", hours)%>>04</option>
                            <option value="05" <%=selectedHours("05", hours)%>>05</option>
                            <option value="06" <%=selectedHours("06", hours)%>>06</option>
                            <option value="07" <%=selectedHours("07", hours)%>>07</option>
                            <option value="08" <%=selectedHours("08", hours)%>>08</option>
                            <option value="09" <%=selectedHours("09", hours)%>>09</option>
                            <%                    for (int i = 10; i <= 23; i++) {
                            %>
                            <option value="<%=i%>" <%=selectedHours(i + "", hours)%>><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        MM: <select name="minutes">
                            <option value="00" <%=selectedMinutes("00", minutes)%>>00</option>
                            <option value="01" <%=selectedMinutes("01", minutes)%>>01</option>
                            <option value="02" <%=selectedMinutes("02", minutes)%>>02</option>
                            <option value="03" <%=selectedMinutes("03", minutes)%>>03</option>
                            <option value="04" <%=selectedMinutes("04", minutes)%>>04</option>
                            <option value="05" <%=selectedMinutes("05", minutes)%>>05</option>
                            <option value="06" <%=selectedMinutes("06", minutes)%>>06</option>
                            <option value="07" <%=selectedMinutes("07", minutes)%>>07</option>
                            <option value="08" <%=selectedMinutes("08", minutes)%>>08</option>
                            <option value="09" <%=selectedMinutes("09", minutes)%>>09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>" <%=selectedMinutes(i + "", minutes)%>><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        SS: <select name="seconds">
                            <option value="00" <%=selectedSeconds("00", seconds)%>>00</option>
                            <option value="01" <%=selectedSeconds("01", seconds)%>>01</option>
                            <option value="02" <%=selectedSeconds("02", seconds)%>>02</option>
                            <option value="03" <%=selectedSeconds("03", seconds)%>>03</option>
                            <option value="04" <%=selectedSeconds("04", seconds)%>>04</option>
                            <option value="05" <%=selectedSeconds("05", seconds)%>>05</option>
                            <option value="06" <%=selectedSeconds("06", seconds)%>>06</option>
                            <option value="07" <%=selectedSeconds("07", seconds)%>>07</option>
                            <option value="08" <%=selectedSeconds("08", seconds)%>>08</option>
                            <option value="09" <%=selectedSeconds("09", seconds)%>>09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>" <%=selectedSeconds(i + "", seconds)%>><%=i%></option>
                            <%
                                }
                            %>
                        </select></td>
                    </td>
                </tr>
                <tr>
                    <td>Floor: </td>
                    <td><select name="floor">
                            <option value="B1" <%=selectedFloor("B1", selectedfloor)%>>B1</option>
                            <option value="L1" <%=selectedFloor("L1", selectedfloor)%>>L1</option>
                            <option value="L2" <%=selectedFloor("L2", selectedfloor)%>>L2</option>
                            <option value="L3" <%=selectedFloor("L3", selectedfloor)%>>L3</option>
                            <option value="L4" <%=selectedFloor("L4", selectedfloor)%>>L4</option>
                            <option value="L5" <%=selectedFloor("L5", selectedfloor)%>>L5</option>
                        </select>
                    </td>
                </tr>
            </table>
            <input type='submit' id='heatmap-btn' value='submit'>
        </form>
        <br/>
        <%
            String errorMsg = (String) request.getAttribute("errorMsg");
            int floorInt = 0;
            if (errorMsg != null) {%>
        <font color="red"><%=errorMsg%></font>
        <%} else {
            List<UsageHeatmap> usageHeatMapList = (List<UsageHeatmap>) request.getAttribute("usageHeatMapList");
            if (usageHeatMapList != null) {%>
        <table border="1" style="margin-left:10px">
            <tr><th>Semantic Place</th><th>Number Of People Using Phone</th>
                <th>Crowd Density</th></tr>
                    <%for (UsageHeatmap usageHeatmap : usageHeatMapList) {
                            String place = usageHeatmap.getSemanticPlace();
                            int numUsers = usageHeatmap.getNumberOfpeopleUsingPhone();
                            int density = usageHeatmap.getCrowdDensity();
                    %>
            <tr><td><%=place%></td><td><%=numUsers%></td><td><%=density%></td></tr>
            <%}%>
        </table>


        <div class="row" style="margin-left:10px">
            <div class="col-md-3">
                <div id="chartLegend">
                    <legend>
                        <h3>Data</h3>
                    </legend>

                    <strong>Semantic place: </strong> <br/>
                    <span id="semantic-place">-</span> <br/>

                    <br/>

                    <strong>Number of people: </strong> <br/>
                    <span id="num-people">-</span> <br/>

                    <br/>

                    <strong>Crowd density: </strong> <br/>
                    <span id="crowd-density">-</span> <br/>

                </div>
                <ul id="legend">
                    <li>
                        <span class="color-block" ></span>
                        0
                    </li>

                    <li>
                        <span class="color-block"></span>
                        1 to 3
                    </li>

                    <li>
                        <span class="color-block"></span>
                        4 to 7
                    </li>

                    <li>
                        <span class="color-block"></span>
                        8 to 13
                    </li>

                    <li>
                        <span class="color-block"></span>
                        14 to 20
                    </li>

                    <li>
                        <span class="color-block"></span>
                        21 and more
                    </li>

                </ul>
            </div>
            <div class="col-md-9">
                <%
                    String floor = request.getParameter("floor");
                    if (floor != null) {

                        if (floor.equals("B1")) {
                            floorInt = 0;
                %>
                <jsp:include page="landmarks/B1.jsp"></jsp:include>
                <%
                } else if (floor.equals("L1")) {
                    floorInt = 1;
                %>
                <jsp:include page="landmarks/L1.jsp"></jsp:include>
                <%
                } else if (floor.equals("L2")) {
                    floorInt = 2;
                %>
                <jsp:include page="landmarks/L2.jsp"></jsp:include>
                <%
                } else if (floor.equals("L3")) {
                    floorInt = 3;
                %>
                <jsp:include page="landmarks/L3.jsp"></jsp:include>
                <%
                } else if (floor.equals("L4")) {
                    floorInt = 4;
                %>
                <jsp:include page="landmarks/L4.jsp"></jsp:include>
                <%
                } else if (floor.equals("L5")) {
                    floorInt = 5;
                %>
                <jsp:include page="landmarks/L5.jsp"></jsp:include>
                <%                        }

                            }
                        }
                    }
                %>
                <script>
                    var colors = ['rgb(240,249,232)', 'rgb(204,235,197)', 'rgb(168,221,181)', 'rgb(123,204,196)', 'rgb(78,179,211)', 'rgb(43,140,190)', 'rgb(8,88,158)'];
                    var data = {};

                    $(document).ready(function () {
                        $('#semantic-place').html('-');
                        $('#num-people').html('-');

                        var str = '/json/usage-heatmap?date=<%=request.getParameter("date")%>&time=<%=request.getParameter("hours") + ":" + request.getParameter("minutes") + ":" + request.getParameter("seconds")%>&floor=<%=floorInt%>&token=smuaAPP';
                        $.getJSON(str, function (results) {
                            results.heatmap.forEach(function (area) {
                                var id = area['semantic-place'];
                                var density = area['crowd-density'];

                                data[id] = area;

                                $('#' + id).css('fill', colors[density]);
                            });
                        });

                    });
                    $(document).on('mouseover', '.heatmap-area', function (event) {
                        var id = $(event.target).attr('id');
                        var areaData = data[id];
                        console.log(areaData);

                        if (areaData) {
                            $('#semantic-place').html(areaData['semantic-place']);
                            $('#num-people').html(areaData['num-people-using-phone']);
                            $('#crowd-density').html(areaData['crowd-density']);
                        } else {
                            $('#semantic-place').html('-');
                            $('#num-people').html('-');
                            $('#crowd-density').html('-');
                        }
                    });


                </script>

            </div>
        </div>

        <%@include file="footer.jsp" %>
    </body>
</html>
