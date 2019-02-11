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
