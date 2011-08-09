<%-- 
    Document   : index
    Created on : Nov 12, 2010, 3:57:48 PM
    Author     : Radim Hatlapatka (hata.radim@gmail.com)
--%>

<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>PdfJbIm</title>
    </head>
    <body>
        <h1>PDF re-compressor pdfJbIm: online demo</h1>

        <c:if test="${not empty error}">
            <div style="border: solid 1px red; background-color: yellow; padding: 10px">
                <c:out value="${error}"/>
            </div>
        </c:if>
        <form action="${pageContext.request.contextPath}/pdfJbIm" method="post" enctype="multipart/form-data">
            <table>
                <tr>
                    <th align="left">Input PDF: </th>
                    <td> <input type="file" name="input" value=""
                                title="upload PDF file for optimization"> </td>
                </tr>
                <tr>
                    <th align="left">Thresholding (0.5..0.9):</th>
                    <td><input type="text" name="thresh" value="" size="20dd"
                               title="sets some relevance for comparing against representant; default value is 0.85 which is safe in most cases"></td>
                </tr>
                <tr>
                    <th align="left">BW thresholding (0..255):</th>
                    <td><input type="text" name="BWthresh" value="" title="has only effect for images which are binarized" size="20dd"></td>
                </tr>
                <tr>                    
                    <th align="left">binarize:</th>
                    <td><input type="checkbox" name="binarize"
                               title="processing colored and grayscale images but in output they will become bitonal" /></td>
                </tr>
                <tr>
                    <th align="left">pdfsizeopt:</th>
                    <td><input type="checkbox" name="pdfsizeopt" title="enable if you want to use pdfsizeopt as well"/></td>
                </tr>
                <tr>
                    <th>&nbsp;</th>
                    <td></td>
                </tr>
                <tr>
                    <th></th>
                    <td><input type="Submit" value="Process" /></td>
                </tr>
            </table>

        </form>
        
    </body>
</html>
