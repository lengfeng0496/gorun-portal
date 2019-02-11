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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.portals.applications.webcontent2.proxy.ProxyContext;
import org.apache.portals.applications.webcontent2.proxy.ReverseProxyException;
import org.apache.portals.applications.webcontent2.proxy.command.InitHttpRequestCommand;
import org.apache.portals.applications.webcontent2.proxy.impl.ServletRequestContext;


public class SSOInitHttpRequestCommand extends InitHttpRequestCommand
{

    @Override
    protected HttpRequestBase createHttpRequest(final ProxyContext context) throws ReverseProxyException, IOException
    {
        List<JetspeedSSOSiteCredentials> ssoCredsList = getJetspeedFormSSOSiteCredentials(context);
        URI remoteURI = context.getRemoteURI();
        JetspeedSSOSiteCredentials ssoSiteCreds = JetspeedSSOUtils.getBestMatchedSSOSiteCrendentials(ssoCredsList, remoteURI);

        if (ssoSiteCreds != null)
        {
            HttpRequestBase httpRequest = new HttpPost(remoteURI);
            List <NameValuePair> formParams = new ArrayList<NameValuePair>();
            formParams.add(new BasicNameValuePair(ssoSiteCreds.getFormUserField(), ssoSiteCreds.getUsername()));
            formParams.add(new BasicNameValuePair(ssoSiteCreds.getFormPwdField(), ssoSiteCreds.getPassword()));
            ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(formParams));
            return httpRequest;
        }

        return super.createHttpRequest(context);
    }

    private List<JetspeedSSOSiteCredentials> getJetspeedFormSSOSiteCredentials(final ProxyContext context)
    {
        List<JetspeedSSOSiteCredentials> ssoCredsList = null;

        HttpSession session = ((ServletRequestContext) context.getRequestContext()).getServletRequest().getSession(false);

        if (session != null) {
            ssoCredsList = (List<JetspeedSSOSiteCredentials>) session.getAttribute(SSOReverseProxyIFramePortlet.SUBJECT_SSO_SITE_CREDS);
        }

        if (ssoCredsList == null)
        {
            return null;
        }

        List<JetspeedSSOSiteCredentials> formCredsList = new ArrayList<JetspeedSSOSiteCredentials>();

        for (JetspeedSSOSiteCredentials ssoCreds : ssoCredsList)
        {
            if (ssoCreds.isFormAuthentication())
            {
                formCredsList.add(ssoCreds);
            }
        }

        return formCredsList;
    }
}
