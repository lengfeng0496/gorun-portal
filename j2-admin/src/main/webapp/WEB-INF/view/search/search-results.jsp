<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

--%>
<%@page contentType="text/html" %>
<%@page import="java.util.List"%>
<%@page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.search.resources.SearchResources" />

<form id='jsSearchList' method="POST" action='<portlet:actionURL/>'>
    <table width='100%'>
        <tr>
            <th class="portlet-section-subheader"><fmt:message key="search.label.title"/></th>
            <th class="portlet-section-subheader"><fmt:message key="search.label.type"/></th>
            <th class="portlet-section-subheader"><fmt:message key="search.label.name"/></th>
            <th class="portlet-section-subheader"><fmt:message key="search.label.score"/></th>
            <th class="portlet-section-subheader"><fmt:message key="search.label.description"/></th>
        </tr>
        <c:forEach var="item" items="${SEARCH_RESULTS}" varStatus="status">
            <c:choose>
                <c:when test="${status.index % 2 == 0}">
                    <c:set var="styleClass" value="'portlet-section-alternate'" />
                </c:when>
                <c:otherwise>
                    <c:set var="styleClass" value="'portlet-section-body'" />
                </c:otherwise>
            </c:choose>
            <tr>
                <td class="${styleClass}">
                    <a href="${item.link}">${item.title}</a>
                </td>
                <td class="${styleClass}">${item.type}</td>
                <td class="${styleClass}">${item.key}</td>
                <td class="${styleClass}">${item.score}</td>
                <td class="${styleClass}">${item.description}</td>
            </tr>
        </c:forEach>
        <tr>
            <th class="portlet-section-header" colspan="5"></th>
        </tr>
    </table>
</form>

