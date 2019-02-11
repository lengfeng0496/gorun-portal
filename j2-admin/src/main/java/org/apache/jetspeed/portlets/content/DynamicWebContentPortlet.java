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
package org.apache.jetspeed.portlets.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.applications.webcontent2.portlet.WebContentPortlet;
import org.apache.portals.applications.webcontent2.portlet.history.WebContentPage;
import org.apache.portals.applications.webcontent2.portlet.rewriter.MappingRewriterController;
import org.apache.portals.applications.webcontent2.portlet.rewriter.Rewriter;
import org.apache.portals.applications.webcontent2.portlet.rewriter.RewriterController;
import org.apache.portals.applications.webcontent2.portlet.rewriter.RewriterException;
import org.apache.portals.applications.webcontent2.portlet.rewriter.WebContentRewriter;
import org.apache.portals.applications.webcontent2.portlet.rewriter.html.neko.NekoParserAdaptor;
import org.apache.portals.applications.webcontent2.portlet.rewriter.rules.Ruleset;
import org.apache.portals.applications.webcontent2.portlet.rewriter.xml.SaxParserAdaptor;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebContentPortlet that dynamically adjusts its content url based
 * on portal request urls. This portlet is designed to work in conjunction
 * with the DynamicPage PSML and PortalSite component in Jetspeed. 
 * 
 * @author <a href="mailto:rwatler@apache.org">Randy Watler</a>
 * @version $Id$
 */
public class DynamicWebContentPortlet extends WebContentPortlet
{
    private final static Logger log = LoggerFactory.getLogger(DynamicWebContentPortlet.class);

    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.portlet.WebContentPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException
    {
        // process preferences
        if (actionRequest.getPortletMode() == PortletMode.EDIT)
        {
            processPreferencesAction(actionRequest, actionResponse);
            return;
        }

        // extract rewritten action and save in page
        String webContentURL = actionRequest.getParameter(WebContentRewriter.ACTION_PARAMETER_URL);
        String webContentMethod = actionRequest.getParameter(WebContentRewriter.ACTION_PARAMETER_METHOD);
        Map<String, String[]> webContentParams = new HashMap(actionRequest.getParameterMap()) ;
        webContentParams.remove(WebContentRewriter.ACTION_PARAMETER_URL);
        webContentParams.remove(WebContentRewriter.ACTION_PARAMETER_METHOD);
        WebContentPage webContentPage = new WebContentPage(webContentURL, webContentMethod, webContentParams);
        PortletMessaging.publish(actionRequest, getClass().getName(), webContentPage);        
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.portlet.WebContentPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // portal request context
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);

        // default page view rendering
        String viewPage = (String)request.getAttribute(PARAM_VIEW_PAGE);
        if (viewPage != null)
        {
            super.doView(request, response);
            return;
        }

        // get source web content URL, parameters, and method to view
        String sourceURL = null;
        Map sourceParams = null;
        String sourceMethod = null;
        WebContentPage webContentPage = (WebContentPage)PortletMessaging.receive(request, getClass().getName());
        if (webContentPage != null)
        {
            // view rewritten action URL page
            sourceURL = webContentPage.getUrl();
            sourceParams = webContentPage.getParams();
            sourceMethod = StringUtils.defaultIfBlank(webContentPage.getMethod(), HttpGet.METHOD_NAME);
        }
        else
        {
            // load and validate preferences, (base url and portal base path
            // should both end in a "/" path separator to ensure that relative
            // urls in the content resolve predictably)
            String baseURL = request.getPreferences().getValue("SRC", null);
            String portalBasePath = request.getPreferences().getValue("PORTALPATH", null);
            if ((baseURL == null) || (portalBasePath == null))
            {
                throw new PortletException("Required SRC and PORTALPATH preferences not set");
            }
            if (!baseURL.endsWith("/"))
            {
                baseURL += "/";
            }
            if (!portalBasePath.startsWith("/"))
            {
                portalBasePath = "/"+portalBasePath;
            }        
            if (!portalBasePath.endsWith("/"))
            {
                portalBasePath += "/";
            }        
            // view content page based on portal request URL
            String portalRequestPath = rc.getPath();
            if (!portalRequestPath.startsWith(portalBasePath))
            {
                throw new PortletException("Unable to map portal request path: "+portalRequestPath+" onto portal base path: "+portalBasePath);
            }
            sourceURL = baseURL+portalRequestPath.substring(portalBasePath.length());            
        }

        // get web content
        byte[] content = null;

        try
        {
            // get and rewrite web content
            if (log.isDebugEnabled())
            {
                log.debug("Portal request: "+rc.getPath()+", Web content: "+sourceURL);
            }

            content = doWebContent(sourceMethod, sourceURL, sourceParams, request, response);

            // write web content to portlet writer
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            IOUtils.copy(new InputStreamReader(bais, "UTF-8"), writer);
            bais.close();
        }
        catch (Throwable t)
        {
            throw new PortletException("Error retrieveing web content: "+t, t);
        }
    }

    @Override
    protected RewriterController getRewriterController(String contextPath) throws Exception
    {
        Class[] rewriterClasses = new Class[] { DynamicWebContentRewriter.class, DynamicWebContentRewriter.class };
        Class[] adaptorClasses = new Class[] { NekoParserAdaptor.class, SaxParserAdaptor.class };
        RewriterController rewriterController = 
                        new MappingRewriterController(contextPath + "conf/rewriter-rules-mapping.xml", 
                                                      Arrays.asList(rewriterClasses),
                                                      Arrays.asList(adaptorClasses));
        return rewriterController;
    }

    @Override
    protected Rewriter createRewriter(PortletRequest request, RewriterController rewriterController, Ruleset ruleset) throws RewriterException
    {
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        String basePortalPath = rc.getPortalURL().getPageBasePath();

        DynamicWebContentRewriter rewriter = (DynamicWebContentRewriter) super.createRewriter(request, rewriterController, ruleset);
        rewriter.setBasePortalPath(basePortalPath);

        return rewriter;
    }
}
