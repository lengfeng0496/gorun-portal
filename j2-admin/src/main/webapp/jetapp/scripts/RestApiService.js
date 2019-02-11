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
