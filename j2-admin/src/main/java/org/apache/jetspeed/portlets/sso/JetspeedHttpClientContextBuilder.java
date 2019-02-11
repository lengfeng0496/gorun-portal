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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.portals.applications.webcontent2.proxy.HttpClientContextBuilder;
import org.apache.portals.applications.webcontent2.proxy.ProxyContext;
import org.apache.portals.applications.webcontent2.proxy.impl.ServletRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JetspeedHttpClientContextBuilder implements HttpClientContextBuilder
{

    private static Logger log = LoggerFactory.getLogger(JetspeedHttpClientContextBuilder.class);

    public HttpClientContext build()
    {
        ProxyContext proxyContext = ProxyContext.getCurrentProxyContext();
        HttpServletRequest request = ((ServletRequestContext) proxyContext.getRequestContext()).getServletRequest();

        List<JetspeedSSOSiteCredentials> ssoCredsList = (List<JetspeedSSOSiteCredentials>) request.getAttribute(SSOReverseProxyIFramePortlet.SUBJECT_SSO_SITE_CREDS);

        if (ssoCredsList == null)
        {
            HttpSession session = request.getSession(false);

            if (session == null)
            {
                return null;
            }

            ssoCredsList = (List<JetspeedSSOSiteCredentials>) session.getAttribute(SSOReverseProxyIFramePortlet.SUBJECT_SSO_SITE_CREDS);
        }

        if (ssoCredsList == null || ssoCredsList.isEmpty())
        {
            return null;
        }

        HttpClientContext httpClientContext = HttpClientContext.create();

        try
        {
            httpClientContext.setCredentialsProvider(new BasicCredentialsProvider());
            httpClientContext.setAuthCache(new BasicAuthCache());

            for (JetspeedSSOSiteCredentials ssoCreds : ssoCredsList)
            {
                HttpHost targetHost = new HttpHost(ssoCreds.getHost(), ssoCreds.getPort(), ssoCreds.getScheme());
                // set Basic authentication scheme
                httpClientContext.getAuthCache().put(targetHost, new BasicScheme());
                httpClientContext.getCredentialsProvider().setCredentials(
                                             new AuthScope(targetHost.getHostName(), targetHost.getPort(), ssoCreds.getRealm()),
                                             new UsernamePasswordCredentials(ssoCreds.getUsername(), ssoCreds.getPassword()));
            }
        }
        catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.warn("Failed to retrieve sso site credentials.", e);
            }
            else
            {
                log.warn("Failed to retrieve sso site credentials. {}", e.toString());
            }
        }

        return httpClientContext;
    }
}
