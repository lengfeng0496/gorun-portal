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

}