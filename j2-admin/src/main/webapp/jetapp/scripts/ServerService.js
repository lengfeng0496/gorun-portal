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

