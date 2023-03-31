<%@ page import="jetbrains.buildServer.web.util.WebUtil" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="intprop" uri="/WEB-INF/functions/intprop"%>

<jsp:useBean id="larkProperties" type="cn.scutbot.teamcitylark.LarkNotifierProperties" scope="request" />
<c:set var="currentRootUrl" value="${WebUtil.getRootUrl(pageContext.request)}"/>

<bs:linkScript>
    /js/bs/forms.js
    /js/bs/editBuildType.js
</bs:linkScript>

<tr>
    <td><label for="displayName">DisplayName: </label><l:star/> </td>
    <td>
        <props:textProperty name="displayName" className="longField" />
    </td>
</tr>

<tr>
    <td><label for="appId">App ID: </label><l:star/> </td>
    <td>
        <props:textProperty name="appId" className="longField" />
    </td>
</tr>

<tr>
    <td><label for="secure:appSecret">App Secret: </label><l:star/> </td>
    <td>
        <props:textProperty name="secure:appSecret" className="longField" />
    </td>
</tr>