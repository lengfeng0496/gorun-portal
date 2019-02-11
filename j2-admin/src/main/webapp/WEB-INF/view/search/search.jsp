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
<%@page import="java.util.List"%>
<%@page import="org.apache.jetspeed.page.document.Node"%>
<%@page import="org.apache.jetspeed.portlets.spaces.BreadcrumbMenu.BreadcrumbMenuItem" %>
<%@ page contentType="text/html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:if test="${renderRequest.getUserPrincipal() != null}">
<script type="text/javascript" language="javascript">
    function submitJetspeedSearch(event) {
        if (event.which == 13 || event.keyCode == 13) {
            document.getElementById("<portlet:namespace/>-jetspeedSearchForm").submit();
            return false;
        }
        return true;
    };
</script>

<form id='<portlet:namespace/>=-jetspeedSearchForm' method="POST" action="<portlet:actionURL/>">
    <input id="jsSearch" name='jsSearch' type="search" style='font-size: 8pt' placeholder='Search' onkeypress="return submitJetspeedSearch(event)" />
</form>
</c:if>