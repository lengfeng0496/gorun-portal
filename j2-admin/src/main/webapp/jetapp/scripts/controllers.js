/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

function addFailureAlert(TextMessages, $scope, errorMessage, status) {
    $scope.alerts.length = 0;
    var statusMsg = (status === undefined) ? "none" : status;
    var key = (status == 401) ? "unauthorized" : "serverFailure";
    $scope.alerts.push({type: 'danger', msg: TextMessages.get(key, errorMessage, statusMsg)});
};

/* Controllers: */
angular.module('j2admin.controllers', [])
    .controller('ProfilerController', function ($scope, $q, $interval, $log, $modal, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {
                field: 'id', displayName: 'ID', width: '20%',
                cellTemplate: '<div ng-click="editProfile(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'
            },
            {
                field: 'title', displayName: 'Profile Name', width: '50%',
                cellTemplate: '<div ng-click="editProfile(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'
            },
            {
                field: 'concreteClass', displayName: 'Class', width: '30%',
                cellTemplate: '<div ng-click="editProfile(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'
            }
        ];

        $scope.filters = {
            term: ''
        };

        var searchTimeout = null;
        $scope.perPage = 10;
        $scope.page = 0;
        $scope.maxPage = 0;
        $scope.currentIndex = -1;

        $scope.options = {
            showSelectionCheckbox: true,
            selectWithCheckboxOnly: true,
            selectedItems: [],
            data: 'profiles',
            columnDefs: 'columnDefs',
            plugins: [new ngGridFlexibleHeightPlugin()]
        };
        $scope.initialized = false;

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };

        // ---- init function
        $scope.init = function (readResourceURL) {

            $scope.readResourceURL = readResourceURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    $scope.perPage = prefs.rowsPerPage;
                    // $scope.columnDefs[1].displayName = (!!~prefs.service.indexOf('cpu')) ? "CPU %" : "Memory %";
                    $scope.getData();

                    if ($scope.initialized == false) {
                        //$interval(refresh, $scope.prefs.refreshSeconds * 1000);
                    }

                    $scope.initialized = true;
                },
                function error(msg, status) {
                    console.log(msg.message);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            );
        }; // end init

        // -- retrieve a page of profiles
        $scope.getData = function () {
            var filters = {};

            if ($scope.filters.term.length) {
                filters.id = $scope.filters.term;
                filters.title = $scope.filters.term;
                filters.concreteClass = $scope.filters.term;
            }

            DataService.listProfiles(filters, $scope.page * $scope.perPage, $scope.perPage).then(
                function success(profiles, status) {
                    $scope.profilesAll = profiles.records;
                    $scope.maxPage = Math.floor($scope.profilesAll.length / $scope.perPage);
                    $scope.getPage();
                },
                function error(msg, status) {
                    console.log(msg.message);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        }; // end getPage

        $scope.getPage = function () {
            var intermediary = _.filter($scope.profilesAll, function(profile) {
                return ((profile.id.indexOf($scope.filters.term) !== -1) ||
                (profile.title.indexOf($scope.filters.term) !== -1) ||
                (profile.concreteClass.indexOf($scope.filters.term) !== -1))
            });

            $scope.profiles = intermediary.slice($scope.page * $scope.perPage, ($scope.page + 1) * $scope.perPage);

            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        // -- get previous page
        $scope.getNextPage = function () {
            $scope.page++;

            $scope.getPage();
        };

        // -- get next page
        $scope.getPrevPage = function () {
            $scope.page--;

            if($scope.page < 0) {
                $scope.page = 0;
            }

            $scope.getPage();
        };

        // -- perform search
        $scope.search = function() {
            if(searchTimeout) {
                clearTimeout(searchTimeout);
            }

            searchTimeout = setTimeout(function() {
                    $scope.page = 0;
                    $scope.getPage();
                },
                500);
        };

        var activeDialog = null;

        // -- add or edit profile
        $scope.editProfile = function(index) {
            $scope.currentIndex = index;

            if(activeDialog) {
                return;
            }

            var modalInstance = $modal.open({
                // TODO: get context
                templateUrl: '/j2-admin/jetapp/views/modals/profile-detail.html',
                controller: ProfileDetailsInstanceController,
                backdrop: false,
                resolve: {
                    DataService: function() { return DataService; },
                    profile: function() { return (typeof index === 'undefined') ? null : $scope.profiles[index]; },
                    parentScope: function() { return $scope; },
                    existing: function() { return (typeof index !== 'undefined'); },
                    existingIds: function() { return _.pluck($scope.profilesAll, "id"); }
                }
            });

            activeDialog = modalInstance;

            modalInstance.result.then(function () {
                activeDialog = null;
                $scope.getData();
            }, function () {
                activeDialog = null;
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.deleteHosts = function() {
            var modalInstance = $modal.open({
                // TODO: get context
                templateUrl: '/j2-admin/jetapp/views/modals/profiles-delete.html',
                controller: DeleteProfilesInstanceController,
                resolve: {
                    DataService: function() { return DataService; },
                    selectedItems: function() { return $scope.options.selectedItems; }
                }
            });

            modalInstance.result.then(function () {
                $scope.options.selectedItems = [];
                $scope.getData();
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };
    }
)
    .controller('ProfilerEditController', function ($scope, DataService, PortletService, TextMessages) {
        $scope.master = {};

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };
        $scope.addSuccessAlert = function () {
            $scope.alerts.length = 0;
            $scope.alerts.push({type: 'success', msg: TextMessages.get('prefsUpdated') });
        };

        $scope.init = function (readResourceURL, writeResourceURL, renderURL) {
            $scope.readResourceURL = readResourceURL;
            $scope.writeResourceURL = writeResourceURL;
            $scope.renderURL = renderURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    $scope.master = angular.copy(prefs);
                    //$scope.profileEditForm.$setPristine();
                },
                function error(msg, status) {
                    console.log(msg.message);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        };

        $scope.update = function (prefs) {
            PortletService.storePreferences($scope.writeResourceURL, prefs).then(
                function success(result, status) {
                    //window.location = $scope.renderURL;
                    $scope.addSuccessAlert();
                },
                function error(msg, status) {
                    console.log(msg.message);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        };

        $scope.reset = function () {
            $scope.prefs = angular.copy($scope.master);
        };

        $scope.clear = function () {
            $scope.master = {};
            $scope.prefs = {};
            $scope.profileEditForm.$setPristine();
        };

        $scope.isUnchanged = function (prefs) {
            return angular.equals(prefs, $scope.master);
        };

        $scope.reset();
    });

var CriteriaDetailsInstanceController = function ($scope, $modalInstance, criteria) {

    $scope.criteria = criteria || {
        name: '',
        value: '',
        resolverType: '',
        fallback: -1,
        order: 0
    };

    $scope.add = function() {
        $modalInstance.close($scope.criteria);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};
CriteriaDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'criteria'];

/**
 * Profile Instance Controller for modal dialog of editing of profile form
 *
 * @param $scope
 * @param $modalInstance
 * @param DataService
 * @param profile
 * @param parentScope
 * @constructor
 */
var ProfileDetailsInstanceController = function ($scope, $modal, $modalInstance, $log, DataService, profile, parentScope, existing, existingIds) {

    $scope.profile = profile || {
        id: '',
        title: '',
        concreteClass: '',
        criteria: []
    };

    $scope.existing = existing;
    $scope.existingIds = existingIds;

    if (profile != null) {
        DataService.getProfile(profile.id).then(
            function success(result) {
                $scope.profile = result;
            },
            function error(msg) {
                console.log(msg.message);
                $scope.addFailureAlert(msg.message, msg.status);
            });
    }

    parentScope.$watch('currentIndex', function(newIndex) {
        if(typeof(newIndex) != 'undefined') {
            $scope.profile = parentScope.profiles[newIndex];
        }
    });

    $scope.isUniqueId = function() {
        return ($scope.existingIds.indexOf($scope.profile.id) === -1);
    };

    $scope.isValidTitle = function() {
        var hostNameRegEx = /^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9]))*$/;
        return true;
        //return ipaddr.isValid($scope.hostIdentity.hostName) || hostNameRegEx.test($scope.hostIdentity.hostName);
    };

    $scope.alerts = [];
    $scope.addFailureAlert = function (errorMessage, status) {
        addFailureAlert(TextMessages, $scope, errorMessage, status);
    };
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.hasAlias = function(alias) {
        return ($scope.hostIdentity.hostNames.indexOf(alias) !== -1);
    };

    $scope.add = function() {

        DataService.createOrUpdateProfile($scope.profile).then(
            function success(entity) {
                $modalInstance.close();
            },
            function error(msg) {
                var message = msg.message;
                if(message.indexOf('ConstraintViolationException') != -1) {
                    message = 'This profile is likely to exist already - please choose another name.';
                }
                console.log(message);
                $scope.addFailureAlert(msg.message, msg.status);
            });
    };

    $scope.addCriteria = function(index) {
        var modalInstance = $modal.open({
            // TODO: get context
            templateUrl: '/j2-admin/jetapp/views/modals/criteria-detail.html',
            controller: CriteriaDetailsInstanceController,
            backdrop: false,
            resolve: {
                criteria: function() { return $scope.profile.criteria[index] || {}; }
            }
        });

        modalInstance.result.then(function (retCriteria) {
            if(typeof index !== 'undefined') {
                $scope.profile.criteria[index] = retCriteria;
            }
            else {
                $scope.profile.criteria.push(retCriteria);
            }
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });
    };

    $scope.removeCriteria = function(index) {
        $scope.profile.criteria.splice(index, 1);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

};
ProfileDetailsInstanceController.$inject = ['$scope', '$modal', '$modalInstance', '$log', 'DataService', 'profile', 'parentScope', 'existing', 'existingIds'];

var DeleteProfilesInstanceController = function ($scope, $modalInstance, DataService, selectedItems) {

    $scope.alerts = [];
    $scope.addFailureAlert = function (errorMessage, status) {
        addFailureAlert(TextMessages, $scope, errorMessage, status);
    };
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.deleteItems = function() {
        var ids = [];

        for(var i = 0, iLimit = selectedItems.length; i < iLimit; i++) {
            ids.push(selectedItems[i].id);
        }

        DataService.deleteProfiles(ids).then(
            function success(data) {
                $modalInstance.close();
            },
            function failures(msg, status) {
                $scope.addFailureAlert(msg.message, msg.status);
            });
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};
DeleteProfilesInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'selectedItems'];

