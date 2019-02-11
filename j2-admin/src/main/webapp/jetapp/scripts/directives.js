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

