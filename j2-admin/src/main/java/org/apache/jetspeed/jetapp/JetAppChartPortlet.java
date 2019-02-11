/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jetspeed.jetapp;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class JetAppChartPortlet extends JetAppPortlet {

    protected static final String J2_ADMIN_CHARTS_CSS_ID = "j2admin_charts_css";
    protected static final String J2_ADMIN_INTERNAL_CHARTS_SCRIPT_ID = "j2admin_charts_js";

    // use merged and minified resources when releasing
    public static final boolean DEV_MODE = false;

    protected static String[][] CHART_PRODUCTION_STYLES = {
            {"/wro/J2_ADMIN_CHARTS_CSS.css", J2_ADMIN_CHARTS_CSS_ID},
    };

    protected static String[][] CHART_DEV_STYLES = {
            {"/jetapp/charts/styles/nv.d3.min.css", "chart_nvd3"},
            {"/jetapp/charts/styles/styles.css", "chart_styles"}
    };

    protected static String[][] CHART_PRODUCTION_SCRIPTS = {
            {"/wro/J2_ADMIN_EXTERNAL_JS.js", J2_ADMIN_EXTERNAL_SCRIPT_ID},
            {"/wro/J2_ADMIN_INTERNAL_CHARTS_JS.js", J2_ADMIN_INTERNAL_CHARTS_SCRIPT_ID},
            { "/jetapp/charts/scripts/d3.min.js", "chart_d3"},
            { "/jetapp/charts/scripts/nv.d3.min.js", "chart_nvd3js"}
    };

    protected static String[][] CHART_DEV_SCRIPTS = {
//            { "/jetapp/charts/scripts/jquery-2.1.3.min.js", "chart_jquery"},
            { "/wro/J2_ADMIN_EXTERNAL_JS.js", J2_ADMIN_EXTERNAL_SCRIPT_ID},
            { "/jetapp/scripts/TextMessages.js", "j2admin_text"},
            { "/jetapp/scripts/ServerService.js", "j2admin_server"},
            { "/jetapp/scripts/RestApiService.js", "j2admin_services"},
            { "/jetapp/scripts/PortletService.js", "j2admin_portlet"},
            { "/jetapp/scripts/StatisticsRestServices.js", "j2admin_rest_stats"},
            { "/jetapp/app.js", "j2admin_app"},
            { "/jetapp/scripts/controllers.js", "j2admin_controllers"},
            { "/jetapp/scripts/chartControllers.js", "j2admin_chart_controllers"},
            { "/jetapp/scripts/directives.js", "j2admin_directives"},
            { "/jetapp/scripts/filters.js", "j2admin_filters"},
            { "/jetapp/charts/scripts/d3.min.js", "chart_d3"},
            { "/jetapp/charts/scripts/nv.d3.min.js", "chart_nvd3js"}
    };

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
        //super.doHeaders(request, response);

        if (alreadyContributedHeaders(request))
            return;

        String[][] styles = (DEV_MODE) ? CHART_DEV_STYLES : CHART_DEV_STYLES;
        String[][] scripts = (DEV_MODE) ? CHART_DEV_SCRIPTS : CHART_PRODUCTION_SCRIPTS;

        for (String[] pair : styles) {
            addStyleLink(response, request.getContextPath() + pair[0], pair[1]);
        }

        for (String[] pair : scripts) {
            addJavaScript(response, request.getContextPath() + pair[0], pair[1]);
        }

        includeAngluar(request, response);
    }

}

