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
)