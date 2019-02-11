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
package org.apache.jetspeed.portlets.search;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.search.SearchEngine;
import org.apache.portals.bridges.common.GenericServletPortlet;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Jetspeed Search Portlet
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SearchResultsPortlet.java 1721082 2015-12-21 04:07:14Z taylor $
 */
public class SearchResultsPortlet extends GenericServletPortlet {
    public static final String SEARCH_PAGE_MAP = "searchPageMap";
    private SearchEngine searchEngine;

    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        PortletContext context = getPortletContext();
        searchEngine = (SearchEngine) context.getAttribute(CommonPortletServices.CPS_SEARCH_COMPONENT);
        if (searchEngine == null)
            throw new PortletException(
                    "Could not get instance of portal Search Engine component");
    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        String viewPage = getPortletConfig().getInitParameter("ViewPage");
        List<SearchInfo> results = (List<SearchInfo>) request.getPortletSession().getAttribute(SearchPortlet.SEARCH_RESULTS, PortletSession.APPLICATION_SCOPE);
        if (results == null) {
            results = new ArrayList<>();
        }
        String searchKey = (String) request.getPortletSession().getAttribute(SearchPortlet.SEARCH_KEY, PortletSession.APPLICATION_SCOPE);
        request.setAttribute(SearchPortlet.SEARCH_KEY, searchKey);
        request.setAttribute(SearchPortlet.SEARCH_RESULTS, results);
        getPortletContext().getRequestDispatcher(viewPage).include(request, response);
    }

}