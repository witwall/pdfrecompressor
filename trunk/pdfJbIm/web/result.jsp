<%-- 
    Document   : result
    Created on : Nov 15, 2010, 5:46:10 PM
    Author     : Radim Hatlapatka (hata.radim@gmail.com)
--%>

<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Result of running PDF optimizer</title>
    </head>
    <body>
        <br />
        <c:if test="${not empty success}">
            <div style="border: solid 1px green; background-color: yellow; padding: 10px">
                <!--<a href="<c:out value='${success}'/>">Optimized PDF</a>-->
                <c:out value="${success}"/>
            </div>
        </c:if>
        <a href="./index.jsp">Optimize next document</a>
    </body>
</html>
