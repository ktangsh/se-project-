<%-- 
    Document   : smartphoneHeatmap
    Created on : Oct 12, 2015, 9:42:56 AM
    Author     : shtang.2014
--%>

<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="smua.models.JSON.UsageHeatmap"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-2.1.4.min.js" charset="utf-8"></script>
        <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="css/main.css">

        <title>Error</title>
    </head>
    <body>
    <center>
        <%
            Map<String, Object> errorMap = (Map<String, Object>) request.getAttribute("error");
            String responseStatus =  request.getAttribute("responseStatus").toString();

            out.println("<h1>Error " + responseStatus + "</h1>");
            if(responseStatus.equals("404")){
                out.println("The page you are looking for cannot be found<br/>");
            }else{
                out.println("Something unexpected happened<br/>");
            }
            out.println("Click <a href='/login'>here</a> to return to login page");
            
        %>
        </center>
            <%@include file="footer.jsp" %>
            </body>
            </html>
