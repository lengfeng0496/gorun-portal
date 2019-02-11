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

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Injects a single angular initialization function, ensuring that only one angular bootstrap statement
 * is included per rendered page of portlets
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class JetAppPortletFilter implements RenderFilter {

    protected final static String JET_DASHBOARD_ANGULAR_FLAG = "jet.dashboard.angular.flag";

    protected FilterConfig filterConfig = null;

    public void doFilter(RenderRequest request, RenderResponse response,
                         FilterChain filterChain) throws IOException, PortletException {
        filterChain.doFilter(request, response);
        includeAngular(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws PortletException {
        this.filterConfig = filterConfig;
    }

    protected final String ANGULAR = "<script>\n    angular.element(document).ready(function() {\n" +
            "        angular.bootstrap(document, ['j2admin']);\n" +
            "    });\n</script>\n";

    protected void includeAngular(RenderRequest request, RenderResponse response) throws PortletException, IOException {
//        if (filterConfig != null) {
//            String flag = filterConfig.getPortletContext().getInitParameter("jetapp");
//            System.out.println("Flag = " + flag);
//        }
        String useAngular = request.getPreferences().getValue("jetapp", null);
        if (useAngular != null && useAngular.equalsIgnoreCase("true")) {
            if (!alreadyContributedAngular(request)) {
                response.getWriter().flush();;
                //response.getWriter().write(ANGULAR.toCharArray());
                response.getPortletOutputStream().write(ANGULAR.getBytes());
            }
        }
    }

    protected boolean alreadyContributedAngular(RenderRequest renderRequest) {
        HttpServletRequest request = JetAppPortlet.getServletRequest(renderRequest);
        if (request == null) {
            return false;
        }
        Boolean contributed = (Boolean) request.getAttribute(JET_DASHBOARD_ANGULAR_FLAG);
        if (contributed == null || contributed == false) {
            request.setAttribute(JET_DASHBOARD_ANGULAR_FLAG, Boolean.TRUE);
            return false;
        }
        return true;
    }


}
