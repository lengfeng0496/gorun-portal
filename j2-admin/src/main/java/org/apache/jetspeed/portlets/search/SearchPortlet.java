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
import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.administration.PortalConfiguration;
import org.apache.jetspeed.administration.PortalConfigurationConstants;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.BaseFragmentElement;
import org.apache.jetspeed.om.page.Fragment;
import org.apache.jetspeed.om.page.FragmentDefinition;
import org.apache.jetspeed.om.page.FragmentReference;
import org.apache.jetspeed.om.page.Page;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Preference;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.page.document.Node;
import org.apache.jetspeed.page.document.NodeSet;
import org.apache.jetspeed.search.ParsedObject;
import org.apache.jetspeed.search.SearchEngine;
import org.apache.jetspeed.search.SearchResults;
import org.apache.portals.bridges.common.GenericServletPortlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Jetspeed Search Portlet
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SearchPortlet.java 1735359 2016-03-17 02:04:33Z taylor $
 */
public class SearchPortlet extends GenericServletPortlet {
    public static final String SEARCH_KEY = "SEARCH_KEY";
    public static final String SEARCH_RESULTS = "SEARCH_RESULTS";
    public static final String DEFAULT_SEARCH_RESULTS_PAGE = "/search/results.psml";

    private SearchEngine searchEngine;
    private PortalAdministration admin;
    private PageManager pageManager;
    private PortletRegistry portletRegistry;
    private PortalConfiguration configuration;

    private String filePreference;
    private String portletClass;
    private String mountPoint;

    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        PortletContext context = getPortletContext();
        searchEngine = (SearchEngine) context.getAttribute(CommonPortletServices.CPS_SEARCH_COMPONENT);
        if (searchEngine == null) {
            throw new PortletException(
                    "Could not get instance of portal Search Engine component");
        }
        admin = (PortalAdministration) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTAL_ADMINISTRATION);
        if (null == admin) {
            throw new PortletException(
                    "Failed to find the Portal Administration on portlet initialization");
        }
        pageManager = (PageManager) context.getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);
        if (pageManager == null) {
            throw new PortletException(
                    "Could not get instance of portal PageManager component");
        }
        portletRegistry = (PortletRegistry) context.getAttribute(CommonPortletServices.CPS_REGISTRY_COMPONENT);
        if (portletRegistry == null) {
            throw new PortletException(
                    "Could not get instance of portal PortletRegistry component");
        }
        configuration = (PortalConfiguration) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTAL_CONFIGURATION);
        if (null == configuration) {
            throw new PortletException(
                    "Failed to find the Portal Configuration on portlet initialization");
        }
        filePreference = config.getInitParameter("FilePreference");
        portletClass = config.getInitParameter("PortletClass");
        mountPoint = config.getInitParameter("MountPoint");

    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String viewPage = getPortletConfig().getInitParameter("ViewPage");
        getPortletContext().getRequestDispatcher(viewPage).include(request, response);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse actionResponse) throws PortletException, IOException {
        String jsSearch = request.getParameter("jsSearch");
        if (jsSearch == null) {
            jsSearch = "";
        }
        SearchResults searchResults = searchEngine.search(jsSearch);
        List<SearchInfo> searchInfoResults = new ArrayList<>();
        for (ParsedObject result : searchResults.getResults()) {
            if (result.getType().equals("url")) {
                int index = result.getTitle().indexOf(mountPoint);
                if (index > -1) {
                    String key = result.getTitle().substring(index);
                    if (key != null) {
                        List<String> links = getPortletToPageMap(request.getPortletSession()).getContent(key);
                        String link = getBestLink(links);
                        if (link != null) {
                            searchInfoResults.add(new SearchInfo(key, result.getType(), link, result.getScore(), result.getDescription(),
                                    admin.getPortalURL(request, actionResponse, link)));
                        }
                    }
                }
            } else {
                String key = result.getKey().substring(result.getKey().indexOf("::") + 2);
                List<String> links = getPortletToPageMap(request.getPortletSession()).getPortlet(key);
                if (links != null) {
                    String link = getBestLink(links);
                    if (link != null) {
                        boolean error = false;
                        try {
                            Page page = pageManager.getPage(link);
                            page.checkAccess(JetspeedActions.VIEW);
                        } catch (Exception e) {
                            error = true;
                        }
                        if (!error) {
                            searchInfoResults.add(new SearchInfo(result.getTitle(), result.getType(), key, result.getScore(), result.getDescription(),
                                    admin.getPortalURL(request, actionResponse, link)));
                        }
                    }
                }
            }
        }
        request.getPortletSession().setAttribute(SEARCH_RESULTS, searchInfoResults, PortletSession.APPLICATION_SCOPE);
        request.getPortletSession().setAttribute(SEARCH_KEY, jsSearch, PortletSession.APPLICATION_SCOPE);
        String redirect = admin.getPortalURL(request, actionResponse, DEFAULT_SEARCH_RESULTS_PAGE);
        actionResponse.sendRedirect(redirect);
    }

    private final static String SEARCH_CACHE = "J2_SEARCH_CACHE";

    /**
     * Build a map of portlets to pages where they exist
     *
     * @return
     */
    private PortletToPageMap getPortletToPageMap(PortletSession session) {

        PortletToPageMap map = (PortletToPageMap) session.getAttribute(SEARCH_CACHE);
        long start = System.currentTimeMillis();
        if (map == null) {
            //System.out.println(".... starting loading of portlet page map");
            map = new PortletToPageMap();
            try {
                Folder root = pageManager.getFolder("/");
                traverse(root, map);
            } catch (Exception e) {
                e.printStackTrace();
            }
            session.setAttribute(SEARCH_CACHE, map);
            //System.out.println(".... completed loading of " + map.portletSize() + " portlet page map in " + (System.currentTimeMillis() - start) + " ms");
            return map;
        }
        return map;
    }

    private void traverse(Folder folder, PortletToPageMap map) {
        try {
            for (Node node : folder.getAll()) {
                switch (node.getType()) {
                    case Folder.FOLDER_TYPE:
                        traverse((Folder) node, map);
                        break;
                    case Page.DOCUMENT_TYPE:
                        visitPortlets((Fragment) ((Page) node).getRootFragment(), map, node.getPath());
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void visitPortlets(Fragment root, PortletToPageMap map, String pagePath) {
        try {
            for (BaseFragmentElement f : root.getFragments()) {
                if (f instanceof FragmentReference) {
                    FragmentReference fragmentReference = (FragmentReference) f;
                    NodeSet definitions = pageManager.getFragmentDefinitions(pageManager.getFolder("/"));
                    Iterator<Node> it = definitions.iterator();
                    while (it.hasNext()) {
                        Node n = it.next();
                        Fragment fragment = (Fragment) ((FragmentDefinition) n).getRootFragment();
                        String contentPath = getContentFilePath(fragment.getName());
                        if (contentPath != null) {
                            map.putContent(contentPath, pagePath);
                        } else {
                            map.putPortlet(fragment.getName(), pagePath);
                        }
                    }
                } else {
                    Fragment fragment = (Fragment) f;
                    String contentPath = getContentFilePath(fragment.getName());
                    if (contentPath != null) {
                        map.putContent(contentPath, pagePath);
                    } else {
                        map.putPortlet(fragment.getName(), pagePath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getContentFilePath(String uniqueName) {
        PortletDefinition portlet = portletRegistry.getPortletDefinitionByUniqueName(uniqueName);
        if (portlet != null) {
            if (portlet.getPortletClass().equals(portletClass)) {
                Preference pref = portlet.getPortletPreferences().getPortletPreference(filePreference);
                if (pref != null) {
                    List<String> values = pref.getValues();
                    if (values.size() > 0) {
                        return values.get(0);
                    }
                }
            }
        }
        return null;
    }

    private String getBestLink(List<String> links) {
        if (links == null || links.size() == 0) {
            return configuration.getString("/" + PortalConfigurationConstants.PSML_PAGE_DEFAULT, "/default-page.psml");
        }
        String best = null;
        for (String link : links) {
            if (link.startsWith("/_") || link.startsWith("/system")) {
                continue;
            }
            best = link;
        }
        return best;
    }
}
