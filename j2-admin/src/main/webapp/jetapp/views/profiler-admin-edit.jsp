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
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<c:set var="rows" value="${renderRequest.getPreferences().getValue('rowsPerPage', '20')}"/>

<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />
<portlet:resourceURL var="writePrefs" id="writePrefs" escapeXml="false" />
<portlet:renderURL var="renderURL" escapeXml="false" windowState="normal" portletMode="view" />

<div class="panel panel-primary">
    <div class="panel-heading">Profile Preferences</div>
    <div ng-controller="ProfilerEditController" class='panel-body'
         ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>',
                       '<%=renderResponse.encodeURL(writePrefs.toString())%>', '<%=renderResponse.encodeURL(renderURL.toString())%>')">
        <form name="profileEditForm" class="form-horizontal app-form" novalidate>

            <div class="form-group">
                <label for="rowsPerPage" class="col-sm-2 control-label">Rows Per Page</label>
                <div class="col-sm-10">
                    <input required type="number" min="5" max="200" class="form-control" id="rowsPerPage" name='rowsPerPage' ng-model='prefs.rowsPerPage' placeholder="Enter rows range: 5..200" tabindex='1'>
                </div>
            </div>

            <%--<button  class="btn btn-primary" ng-click="update(prefs)" ng-disabled="profileEditForm.$invalid || isUnchanged(prefs)" tabindex='6'>Submit</button>--%>
            <button  class="btn btn-primary" ng-click="update(prefs)" ng-disabled="!prefs.rowsPerPage" tabindex='6'>Submit</button>
            <alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.msg}}</alert>
            <pre ng-bind =" contact | json" ng-hide="!debug"> </pre>

        </form>
    </div>
</div>

