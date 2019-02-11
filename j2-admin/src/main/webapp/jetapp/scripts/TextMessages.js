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
