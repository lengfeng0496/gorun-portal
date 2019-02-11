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
package org.apache.jetspeed.portlets.sso;

import java.io.IOException;
import java.util.List;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.sso.SSOManager;
import org.apache.portals.applications.webcontent2.portlet.IFrameGenericPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSOReverseProxyIFramePortlet
 * 
 * @version $Id: SSOReverseProxyIFramePortlet.java 1635955 2014-11-01 14:26:28Z woonsan $
 */
public class SSOReverseProxyIFramePortlet extends IFrameGenericPortlet
{

    public static final String SUBJECT_SSO_SITE_CREDS = "org.apache.jetspeed.portlets.sso.ssoSiteCredsOfSubject";

    private static Logger log = LoggerFactory.getLogger(SSOReverseProxyIFramePortlet.class);

    private SSOManager ssoManager;

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);

        ssoManager = (SSOManager) config.getPortletContext().getAttribute("cps:SSO");

        if (null == ssoManager) 
        {
            throw new PortletException("Failed to find SSO Provider on portlet initialization"); 
        }
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        List<JetspeedSSOSiteCredentials> ssoCredsList = JetspeedSSOUtils.getSubjectSSOSiteCredentials(ssoManager);

        if (ssoCredsList.isEmpty())
        {
            String warningMessage = getResourceBundle(request.getLocale()).getString("no.credentials");
            response.getWriter().print(warningMessage);
            return;
        }

        request.getPortletSession().setAttribute(SUBJECT_SSO_SITE_CREDS, ssoCredsList, PortletSession.APPLICATION_SCOPE);

        super.doView(request, response);
    }
}
