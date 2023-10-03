<%@ page import="com.amazon.inspector.teamcity.ScanConstants" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<c:set var="archivePath" value="<%=ScanConstants.ARCHIVE_PATH%>"/>
<c:set var="roleArn" value="<%=ScanConstants.ROLE_ARN%>"/>
<c:set var="region" value="<%=ScanConstants.REGION%>"/>
<c:set var="countCritical" value="<%=ScanConstants.COUNT_CRITICAL%>"/>
<c:set var="countHigh" value="<%=ScanConstants.COUNT_HIGH%>"/>
<c:set var="countMedium" value="<%=ScanConstants.COUNT_MEDIUM%>"/>
<c:set var="countLow" value="<%=ScanConstants.COUNT_LOW%>"/>

<l:settingsGroup title="My Runner settings">
    <tr>
        <th><label size="10" for="${archivePath}">Archive Path: <l:star/></label></th>
        <td>6
            <div class="posRel">
                <props:textProperty name="${archivePath}" size="56" maxlength="100"/>
                <span class="error" id="error_${archivePath}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${roleArn}">Role Arn: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${roleArn}" size="56" maxlength="100"/>
                <span class="error" id="error_${roleArn}"></span>
            </div>
        </td>
    </tr>
    <th><label size="10" for="${region}">Region: <l:star/></label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${region}" size="56" maxlength="100"/>
            <span class="error" id="error_${region}"></span>
        </div>
    </td>
    </tr>
    <tr>
        <th><label size="10" for="${countCritical}">Count Critical: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${countCritical}" size="10" maxlength="100"/>
                <span class="error" id="error_${countCritical}"></span>
            </div>
        </td>
        <th><label size="10" for="${countHigh}">Count High: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${countHigh}" size="10" maxlength="100"/>
                <span class="error" id="error_${countHigh}"></span>
            </div>
        </td>
        <th><label size="10" for="${countMedium}">Count Medium: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${countMedium}" size="10" maxlength="100"/>
                <span class="error" id="error_${countMedium}"></span>
            </div>
        </td>
        <th><label size="10" for="${countLow}">Count Low: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${countLow}" size="10" maxlength="100"/>
                <span class="error" id="error_${countLow}"></span>
            </div>
        </td>
    </tr>
</l:settingsGroup>