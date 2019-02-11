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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.security.mfa.util.SecurityHelper;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.applications.webcontent2.portlet.WebContentPortlet;
import org.apache.portals.applications.webcontent2.portlet.rewriter.WebContentRewriter;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SSOWebContentPortlet
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 */
public class SSOWebContentPortlet extends WebContentPortlet
{
    // sso.type
    public static final String SSO_TYPE = "sso.type";

    public static final String SSO_TYPE_HTTP = "http";
    public static final String SSO_TYPE_BASIC = "basic";
    public static final String SSO_TYPE_BASIC_PREEMPTIVE = "basic.preemptive";

    public static final String SSO_TYPE_FORM = "form";
    public static final String SSO_TYPE_FORM_GET = "form.get";
    public static final String SSO_TYPE_FORM_POST = "form.post";

    public static final String SSO_TYPE_URL = "url";
    public static final String SSO_TYPE_URL_BASE64 = "url.base64";

    public static final String SSO_TYPE_CERTIFICATE = "certificate";

    public static final String SSO_TYPE_DEFAULT = SSO_TYPE_BASIC;  // handled well even if nothing but credentials are set (see: doRequestedAuthentication)

    /**
     * @deprecated Use the key, 'no.credentials', from the portlet resource bundle instead.
     */
    public static final String NO_CREDENTIALS = "<p>No credentials configured for current user.</p>";

    public static final String[] SSO_TYPES = 
    {
        SSO_TYPE_BASIC,
        SSO_TYPE_BASIC_PREEMPTIVE,
        SSO_TYPE_FORM,
        SSO_TYPE_FORM_GET,
        SSO_TYPE_FORM_POST,
        SSO_TYPE_URL,
        SSO_TYPE_URL_BASE64,
        SSO_TYPE_CERTIFICATE
    };

    // ...standardized auth types

    public static final String BASIC_AUTH_SCHEME_NAME = "basic";

    // supporting parameters - for various sso types

    // ...names of query args for sso.type=url|url.base64

    public static final String SSO_TYPE_URL_USERNAME_PARAM = "sso.url.Principal";
    public static final String SSO_TYPE_URL_PASSWORD_PARAM = "sso.url.Credential";

    // ...names of fields for sso.type=form|form.get|form.post

    public static final String SSO_TYPE_FORM_ACTION_URL = "sso.form.Action";
    public static final String SSO_TYPE_FORM_ACTION_ARGS = "sso.form.Args";
    public static final String SSO_TYPE_FORM_USERNAME_FIELD = "sso.form.Principal";
    public static final String SSO_TYPE_FORM_PASSWORD_FIELD = "sso.form.Credential";

    // ...tags for passing creditials along on the current request object

    public static final String SSO_REQUEST_ATTRIBUTE_USERNAME = "sso.ra.username";
    public static final String SSO_REQUEST_ATTRIBUTE_PASSWORD = "sso.ra.password";

    // ...field names for EDIT mode

    public static final String SSO_EDIT_FIELD_PRINCIPAL = "ssoPrincipal";
    public static final String SSO_EDIT_FIELD_CREDENTIAL = "ssoCredential";

    // SSOWebContent session variables 

    public static final String FORM_AUTH_STATE = "ssowebcontent.form.authstate" ;

    // Class Data

    protected final static Logger log = LoggerFactory.getLogger(SSOWebContentPortlet.class);

    // Data Members

    protected PortletContext context;
    protected SSOManager sso;
    protected UserManager userManager;
    protected List<String> ssoTypesList;

    // Methods

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);

        context = getPortletContext();

        sso = (SSOManager) context.getAttribute("cps:SSO");

        if (sso == null)
        {
           throw new PortletException("Failed to find SSO Manager on portlet initialization");
        }

        userManager = (UserManager) context.getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);

        if (null == userManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }

        ssoTypesList = new LinkedList<String>();

        for (String s : SSO_TYPES)
        {
            ssoTypesList.add(s);
        }
    }

    protected JetspeedPrincipal getLocalPrincipal(String localUserName) {
        JetspeedPrincipal localPrincipal = null;

        try{
            localPrincipal = userManager.getUser(localUserName);
        } catch (SecurityException secex){
        }

        return localPrincipal;
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException
    {
        // grab parameters - they will be cleared in processing of edit response
        String webContentParameter = actionRequest.getParameter(WebContentRewriter.ACTION_PARAMETER_URL);
        String ssoPrincipalName = actionRequest.getParameter(SSO_EDIT_FIELD_PRINCIPAL);
        String ssoPrincipalPassword = actionRequest.getParameter(SSO_EDIT_FIELD_CREDENTIAL);

        // save the prefs
        super.processAction(actionRequest, actionResponse);

        // process credentials
        if (webContentParameter == null || actionRequest.getPortletMode() == PortletMode.EDIT)
        {
            // processPreferencesAction(request, actionResponse);
            // get the POST params -- requires HTML post params named above 
            String siteUrl = actionRequest.getPreferences().getValue("SRC", "");
            SSOSite site = JetspeedSSOUtils.getBestSubjectSSOSiteByURL(sso, siteUrl);

            try
            {
                if (!SecurityHelper.isEmpty(siteUrl) && !SecurityHelper.isEmpty(ssoPrincipalName) && !SecurityHelper.isEmpty(ssoPrincipalPassword))
                {
                    if (site == null)
                    {
                        site = sso.newSite(siteUrl, siteUrl);
                        sso.addSite(site);
                        SSOPortletUtil.updateUser(sso, actionRequest, site, ssoPrincipalName, ssoPrincipalPassword);
                    }
                    else
                    {
                        SSOPortletUtil.updateUser(sso, actionRequest, site, ssoPrincipalName, ssoPrincipalPassword);
                    }
                }
            }
            catch (SSOException e)
            {
                String errorMessage = "Failed to add remote user for the portal principal, " + actionRequest.getUserPrincipal().getName() + ".";

                if (e.getCause() != null)
                {
                    errorMessage += " (" + e.getCause() + ")";
                }

                StatusMessage statusMessage = new StatusMessage(errorMessage, StatusMessage.ERROR);
                PortletMessaging.publish(actionRequest, "SSOWebContent", "status", statusMessage);
                actionResponse.setPortletMode(PortletMode.EDIT); // stay on edit
            }
        }
    }

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String siteName = request.getPreferences().getValue("SRC", null);
        SSOSite site = null;

        if (siteName != null)
        {
            site = JetspeedSSOUtils.getBestSubjectSSOSiteByURL(sso, siteName);
        }

        if (site == null)
        {
            String warningMessage = getResourceBundle(request.getLocale()).getString("no.credentials");
            response.getWriter().print(warningMessage);
            return;
        }
        else
        {
            PasswordCredential pwc = SSOPortletUtil.getCredentialsForSite(sso,siteName,request);

            if (pwc != null)
            {
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME, pwc.getUserName());
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD, pwc.getPassword());
            }
            else
            {
                String warningMessage = getResourceBundle(request.getLocale()).getString("no.credentials");
                response.getWriter().print(warningMessage);
                return;
            }
        }

        StatusMessage msg = (StatusMessage) PortletMessaging.consume(request, "SSOWebContent", "status");

        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);
        }

        super.doView(request, response);
    }

    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String site = request.getPreferences().getValue("SRC", "");
        PasswordCredential pwc = SSOPortletUtil.getCredentialsForSite(sso,site,request);

        if (pwc != null)
        {
            getContext(request).put(SSO_EDIT_FIELD_PRINCIPAL, pwc.getUserName());
            getContext(request).put(SSO_EDIT_FIELD_CREDENTIAL, pwc.getPassword());
        }
        	else
        {
            // no credentials configured in SSO store
            // switch to SSO Configure View
            getContext(request).put(SSO_EDIT_FIELD_PRINCIPAL, "");
            getContext(request).put(SSO_EDIT_FIELD_CREDENTIAL, "");
        }

        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOWebContent", "status");

        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);
        }

        this.getContext(request).put("ssoTypes", SSO_TYPES);
        this.getContext(request).put("ssoTypeSelected", request.getPreferences().getValue("sso.type", SSO_TYPE_BASIC));

        super.doEdit(request, response);
    }

    @Override
    protected HttpClientContext getHttpClientContext(PortletRequest request, HttpRequestBase httpRequest)
    {
        HttpClientContext httpClientContext = null;

        PortletPreferences prefs = request.getPreferences();
        String type = getSingleSignOnAuthType(prefs);

        if (SSO_TYPE_BASIC_PREEMPTIVE.equalsIgnoreCase(type))
        {
            // Preemptive, basic authentication
            String ssoUserName = StringUtils.defaultString((String) request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME));
            String ssoPassword = StringUtils.defaultString((String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD));

            httpClientContext = HttpClientContext.create();
            httpClientContext.setCredentialsProvider(new BasicCredentialsProvider());
            httpClientContext.setAuthCache(new BasicAuthCache());
            URI targetURI = httpRequest.getURI();
            String targetScheme = targetURI.getScheme();
            String targetHost = targetURI.getHost();
            int targetPort = (targetURI.getPort() > 0 ? targetURI.getPort() : ("https".equals(targetScheme) ? 443 : 80));
            HttpHost targetHttpHost = new HttpHost(targetHost, targetPort, targetScheme);
            // set Basic authentication scheme
            httpClientContext.getAuthCache().put(targetHttpHost, new BasicScheme());
            httpClientContext.getCredentialsProvider().setCredentials(
                                         new AuthScope(targetHost, targetPort),
                                         new UsernamePasswordCredentials(ssoUserName, ssoPassword));
        }

        return httpClientContext;
    }

    @Override
    protected byte[] doPreemptiveAuthentication(CloseableHttpClient client, CookieStore cookieStore, HttpRequestBase httpRequest, RenderRequest request, RenderResponse response)
    {
        byte[] result = super.doPreemptiveAuthentication(client, cookieStore, httpRequest, request, response);

        if ( result != null)
        {
            // already handled
            return result ;
        }

        PortletPreferences prefs = request.getPreferences();
        String type = getSingleSignOnAuthType(prefs);

        if (StringUtils.startsWith(type, SSO_TYPE_FORM))
        {
            try
            {
                Boolean formAuth = (Boolean) PortletMessaging.receive(request, FORM_AUTH_STATE);

                if (formAuth != null)
                {
                    // already been here, done that
                    return (formAuth.booleanValue() ? result : null);
                }
                else
                {
                    // stop recursion, but assume failure, ...for now
                    PortletMessaging.publish(request, FORM_AUTH_STATE, Boolean.FALSE);
                }

                String formAction = prefs.getValue(SSO_TYPE_FORM_ACTION_URL, "");

                if (StringUtils.isEmpty(formAction))
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_ACTION_URL+", action was specified - unable to preemptively authenticate by form.");
                    return null ;
                }

                String userNameField = prefs.getValue(SSO_TYPE_FORM_USERNAME_FIELD, "");

                if (StringUtils.isEmpty(userNameField))
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_USERNAME_FIELD+", username field was specified - unable to preemptively authenticate by form.");
                    return null ;
                }

                String passwordField = prefs.getValue(SSO_TYPE_FORM_PASSWORD_FIELD, "password");

                if (StringUtils.isEmpty(passwordField))
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_PASSWORD_FIELD+", password field was specified - unable to preemptively authenticate by form.");
                    return null ;
                }

                String userName = StringUtils.defaultString((String) request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME));
                String password = StringUtils.defaultString((String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD));

                // get submit method
                boolean isPost = true;

                if (StringUtils.contains(type, '.')) {
                    isPost = StringUtils.equalsIgnoreCase(StringUtils.substringAfter(type, "."), HttpPost.METHOD_NAME);
                } else {
                    isPost = StringUtils.equalsIgnoreCase(type, HttpPost.METHOD_NAME);
                }

                String formMethod = (isPost) ? HttpPost.METHOD_NAME : HttpGet.METHOD_NAME;

                // get parameter map
                Map<String, String[]> formParams = new HashMap<String, String[]>();
                formParams.put(userNameField, new String[] { userName });
                formParams.put(passwordField, new String[] { password });
                String formArgs = prefs.getValue(SSO_TYPE_FORM_ACTION_ARGS, "");

                if (StringUtils.isNotEmpty(formArgs))
                {
                    String [] pairs = StringUtils.split(formArgs, ";");
                    String paramName;
                    String paramValue;

                    for (String pair : pairs)
                    {
                        paramName = StringUtils.substringBefore(pair, "=");
                        paramValue = StringUtils.substringAfter(pair, "=");

                        if (StringUtils.isNotEmpty(paramName))
                        {
                            formParams.put(paramName, new String[] { paramValue });
                        }
                    }
                }

                // resuse client - in case new cookies get set - but create a new method (for the formAction)
                HttpRequestBase httpFormRequest = createHttpRequest(client, formMethod, formAction, null, formParams, request);
                result = doHttpWebContent(client, cookieStore, httpFormRequest, 0, request, response, null);

                PortletMessaging.publish(request, FORM_AUTH_STATE, Boolean.valueOf(result != null));
                return result ;
            }
            catch (Exception ex)
            {
                // bad
                log.error("Form-based authentication failed", ex);
            }
        }
        else if (type.equalsIgnoreCase(SSO_TYPE_URL) || type.equalsIgnoreCase(SSO_TYPE_URL_BASE64))
        {
            // set user name and password parameters in the HttpMethod
            String userNameParam = prefs.getValue(SSO_TYPE_URL_USERNAME_PARAM, "");

            if (StringUtils.isEmpty(userNameParam))
            {
                log.warn("sso.type specified as 'url', but no: "+SSO_TYPE_URL_USERNAME_PARAM+", username parameter was specified - unable to preemptively authenticate by URL.");
                return null ;
            }

            String passwordParam = prefs.getValue(SSO_TYPE_URL_PASSWORD_PARAM, "");

            if (StringUtils.isEmpty(passwordParam))
            {
                log.warn("sso.type specified as 'url', but no: "+SSO_TYPE_URL_PASSWORD_PARAM+", password parameter was specified - unable to preemptively authenticate by URL.");
                return null ;
            }

            String userName = StringUtils.defaultString((String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME));
            String password = StringUtils.defaultString((String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD));

            if (SSO_TYPE_URL_BASE64.equalsIgnoreCase(type))
            {
                Base64 encoder = new Base64() ;
                userName = new String(encoder.encode(userName.getBytes()));
                password = new String(encoder.encode(password.getBytes()));
            }

            // GET and POST accept args differently
            if ( httpRequest instanceof HttpPost )
            {
                // add POST data
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                formParams.add(new BasicNameValuePair(userNameParam, userName));
                formParams.add(new BasicNameValuePair(passwordParam, password));
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                ((HttpPost) httpRequest).setEntity(httpEntity);
            }
            else
            {
                try
                {
                    // augment GET query string
                    URIBuilder uriBuilder = new URIBuilder(httpRequest.getURI());
                    uriBuilder.addParameter(userNameParam, userName);
                    uriBuilder.addParameter(passwordParam, password);
                    httpRequest.setURI(uriBuilder.build());
                }
                catch (URISyntaxException e)
                {
                    log.error("URI syntax error.", e);
                }
            }

            return result ;
        }

        // not handled
        return null ;
    }

    protected String getSingleSignOnAuthType(PortletPreferences prefs)
    {
        String type = prefs.getValue(SSO_TYPE, SSO_TYPE_DEFAULT);

        if (SSO_TYPE_HTTP.equalsIgnoreCase(type))
        {
            log.warn("sso.type: " + SSO_TYPE_HTTP + ", has been deprecated - use: " + SSO_TYPE_BASIC + ", or: " + SSO_TYPE_BASIC_PREEMPTIVE);
            type = SSO_TYPE_BASIC;
        }

        return type;
    }
}
