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
<c:set var="sbomgenSelection" value="<%=ScanConstants.SBOMGEN_SELECTION%>"/>
<c:set var="awsAccessKeyId" value="<%=ScanConstants.AWS_ACCESS_KEY_ID%>"/>
<c:set var="awsSecretKey" value="<%=ScanConstants.AWS_SECRET_KEY%>"/>
<c:set var="awsProfileName" value="<%=ScanConstants.AWS_PROFILE_NAME%>"/>

<l:settingsGroup title="Inspector-sbomgen Installation Method">
    <l:settingsGroup title="Automatic - Select OS and CPU arch">
        <tr>
            <th>
                <props:radioButtonProperty name="${sbomgenSelection}" value="linuxAmd64"/>
                <label size="10" for="${sbomgenSelection}">Linux, AMD64</label>
            </th>
            <td></td>
        </tr>
        <tr>
            <th>
                <props:radioButtonProperty name="${sbomgenSelection}" value="linuxArm64"/>
                <label size="10" for="${sbomgenSelection}">Linux, ARM64</label>
            </th>
            <td></td>
        </tr>
    </l:settingsGroup>
    <l:settingsGroup title="Manual">
        <tr>
            <th>
                <props:radioButtonProperty name="${sbomgenSelection}" value="manual"/>
                <label size="10" for="${sbomgenSelection}">Manual</label>
            </th>
            <td>
                <div class="posRel">
                    <props:textProperty name="${sbomgenPath}" size="56" maxlength="100"/>
                    <span class="error" id="error_${sbomgenPath}"></span>
                </div>
            </td>
        </tr>
    </l:settingsGroup>
</l:settingsGroup>

<l:settingsGroup title="Scan Settings">
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
        <td>
            <props:selectSectionProperty name="${region}" title="Region: ">
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
</l:settingsGroup>
<l:settingsGroup title="Docker Authentication - Optional, required if image being scanned is within a private repository">
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
<l:settingsGroup title="AWS Authentication - At least one option is required">
    <tr>
        <th><label size="10" for="${awsAccessKeyId}">Credentials, AWS Access Key ID: </label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${awsAccessKeyId}" size="56" maxlength="100"/>
                <span class="error" id="error_${awsAccessKeyId}"></span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label size="10" for="${awsSecretKey}">Credentials, AWS Secret Key: </label></th>
        <td>
            <div class="posRel">
                <props:passwordProperty name="${awsSecretKey}" size="56" maxlength="100"/>
                <span class="error" id="error_${awsSecretKey}"></span>
            </div>
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
    <tr>
        <th><label size="10" for="${awsProfileName}">AWS Profile Name: </label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${awsProfileName}" size="56" maxlength="100" value="default"/>
                <span class="error" id="error_${awsProfileName}"></span>
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
        <th></th>
        <td>
            <props:checkboxProperty id="isThresholdEqual" name="isThresholdEqual"/>
            <label for="isThresholdEqual">Enable Equal Threshold</label>
            <span class="smallNote">
              Specifies whether build will only pass if vulnerabilities equal threshholds.
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