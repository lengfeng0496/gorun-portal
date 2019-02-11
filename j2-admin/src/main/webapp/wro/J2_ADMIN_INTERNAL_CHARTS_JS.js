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
var TextMessages = function () {

    var service = {

        messages_en: {
            notFound: 'Message not found',
            unauthorized:
                'We\'re sorry, but you don\'t seem to have access to the web component you are requesting. Please ensure you are authorized. [Message: %s] [Status: %s]',
            serverFailure:
                'We\'re sorry, but we had trouble contacting our Portal server. Please contact support for further assistance. [Message: %s] [Status: %s]',
            serverSuccess: 'Server is up and running',
            serverFailed: 'Bad status. Status: %s',
            prefsUpdated: 'Your preferences have been updated.'
        },

        get: function (key, etc) {
            // TODO: localize
            var message = service.messages_en[key];
            if (message === undefined)
                return service.messages_en['notFound'];
            if (arguments.length <= 1)
                return message;
            var args = Array.prototype.slice.call(arguments, 1);
            args.unshift(message);
            return service.sprintf.apply(service, args);
        },

        sprintf: function(format, etc) {
            var arg = arguments;
            var i = 1;
            return format.replace(/%((%)|s)/g, function (m) { return m[2] || arg[i++] })
        }

    }

    return service;
}
;/*
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
/* jshint indent:false, unused:false */
/* jshint -W087 */
/* global appConfig:true, _:true */
'use strict';

var ServerService = function ($cookies) {

    var apiHttpConfig = {
        // override this setting with request URL parsing
        rootPath : "http://localhost:8080/jetspeed/services",
        withCredentials: false,
        withToken: false,
        headers: {
            'JETAPP-API-TOKEN': '6CB95F7FFA7B4B0ABF92216D822E4ECD',
            'JETAPP-APP-NAME' : 'j2-admin'
        }
    }

    var PATH_SEPARATOR = "/";
    var PORT_SEPARATOR = ":";

    function endsWith(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }

    function startsWith(str, prefix) {
        return str.indexOf(prefix) === 0;
    }

    function getContextPath() {
        return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
    }

    function concatenatePaths(base, path) {
        var result = "";
        if (base === null) base = "";
        if (path === null) path = "";
        result = result + base;
        if (endsWith(base, PATH_SEPARATOR)) {
            if (startsWith(path, PATH_SEPARATOR)) {
                result = result + path.substring(1);
            }
            else
                result = result + path;
        }
        else {
            if (startsWith(path, PATH_SEPARATOR) || startsWith(path, PORT_SEPARATOR))
                result = result + path;
            else {
                result = result  + PATH_SEPARATOR;
                result = result + path;
            }
        }
        return result;
    }


    return {
        api: function (endPoint, pathParam, pathParam2, pathParam3, pathParam4) {
            var url = window.location.origin + getContextPath() + '/services' + endPoint;
            if (apiHttpConfig.withToken) {
                var token = $cookies.JetAppToken;
                if (!_.isUndefined(token)) {
                    apiHttpConfig.headers['JETAPP-API-TOKEN'] = token;
                }
                var root = angular.fromJson($cookies.JetAppRestService);
                var url = "";
                if (!_.isUndefined(root)) {
                    apiHttpConfig.headers.rootPath = root;
                    url = concatenatePaths(root, endPoint);
                }
                else {
                    url = window.location.origin + '/jetspeed/services' + endPoint;
                }
            }
            if (pathParam !== undefined) {
                url += '/' + pathParam;
            }
            if (pathParam2 !== undefined) {
                url += '/' + pathParam2;
            }
            if (pathParam3 !== undefined) {
                url += '/' + pathParam3;
            }
            if (pathParam4 !== undefined) {
                url += '/' + pathParam4;
            }
            return url;
        },

        apiConfig: function () {
            return _.clone(apiHttpConfig);
        }
    }
};

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

/* Services */

var RestService = function ($http, $q, ServerService) {

    var service = {

        profiles: [],

        /**
         * List Profiles via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches profile id
         * 'title' : contains within title
         * 'concreteClass' : contains within class name
         *
         * Profiles are returned in ascending id order. All matches are
         * case insensitive.
         *
         * @param queryObject optional query object, defaults to {}
         * @param first optional first data item index, defaults to 0
         * @param count optional count to return, defaults to user preference
         * @returns promise rejected or resolved with a wrapped list of Profile objects.
         */
        listProfiles: function(queryObject, first, count) {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/profiler/list');
            var config = ServerService.apiConfig();
            config.params = {
                "type" : "json"
            };
            $http.get(url, config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        deferred.resolve(data, 404);
                    }
                    else {
                        deferred.reject(data, data.status);
                    }
                });
            return deferred.promise;
        },

        /**
         * Lookup a HostIdentity by id or host names. Matches are case insensitive.
         *
         * @param id primary key profile id to look up by
         * @returns promise rejected or resolved with a Profile object.
         */
        getProfile: function(id) {
            var deferred = $q.defer();
            var url = ServerService.api('/profiler/edit', encodeURIComponent(id));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data, status);
                })
                .error(function error(message, status) {
                    deferred.reject(message, status);
                }
            );
            return deferred.promise;
        },

        /**
         * Delete Profiles
         *
         * @param profileIds array of profile ids to delete
         * @returns promise rejected or resolved
         */
        deleteProfiles: function(profileIds) {
            var deferred = $q.defer();
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/profiler'),
                headers: {'Content-Type': 'application/json'},
                data: profileIds
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.status === 200)) {
                        deferred.resolve(undefined, status);
                    } else {
                        deferred.reject("Profiles not deleted.", status);
                    }
                })
                .error(function error(message, status) {
                    deferred.reject(message, status);
                }
            );
            return deferred.promise;
        },


        /**
         * Create or update Profile. Operation is considered a create if the
         * profile id is unique and the id is undefined.
         *
         * @param profile the Profile record to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateProfile: function(profile) {
            var deferred = $q.defer();
            var url = ServerService.api('/profiler/update');
            var config = ServerService.apiConfig();
            config.params = {
                "type" : "json"
            };
            $http.post(url, profile, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data, status);
                })
                .error(function error(message, status) {
                    deferred.reject(message, status);
                }
            );
            return deferred.promise;
        }
    };

    /**
     * Format Date into SQL string.
     *
     * @param date Date instance
     * @returns formatted SQL string
     */
    function formatTimestamp(date) {
        return date.getFullYear()+'-'+('0'+(date.getMonth()+1)).slice(-2)+'-'+('0'+date.getDate()).slice(-2)+' '+('0'+date.getHours()).slice(-2)+':'+('0'+date.getMinutes()).slice(-2)+':'+('0'+date.getSeconds()).slice(-2);
    }

    /**
     * Resolve with entity or reject with message.
     *
     * @param deferred deferred prommise
     * @param data results data
     * @param status status
     */
    function resolveWithEntityOrRejectWithMessage(deferred, data, status) {
        if (!!data && ((data.successful === 1) || (data.status == 200))) {
            var entity = ((!!data && !!data.results && data.results.length) ? data.results[0].entity : undefined);
            deferred.resolve(entity, status);
        } else {
            var message = ((!!data && !!data.results && data.results.length) ? data.results[0].message : undefined);
            deferred.reject(message, status);
        }
    }

    return service;
};

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', []).
    value('version', '0.1');
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

var PortletService = function ($http, $q, ServerService) {

    var service = {

        config: {
            withCredentials: true
        },

        lookupPreferences: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    deferred.reject(data, status);
                });
            return deferred.promise;
        },
        storePreferences: function (url, prefs) {
            var self = this, deferred = $q.defer();
            $http.post(url, prefs, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    deferred.reject(data, status);
                });
            return deferred.promise;
        }

    }
    return service;
}
;/*
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

/* Statistics Services */

var StatisticsRestService = function ($http, $q, ServerService) {

    //Needs to be unique like service?
    var service = {

        statistics: [],

        /**
         * Get JVM runtime memory info via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches profile id
         * 'title' : contains within title
         * 'concreteClass' : contains within class name
         *
         */
        memoryUsage: function () {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/statistics/memory');
            var config = ServerService.apiConfig();
            config.params = {
                "type": "json"
            };
            $http.get(url, config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        deferred.resolve(data, 404);
                    }
                    else {
                        deferred.reject(data, data.status);
                    }
                });
            return deferred.promise;
        },

        /**
         * Get JVM runtime top pages usage info via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches profile id
         * 'title' : contains within title
         * 'concreteClass' : contains within class name
         *
         */
        pageHits: function () {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/statistics/pages');
            var config = ServerService.apiConfig();
            config.params = {
                "type": "json"
            };
            $http.get(url, config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        deferred.resolve(data, status);
                    }
                    else {
                        deferred.reject(data, status);
                    }
                });
            return deferred.promise;
        },

        /**
         * Get JVM runtime top user sessions usage info via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches profile id
         * 'title' : contains within title
         * 'concreteClass' : contains within class name
         *
         */
        sessions: function () {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/statistics/users');
            var config = ServerService.apiConfig();
            config.params = {
                "type": "json"
            };
            $http.get(url, config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        deferred.resolve(data, status);
                    }
                    else {
                        deferred.reject(data, status);
                    }
                });
            return deferred.promise;
        },

        /**
         * Get JVM runtime top caches usage info via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches profile id
         * 'title' : contains within title
         * 'concreteClass' : contains within class name
         *
         */
        cacheStats: function () {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/statistics/caches');
            var config = ServerService.apiConfig();
            config.params = {
                "type": "json"
            };
            $http.get(url, config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data, status);
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        deferred.resolve(data, status);
                    }
                    else {
                        deferred.reject(data, status);
                    }
                });
            return deferred.promise;
        }
    }

    return service;

};/*
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

// Declare app level module which depends on filters, and services
angular.module('j2admin', [
    'ngRoute',
    'ngGrid',
    'ui.bootstrap',
    'ngCookies',
    'j2admin.filters',
//    'j2admin.services',
    'j2admin.directives',
    'j2admin.controllers',
    'j2admin.chartControllers'
])
    .factory('ServerService', ['$cookies', ServerService])
    .factory('TextMessages', [TextMessages])
    .factory('DataService', ['$http', '$q', 'ServerService', RestService])
    .factory('PortletService', ['$http', '$q', 'ServerService', PortletService])
    .factory('StatisticsService', ['$http', '$q', 'ServerService', StatisticsRestService])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/monitor', {templateUrl: 'views/monitor.html'});
        $routeProvider.otherwise({redirectTo: '/monitor'});
    }]);
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

/* Utility functions */
function bytesToSize(bytes)
{
    var precision = 1,
        kilobyte = 1024,
        megabyte = kilobyte * 1024,
        gigabyte = megabyte * 1024,
        terabyte = gigabyte * 1024;

    var text = "";

    if ((bytes >= 0) && (bytes < kilobyte))
    {
        text = bytes + ' B';
    }
    else if ((bytes >= kilobyte) && (bytes < megabyte))
    {
        text = (bytes / kilobyte).toFixed(precision) + ' KB';
    }
    else if ((bytes >= megabyte) && (bytes < gigabyte))
    {
        text = (bytes / megabyte).toFixed(precision) + ' MB';
    }
    else if ((bytes >= gigabyte) && (bytes < terabyte))
    {
        text = (bytes / gigabyte).toFixed(precision) + ' GB';
    }
    else if (bytes >= terabyte)
    {
        text = (bytes / terabyte).toFixed(precision) + ' TB';
    }
    else
    {
        text = bytes + ' B';
    }

    return text;
}

function addFailureAlert(TextMessages, $scope, errorMessage, status) {
    $scope.alerts.length = 0;
    var statusMsg = (status === undefined) ? "none" : status;
    var key = (status == 401) ? "unauthorized" : "serverFailure";
    $scope.alerts.push({type: 'danger', msg: TextMessages.get(key, errorMessage, statusMsg)});
};


/* Controllers: */
angular.module('j2admin.chartControllers', [])

    // -- Memory Usage Controller
    .controller('MemoryController', function ($scope, $q, StatisticsService, PortletService, TextMessages) {
        $scope.memoryUsage = [];

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };

        $scope.refresh = function() {
            $scope.init();
        };

        $scope.init = function () {

            // -- retrieve runtime memory usage
            StatisticsService.memoryUsage().then(
                function success(memory, status) {
                    $scope.memoryUsage = memory;
                    var datum = [];

                    datum.push({label: "Free", value: $scope.memoryUsage.memory.free});
                    datum.push({label: "Used", value: $scope.memoryUsage.memory.total - $scope.memoryUsage.memory.free});

                    nv.addGraph(function() {
                        var chart = nv.models.pieChart()
                            .x(function(d) { return (d.label + ": " + bytesToSize(d.value) + " (" + (d.value / $scope.memoryUsage.memory.total * 100).toFixed(1) + "%)" ) })    //Specify the data accessors.
                            .y(function(d) { return d.value })
                            .tooltips(true)
                            .donut(false)
                            .donutRatio(0.4)
                            .showLabels(false)
                            .donutLabelsOutside(true)
                            .showLegend(true)
                            .duration(500);


                        d3.select(".memory-container")
                            .datum(datum)
                            .attr("width", 960)
                            .attr("height", 500)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        var svg = d3.select("svg");
                        var donut = svg.selectAll("g.nv-pie").filter(
                            function (d, i) {
                                return i == 1;
                            });

                        donut.selectAll("text", "g").remove();
                        donut.append("text", "g")
                            .text("Total: " + bytesToSize($scope.memoryUsage.memory.total))
                            .attr("class","css-label-class")
                            .attr("text-anchor", "middle");

                        return chart;
                    })
                },
                function error(msg, status) {
                    console.log(msg.message);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        }
    }
)

    //-- Page Usage Controller
    .controller('PagesController', function ($scope, $q, StatisticsService, PortletService, TextMessages) {
        $scope.pageHits = [];

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };

        $scope.refresh = function() {
            $scope.init();
        };

        // ---- init function
        $scope.init = function () {

            // -- retrieve runtime page usage
            StatisticsService.pageHits().then(
                function success(pages, status) {
                    $scope.pageHits = pages;

                    // Process page data:
                    var datum = [{key: "Page Hits", values: []}];

                    for (var label in $scope.pageHits.pages) {
                        var index = label.lastIndexOf("/");
                        var simpleLabel = (index > -1) ? label.substring(index+1) : label;
                        index = simpleLabel.lastIndexOf(".psml");
                        simpleLabel = (index > -1) ? simpleLabel.substring(0, index) : simpleLabel;
                        datum[0].values.push({label: simpleLabel, fullLabel: label, value: $scope.pageHits.pages[label]})
                    }

                    nv.addGraph(function () {
                        var chart = nv.models.discreteBarChart()
                            .x(function (d) {
                                return d.label
                            })    //Specify the data accessors.
                            .y(function (d) {
                                return d.value
                            })
                            .tooltips(true)
                            .showValues(true)
                            .duration(500)
                            .tooltipContent(function(key, y, e, graph) {
                                for (var x in datum[0].values) {
                                    if (datum[0].values[x].label == y) {
                                        return datum[0].values[x].fullLabel;
                                    }
                                }
                                return y;
                            });


                        chart.yAxis.tickFormat(function (d) {
                            return d3.format('g')(d);
                        });
                        chart.xAxis.tickPadding(10);
                        chart.valueFormat(d3.format('g'));

                        d3.select(".pages-container")
                            .datum(datum)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    })
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        }
    }
)

    //-- User Sessions Usage Controller
    .controller('UsersController', function ($scope, $q, StatisticsService, PortletService, TextMessages) {
        $scope.sessions = [];

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };

        $scope.refresh = function() {
            $scope.init();
        };

        // -- init function
        $scope.init = function () {

            // -- retrieve top user logins
            StatisticsService.sessions().then(
                function success(users, status) {
                    $scope.sessions = users;

                    // Process user data:
                    var datum = [{key: "User Sessions", values: []}];

//                    console.log('>>>>>>> sessions: ' +JSON.stringify($scope.sessions));

                    for(var label in $scope.sessions.users) {
                        datum[0].values.push({label: label, value: $scope.sessions.users[label]});
                    }

                    nv.addGraph(function() {
                        var chart = nv.models.discreteBarChart()
                            .x(function(d) { return d.label })    //Specify the data accessors.
                            .y(function(d) { return d.value })
                            .tooltips(false)        //Don't show tooltips
                            .showValues(true)       //...instead, show the bar value right on top of each bar.
                            .duration(500);

                        chart.yAxis.tickFormat(function(d) { return d3.format('g')(d); });
                        chart.xAxis.tickPadding(10);
                        chart.valueFormat(d3.format('g'));

                        d3.select(".users-container")
                            .datum(datum)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    })
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        }
    }
)

    //-- Top Caches Usage Controller
    .controller('CachesController', function ($scope, $q, StatisticsService, PortletService, TextMessages) {
        $scope.cacheStats = [];

        $scope.alerts = [];
        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };
        $scope.addFailureAlert = function (errorMessage, status) {
            addFailureAlert(TextMessages, $scope, errorMessage, status);
        };

        $scope.refresh = function() {
            $scope.init();
        };

        // -- init function
        $scope.init = function () {

            // -- retrieve runtime page usage
            StatisticsService.cacheStats().then(
                function success(caches, status) {
                    $scope.cacheStats = caches;

                    // Process cache data:
                    var datum = [{key: "Hits", values: []}, {key: "Misses", values: []}, {key: "Evictions", values: []}];

//                    console.log('>>>>>>> cacheStats: ' +JSON.stringify($scope.cacheStats));

                    for (var cache in $scope.cacheStats.caches) {
                        datum[0].values.push({x: cache, y: $scope.cacheStats.caches[cache].hits});
                        datum[1].values.push({x: cache, y: $scope.cacheStats.caches[cache].misses});
                        datum[2].values.push({x: cache, y: $scope.cacheStats.caches[cache].evictions});
                    }

                    nv.addGraph(function () {
                        var chart = nv.models.multiBarChart()
                            .x(function (d) {
                                return d.x
                            })    //Specify the data accessors.
                            .y(function (d) {
                                return d.y
                            })
                            .tooltips(true)        //Don't show tooltips
//                            .showValues(true)      // ...instead, show the bar value right on top of each bar.
                            .duration(500);

                        chart.yAxis.tickFormat(function (d) {
                            return d3.format('g')(d);
                        });
                        chart.xAxis.tickPadding(10);
//                        chart.valueFormat(d3.format('g'));

                        d3.select(".caches-container")
                            .datum(datum)
                            .call(chart);

                        nv.utils.windowResize(chart.update);

                        return chart;
                    })
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg.message, msg.status);
                }
            )
        }
    }
);/*
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

/* Directives: */
angular.module('j2admin.directives', [])
    .directive('gwpStatusIcon', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element);

                $timeout(function() {
                    if(self.hasClass('status-yellow')) {
                        self.append('<div class="glyphicon glyphicon-warning-sign"></div>');
                    }
                });
            }
        }
    }])
    .directive('gwpStatusHover', ['DataService', function (DataService) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs) {
                var self = $(element),
                    item = scope.item,
                    body = $(document.body);

                self.hover(function() {
                        $('#infohover').remove();

                        var position = self.offset();

                        DataService.getEventsByHost(item.hostName)
                            .then(function(data) {
                                var events = data,
                                    highAlerts = [1, 2, 3], avgAlerts = [1, 2, 3], lowAlerts = [1, 2, 3];

                                for(var i = 0, iLimit = events.length; i < iLimit; i++) {
                                    var event = events[i];

                                    switch(event.monitorStatus) {
                                        case 'SCHEDULED CRITICAL':
                                        case 'SCHEDULED DOWN':
                                        case 'UNREACHABLE':
                                        case 'UNSCHEDULED DOWN':
                                        case 'UNSCHEDULED CRITICAL':
                                        case 'ACKNOWLEDGEMENT (CRITICAL)':
                                        case 'ACKNOWLEDGEMENT (DOWN)':
                                        case 'ACKNOWLEDGEMENT (UNREACHABLE)':
                                        case 'CRITICAL':
                                        case 'DOWN':
                                            highAlerts.push(event);
                                            break;

                                        case 'WARNING':
                                        case 'ACKNOWLEDGEMENT (WARNING)':
                                        case 'ACKNOWLEDGEMENT (MAINTENANCE)':
                                        case 'MAINTENANCE':
                                            avgAlerts.push(event);
                                            break;

                                        case 'UP':
                                        case 'OK':
                                        case 'ACKNOWLEDGEMENT (UP)':
                                        case 'ACKNOWLEDGEMENT (OK)':
                                        case 'UNKNOWN':
                                        case 'PENDING':
                                        case 'ACKNOWLEDGEMENT (UNKNOWN)':
                                        case 'ACKNOWLEDGEMENT (PENDING)':
                                        case 'SUSPENDED':
                                        default:
                                            lowAlerts.push(event);
                                            break;
                                    }
                                }

                                var alertHtml = '';

                                if(highAlerts.length || avgAlerts.length || lowAlerts.length) {
                                    alertHtml = '<hr />';

                                    if(highAlerts.length) {
                                        alertHtml += '<h4><img class="icon icon-alert" src="/portal-groundwork-base/app/images/status/host-red.gif" />Critical alerts (' + highAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = highAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + highAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                        if(avgAlerts.length || lowAlerts.length) {
                                            alertHtml += '<hr />';
                                        }
                                    }

                                    if(avgAlerts.length) {
                                        alertHtml += '<h4><img class="icon" src="/portal-groundwork-base/app/images/status/host-yellow.gif" />Warning alerts (' + avgAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = avgAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + avgAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                        if(lowAlerts.length) {
                                            alertHtml += '<hr />';
                                        }
                                    }

                                    if(lowAlerts.length) {
                                        alertHtml += '<h4><img class="icon" src="/portal-groundwork-base/app/images/status/host-green.gif" />Info alerts (' + lowAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = lowAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + lowAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                    }
                                }

                                $('#infohover').remove();

                                var bodyWidth = body.width();
                                var infoHover = $('<div id="infohover"><h3>' + item.hostName + '</h3><p class="timestamp">' + moment(item.lastCheckTime).format('dddd, MMMM Do YYYY, h:mm:ss a') + '</p>' + alertHtml + '</div>');
                                body.append(infoHover);

                                var left = position.left + self.outerWidth()  / 1.5;

                                if((left + infoHover.width()) > bodyWidth) {
                                    left = position.left + self.outerWidth() / 3 - infoHover.width();
                                }

                                infoHover.css({
                                    top:  position.top  + self.outerHeight() / 1.5,
                                    left: left
                                });
                            },
                            function() {
                            });
                    },
                    function() {
                        $('#infohover').remove();
                    });
                    
                    self.closest('tab-content').hover(function() {}, function() {
                        $('#infohover').remove();
                    });
            }
        }
    }])
    .directive('gwpEventHover', ['DataService', function (DataService) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element),
                    event = scope.event,
                    body = $(document.body);

                self.hover(function() {
                        $('#eventhover').remove();

                        var position = self.offset();

                        var eventHover = $('<div id="eventhover"><p>Updated ' + event.lastDate.format('dddd, MMMM Do YYYY, h:mm:ss a') + ', click to view this alert.</p><p>' + event.event.textMessage + '</p></div>');

                        body.append(eventHover);

                        eventHover.css({
                            top:  position.top  + self.outerHeight(),
                            left: position.left + 100
                        });
                    },
                    function() {
                        $('#eventhover').remove();
                    });
            }
        }
    }])
    .directive('gwpSlidingDialog', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), dialog = self.closest('.modal');
                dialog.addClass('slide-from-right');

                var baseGrid = $('#grid.base-grid');
                if(baseGrid.length) {
                    var top = baseGrid.offset().top;

                    dialog.css('margin-top', top + 'px');
                }
            }
        }
    })
    .directive('gwpAutofocus', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), dialog = self.closest('.modal');

                dialog.on('shown.bs.modal', function () {
                    setTimeout(function() {
                        element.focus();
                    },
                    100);
                });

                setTimeout(function() {
                    element.focus();
                },
                100);
            }
        }
    })
    .directive('gwpNoSpaces', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element);

                self.on('keypress', function(e) {
                    if(e.which === 32) {
                        if(e.preventDefault) {
                            e.preventDefault();
                        }

                        return false;
                    }
                });

                self.on('paste', function () {
                    setTimeout(function () {
                        var text = self.val().replace(/\s/g, "");

                        self.val(text);
                    },
                    100);
                });
            }
        }
    })
    .directive('gwpFitGrid', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var container = $(element);

                $timeout(function() {
                    var nav = container.find('.audit-nav-container'),
                        grid = container.find('.panel-grid'),
                        viewport = grid.find('.ngViewport');

                    grid.css({height: (container.height() - nav.height()) + 'px'});
                    viewport.css({height: (container.height() - nav.height() - grid.find('.ngHeaderContainer').height()) + 'px'});
                });
            }
        }
    }]);

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

/* Filters */

angular.module('j2admin.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }]);
