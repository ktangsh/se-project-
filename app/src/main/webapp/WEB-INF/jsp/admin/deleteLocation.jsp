<%-- 
    Document   : bootstrapDelete
    Created on : Oct 16, 2015, 9:34:42 AM
    Author     : ChuQian
--%>

<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="smua.models.JSON.ErrorMsg"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="smua.models.JSON.BootstrapError"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE HTML>
<html>
    <head> 
        <title>SMUA</title> 
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

        <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
              rel="stylesheet">
        <link href="../../css/main.css" rel="stylesheet" type="text/css"/>
        <script src="//code.jquery.com/jquery-1.11.0.min.js"></script>

    </head>
    <body>
        <%@include file="../navbar.jsp" %>
        <div class="container-fluid">
            <div class="row text-center">
                <div class="col-lg-3"><h1>Bootstrap Delete</h1></div>
            </div>

            <div class="row">
                <form action="/deleteLocation" method="post">
                    <div class="col-lg-9">
                        Mac Address: <input type="text" name="macAddress"><br>
                        Start Date: <input type="date" name="startDate">
                        HH: <select name="starthours">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%                    for (int i = 10; i <= 23; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        MM: <select name="startminutes">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        SS: <select name="startseconds">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        <br>
                        End Date: <input type="date" name="endDate">
                        HH: <select name="endhours">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%                    for (int i = 10; i <= 23; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        MM: <select name="endminutes">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        SS: <select name="endseconds">
                            <option selected="selected" value="00">00</option>
                            <option value="01">01</option>
                            <option value="02">02</option>
                            <option value="03">03</option>
                            <option value="04">04</option>
                            <option value="05">05</option>
                            <option value="06">06</option>
                            <option value="07">07</option>
                            <option value="08">08</option>
                            <option value="09">09</option>
                            <%
                                for (int i = 10; i <= 59; i++) {
                            %>
                            <option value="<%=i%>"><%=i%></option>
                            <%
                                }
                            %>
                        </select>
                        <br>
                        Location ID: <input type="text" name="locationID"><br>
                        Semantic Place: <input type="text" name="semanticPlace"><br>
                        <input type="submit" name="submit" value="Delete">
                    </div>

                </form>

            </div>


            <%
                if (request.getAttribute("errorList") != null) {
                    ArrayList<String> errorList = (ArrayList<String>) request.getAttribute("errorList");
                    for (String error : errorList) {
                        out.print(error + "<br>");
                    }
                } else if (request.getAttribute("rowsDeleted") != null) {
                    int numOfRowDeleted = (Integer) request.getAttribute("rowsDeleted");
                    if (numOfRowDeleted > 0) {
                        out.print("Number of rows deleted :" + numOfRowDeleted);
                    } else {
                        out.print("Number of rows deleted: 0");
                    }
                }

                if (request.getParameter("rowsDeleted") != null) {
                    out.print("HELLO");
                }
            %>

            <%@include file="../footer.jsp" %>
        </div>

    </body>

</html>


