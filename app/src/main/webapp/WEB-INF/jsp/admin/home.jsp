<%-- 
    Document   : bootstrap
    Created on : Sep 24, 2015, 6:15:08 PM
    Author     : r128
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
       
        <script>
            $(document).on('change', '.btn-file :file', function () {
                var input = $(this),
                        numFiles = input.get(0).files ? input.get(0).files.length : 1,
                        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                input.trigger('fileselect', [numFiles, label]);
            });

            $(document).ready(function () {
                $('.btn-file :file').on('fileselect', function (event, numFiles, label) {

                    var input = $(this).parents('.input-group').find(':text'),
                            log = numFiles > 1 ? numFiles + ' files selected' : label;

                    if (input.length) {
                        input.val(log);
                    } else {
                        if (log)
                            alert(log);
                    }

                });
            });
            
            
        </script>
    </head>
    <body>
        <%@include file="../navbar.jsp" %>
        <div class="container-fluid">
            <div class="row text-center">
                <div class="col-lg-6"><h1>Bootstrap</h1></div>
                <div class="col-lg-6"><h1>Add Data</h1></div>
            </div>

            <div class="row">
                <form enctype="multipart/form-data" action="/bootstrap" method="post">
                    <div class="col-lg-6">
                        <div class="input-group">
                            <span class="input-group-btn">
                                <span class="btn btn-default btn-file">
                                    Add File<input name="fileToUpload" type="file">
                                </span>
                            </span>
                            <input class="form-control" readonly>
                            <span class="input-group-btn">
                                <button type="submit" value ="submit" class="btn btn-success">
                                    <span class="glyphicon glyphicon-upload"></span>
                                </button>
                            </span>
                        </div>
                    </div>
                </form>
                <form enctype="multipart/form-data" action="/addData" method="post" class="role">
                    <div class="col-lg-6">
                        <div class="input-group">
                            <span class="input-group-btn">
                                <span class="btn btn-default btn-file">
                                    Add File<input name="fileToUpload" type="file">
                                </span>
                            </span>
                            <input class="form-control" readonly>
                            <span class="input-group-btn">
                                <button type="submit" value = ""class="btn btn-success">
                                    <span class="glyphicon glyphicon-upload"></span>
                                </button>
                            </span>
                        </div> 
                    </div>
                </form>
            </div>
            <%                
                BootstrapError bootstrapError = (BootstrapError) request.getAttribute("bootstrapError");
                String type = (String) request.getAttribute("type");
                String status = (String) request.getAttribute("status");
                TreeMap<String, Integer> numRecordLoaded = null;
                ArrayList<ErrorMsg> errors = null;
                
                if (bootstrapError != null) {
                    numRecordLoaded = bootstrapError.getNumRecordLoaded();
                    errors = bootstrapError.getErrors();
                }

                

            %>
            <br/>
            
            
                <div class="col-lg-12">
                    
                    
                        
                    
                    <%
                        if(type != null) {
                            out.println("<h3><b>"+type+"</b></h3>");
                        }
                        if (status != null) {
                            out.println("Status : " + status + "<br/><br/>");
                        }
                        
                        
                        if (numRecordLoaded != null) {
                            
                            boolean deleteData = false;
                            
                            if(numRecordLoaded.containsKey("validDelete")) {
                                deleteData = true;
                            }
                    %>
                    
                    <table>

                        <tr>
                            <th>
                                Records Loaded
                            </th>
                            <%
                            if(deleteData) {
                            %>
                            <th>
                                Records Deletion
                            </th>
                            <%
                            }
                            %>
                        </tr>

                    <%
                            
                            int count = 0;
                            Iterator<String> numRecordLoadedIter = numRecordLoaded.keySet().iterator();
                            while (numRecordLoadedIter.hasNext()) {
                                String fileName = numRecordLoadedIter.next();
                                
                                if(type.equals("Bootstrap")) {
                                    if(!fileName.equals("invalidDelete") && !fileName.equals("validDelete")) {
                                        out.println("<tr>");
                                        out.println("<td width='300px;'>"+fileName + " : " + numRecordLoaded.get(fileName) +"</td>");
                                        if(deleteData) {
                                            if(count == 0) {
                                                out.println("<td> Invalid Delete Records: " + numRecordLoaded.get("invalidDelete") +"</td>");
                                                count++;
                                            }else if(count == 1) {
                                                out.println("<td> Valid Delete Records: " + numRecordLoaded.get("validDelete") +"</td>");
                                                count++;
                                            }
                                            out.println("</tr>");
                                        }
                                    }
                                }else {
                                    
                                    if(numRecordLoaded.containsKey("invalidDelete") && numRecordLoaded.size() == 2) {
                                        out.println("<tr>");
                                        out.println("<td width='300px;'></td>");
                                        if(deleteData) {
                                            if(count == 0) {
                                                out.println("<td> Invalid Delete Records: " + numRecordLoaded.get("invalidDelete") +"</td>");
                                                count++;
                                            }else if(count == 1) {
                                                out.println("<td> Valid Delete Records: " + numRecordLoaded.get("validDelete") +"</td>");
                                                count++;
                                            }
                                            out.println("</tr>");
                                        }
                                    }else {
                                        if(!fileName.equals("invalidDelete") && !fileName.equals("validDelete")) {
                                            out.println("<tr>");
                                            out.println("<td width='300px;'>"+fileName + " : " + numRecordLoaded.get(fileName) +"</td>");
                                            if(deleteData) {
                                                if(count == 0) {
                                                    out.println("<td> Invalid Delete Records: " + numRecordLoaded.get("invalidDelete") +"</td>");
                                                    count++;
                                                }else if(count == 1) {
                                                    out.println("<td> Valid Delete Records: " + numRecordLoaded.get("validDelete") +"</td>");
                                                    count++;
                                                }
                                                out.println("</tr>");
                                            }
                                            if(deleteData && numRecordLoaded.size() == 3) {
                                                out.println("<tr>");
                                                out.println("<td width='300px'></td>");
                                                out.println("<td> Invalid Delete Records: " + numRecordLoaded.get("invalidDelete") +"</td>");
                                                out.println("</tr>");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        
                    %>
                    </table>
                    
                    <br/>
                    <br/>
                   
                      
                        <%
                            
                        %>
                        
                    
                        
                        <%
                            String invalidExtension = (String) request.getAttribute("invalidExtension");
                                if (invalidExtension != null) {
                                    out.println(invalidExtension);
                                }
                        %>

                        <table class="table table-fixedheader" style="width:1203px">
                            <thead>
                                 <tr>
                                    <th width="300px">
                                        File
                                    </th>
                                    <th width="150px">
                                        Line Number
                                    </th>
                                    <th width="750px">
                                       Error
                                    </th>
                                </tr>
                            </thead>
                            <tbody style="height:350px;" >
                               
                                <%
                                    if (errors != null) {
                                        for (ErrorMsg errorMsg : errors) {
                                            String[] msgs = errorMsg.getMessage();
                                            String concat = "";
                                            out.println("<tr>");
                                            out.println("<td width='300px'>" + errorMsg.getFile() + "</td>");
                                            out.println("<td width='150px'>" + errorMsg.getLine() + "</td>");
                                            for (String msgPart : msgs) {
                                                concat += msgPart + ",";
                                            }
                                            out.println("<td width='735px'>" + concat.substring(0, concat.length() - 1) + "</td>");
                                            out.println("</tr>");
                                        }
                                        
                                        
                                    }  
                                %>
                                
                                


                            </tbody>

                        </table>
                   
                    

                </div>
            </div>
            
         
     <%@include file="../footer.jsp" %>                            
    </body>
   
</html>

