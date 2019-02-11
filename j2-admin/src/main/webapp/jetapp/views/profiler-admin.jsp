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
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div ng-controller="ProfilerController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="form-group">
        <div class="col-md-10 col-sm-10 padding-left0">
            <alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
        <br />
    </div>

    <div class="row no-margin">

        <div class="col-sm-9 col-md-9 col-lg-9">
            <div class="form-group">
                <div class="col-sm-6 col-md-6 col-lg-6 no-padding">
                    <input type="text" class="form-control" id="host-identities-search" placeholder="Filter Profiles" ng-model="filters.term" ng-keyup="search()" />
                </div>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-click="editProfile()">
                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-disabled="!options.selectedItems.length" ng-click="deleteHosts()">
                    <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-disabled="!options.selectedItems.length">
                    <span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span>
                </button>
            </div>
        </div>

        <div class="col-sm-3 col-md-3 col-lg-3 text-right">
            <button id="audit-next" type="button" class="btn btn-default" ng-click="getPrevPage()" ng-disabled="!page">
                <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
            </button>
            &nbsp;&nbsp;
            <button type="button" class="btn btn-default" ng-click="getNextPage()" ng-disabled="(profilesAll.length <= perPage) || (maxPage == page)">
                <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
            </button>
        </div>
    </div>

    <div class="no-padding">
        <div id="grid" ng-grid="options"></div>
    </div>
</div>
