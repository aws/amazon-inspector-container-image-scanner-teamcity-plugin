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
<c:set var="dockerUsername" value="<%=ScanConstants.DOCKER_USERNAME%>"/>
<c:set var="dockerPassword" value="<%=ScanConstants.DOCKER_PASSWORD%>"/>
<c:set var="sbomgenPath" value="<%=ScanConstants.SBOMGEN_PATH%>"/>

<script type="text/javascript">
    function test() {
        let checked = document.getElementById("isThresholdEnabled").checked;
        let thresholds = document.getElementById("thresholds");

        if (checked) {
            thresholds.setAttribute('style', 'display: block !important');
        } else {
            thresholds.setAttribute('style', 'display: none !important');
        }
    }
</script>

<l:settingsGroup title="Scan Settings">
    <tr>
        <th><label size="10" for="${sbomgenPath}">Path to inspector-sbomgen: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${sbomgenPath}" size="56" maxlength="100"/>
                <span class="error" id="error_${sbomgenPath}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${archivePath}">Image Id: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${archivePath}" size="56" maxlength="100"/>
                <span class="error" id="error_${archivePath}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${region}">Region: <l:star/></label></th>
        <td>
            <props:selectSectionProperty name="${region}" title="">
                <props:selectSectionPropertyContent value="us-east-1" caption="us-east-1">us-east-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-west-1" caption="eu-west-1">eu-west-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-west-1" caption="us-west-1">us-west-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-southeast-1" caption="ap-southeast-1">ap-southeast-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-northeast-1" caption="ap-northeast-1">ap-northeast-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-west-2" caption="us-west-2">us-west-2</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="sa-east-1" caption="sa-east-1">sa-east-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-southeast-2" caption="ap-southeast-2">ap-southeast-2</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-iso-east-1" caption="us-iso-east-1">us-iso-east-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-central-1" caption="eu-central-1">eu-central-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-northeast-2" caption="ap-northeast-2">ap-northeast-2</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-south-1" caption="ap-south-1">ap-south-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-east-2" caption="us-east-2">us-east-2</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ca-central-1" caption="ca-central-1">ca-central-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-west-2" caption="eu-west-2">eu-west-2</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-isob-east-1" caption="us-isob-east-1">us-isob-east-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-west-3" caption="eu-west-3">eu-west-3</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-northeast-3" caption="ap-northeast-3">ap-northeast-3</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-north-1" caption="eu-north-1">eu-north-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="ap-east-1" caption="ap-east-1">ap-east-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="me-south-1" caption="me-south-1">me-south-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-south-1" caption="eu-south-1">eu-south-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="af-south-1" caption="af-south-1">af-south-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="us-iso-west-1" caption="us-iso-west-1">us-iso-west-1</props:selectSectionPropertyContent>
                <props:selectSectionPropertyContent value="eu-central-2" caption="eu-central-2">eu-central-2</props:selectSectionPropertyContent>
            </props:selectSectionProperty>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${roleArn}">IAM Role: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${roleArn}" size="56" maxlength="100"/>
                <span class="error" id="error_${roleArn}"></span>
            </div>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="Docker Authentication - Only required if image being scanned is within a private repository">
    <tr>
        <th><label size="10" for="${dockerUsername}">Docker Username: </label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${dockerUsername}" size="56" maxlength="100"/>
                <span class="error" id="error_${dockerUsername}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${dockerPassword}">Docker Password: </label></th>
        <td>
            <div class="posRel">
                <props:passwordProperty name="${dockerPassword}" size="56" maxlength="100"/>
                <span class="error" id="error_${dockerPassword}"></span>
            </div>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="Vulnerability Thresholds">
    <tr>
        <th></th>
        <td>
            <props:checkboxProperty id="isThresholdEnabled" name="isThresholdEnabled"/>
            <label for="isThresholdEnabled">Enable Vulnerability Thresholds</label>
            <span class="smallNote">
              Specifies whether scanned vulnerabilities exceeding a value will cause a build failure.
            </span>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${countCritical}">Count Critical: <l:star/></label></th>
        <td>
            <div>
                <props:textProperty name="${countCritical}" size="10" maxlength="100" />
                <span class="error" id="error_${countCritical}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${countHigh}">Count High: <l:star/></label></th>
        <td>
            <div>
                <props:textProperty name="${countHigh}" size="10" maxlength="100"/>
                <span class="error" id="error_${countHigh}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${countMedium}">Count Medium: <l:star/></label></th>
        <td>
            <div>
                <props:textProperty name="${countMedium}" size="10" maxlength="100"/>
                <span class="error" id="error_${countMedium}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${countLow}">Count Low: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${countLow}" size="10" maxlength="100"/>
                <span class="error" id="error_${countLow}"></span>
            </div>
        </td>
    </tr>
</l:settingsGroup>