<%-- 
    Document   : fileUploadJSONTester
    Created on : Oct 25, 2015, 4:13:38 PM
    Author     : famous
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>FILE UPLOAD JSON TESTER</h1>
        <!--http://se-2015is203g7t4.rhcloud.com/json/bootstrap-->
        <form enctype="multipart/form-data" action="http://se-2015is203g7t4.rhcloud.com/json/location-upload" method="POST">
            Token<input type="text" name="token">
            <input type="file" name="bootstrap-file"/>
            <input type="submit"/>
        </form>
    </body>
</html>
