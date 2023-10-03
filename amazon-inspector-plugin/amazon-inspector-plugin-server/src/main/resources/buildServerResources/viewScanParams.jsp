<%@ page import="com.netsparker.teamcity.ScanConstants" %>
<%@ page import="com.amazon.inspector.teamcity.ScanConstants" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<c:set var="archivePath" value="<%=ScanConstants.ARCHIVE_PATH%>"/>
<c:set var="roleArn" value="<%=ScanConstants.ROLE_ARN%>"/>
<c:set var="region" value="<%=ScanConstants.REGION%>"/>
<c:set var="countCritical" value="<%=ScanConstants.COUNT_CRITICAL%>"/>
<c:set var="countHigh" value="<%=ScanConstants.COUNT_HIGH%>"/>
<c:set var="countMedium" value="<%=ScanConstants.COUNT_MEDIUM%>"/>
<c:set var="countLow" value="<%=ScanConstants.COUNT_LOW%>"/>

<div class="parameter">
    Message: <props:displayValue name="${messageId}" emptyValue=""/>
</div>