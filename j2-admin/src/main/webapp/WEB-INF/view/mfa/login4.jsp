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
<%@ page import="org.apache.jetspeed.security.mfa.portlets.MFALogin" %>
<%@ page import="org.apache.jetspeed.security.mfa.portlets.StatusMessage" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>

<portlet:actionURL var="restartAction" >
	<portlet:param name="phase" value="restart"/>
</portlet:actionURL>

<portlet:actionURL var="submitAction" />
<br/>
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
	<div class="portlet-msg-alert">Your account has been locked. Please contact your administrator.</div>
</tr>
</table>

<a href='${restartAction}'>Return to login</a>
