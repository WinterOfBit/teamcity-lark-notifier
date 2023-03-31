<%@ taglib prefix="ext" tagdir="/WEB-INF/tags/ext" %>
<%@ taglib prefix="et" tagdir="/WEB-INF/tags/eventTracker" %>
<%@ taglib prefix="queue" tagdir="/WEB-INF/tags/queue" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="p" tagdir="/WEB-INF/tags/p" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/notifications" %>
<%@ taglib prefix="profile" tagdir="/WEB-INF/tags/userProfile" %>
<%@ taglib prefix="ufn" uri="/WEB-INF/functions/user" %>
<%@ taglib prefix="changefn" uri="/WEB-INF/functions/change" %>
<%@ taglib prefix="intprop" uri="/WEB-INF/functions/intprop" %>
<%@ taglib prefix="problems" tagdir="/WEB-INF/tags/problems" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="larkProperties" type="cn.scutbot.teamcitylark.LarkNotifierProperties" scope="request" />
<jsp:useBean id="availableConnections"
             type="java.util.List<jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor>" scope="request"/>
<jsp:useBean id="receiverTypes"
             type="java.util.List<jetbrains.buildServer.serverSide.parameters.SelectOption>" scope="request" />
<jsp:useBean id="buildTypeId" type="java.lang.String" scope="request"/>
<jsp:useBean id="createConnectionUrl" type="java.lang.String" scope="request"/>

<tr>
    <th>
        <label for="${larkProperties.propertyConnection.key}">
            Connection: <l:star />
        </label>
    </th>
    <td>
        <c:choose>
            <c:when test="${empty availableConnections}">
                No suitable Slack connections were found. You can configure a connection in the
                <a href="${createConnectionUrl}"> parent project's settings</a>.
            </c:when>
            <c:otherwise>
                <props:selectProperty
                        name="${larkProperties.propertyConnection.key}"
                        id="${larkProperties.propertyConnection.key.replace(':', '-')}"
                        className="longField">
                    <props:option value="">-- Select Lark connection --</props:option>
                    <c:forEach var="connection" items="${availableConnections}">
                        <props:option value="${connection.id}">
                            <c:out
                                    value="${connection.connectionDisplayName}"/>
                        </props:option>
                    </c:forEach>
                </props:selectProperty>
            </c:otherwise>
        </c:choose>


    </td>
</tr>

<tr>
    <td><label for="receiverType">Receiver Type</label><l:star/> </td>
    <td>
        <props:selectProperty
                name="${larkProperties.propertyReceiverType.key}"
                id="${larkProperties.propertyReceiverType.key}"
                className="longField">
            <props:option value="">-- Select Message type --</props:option>
            <c:forEach var="option" items="${receiverTypes}">
                <props:option value="${option.value}">
                    <c:out
                            value="${option.displayName}"/>
                </props:option>
            </c:forEach>
        </props:selectProperty>
    </td>
</tr>

<tr>
    <td><label for="receiverId">Receiver ID</label><l:star/> </td>
    <td>
        <props:textProperty
                name="${larkProperties.propertyReceiverID.key}"
                id="${larkProperties.propertyReceiverID.key}"
                className="longField" />
    </td>
</tr>

<tr>
    <td><label for="overriddenParams">Override Params</label></td>
    <td>
        <props:textProperty
                name="${larkProperties.propertyOverriddenParams.key}"
                id="${larkProperties.propertyOverriddenParams.key}"
                className="longField" />
    </td>
</tr>

<tr>
    <td><label for="buildSuccessMsgTemplate">Build Success Template ID</label><l:star/> </td>
    <td>
        <props:textProperty
                name="${larkProperties.propertyMessageBuildSuccessTemplateId.key}"
                id="${larkProperties.propertyMessageBuildSuccessTemplateId.key}"
                className="longField" />
    </td>
</tr>

<tr>
    <td><label for="buildStartMsgTemplate">Build Start Template ID</label><l:star/> </td>
    <td>
        <props:textProperty
                name="${larkProperties.propertyMessageBuildStartTemplateId.key}"
                id="${larkProperties.propertyMessageBuildStartTemplateId.key}"
                className="longField" />
    </td>
</tr>

<tr>
    <td><label for="buildFailMsgTemplate">Build Fail Template ID</label><l:star/> </td>
    <td>
        <props:textProperty
                name="${larkProperties.propertyMessageBuildFailTemplateId.key}"
                id="${larkProperties.propertyMessageBuildFailTemplateId.key}"
                className="longField" />
    </td>
</tr>