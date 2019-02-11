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
import javax.servlet.http.Cookie;
import java.io.IOException;

/**
 * Extends JetApp Portlet, adding support for using API Tokens from server side
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class JetAppTokenPortlet extends  JetAppPortlet {

    protected final static String MSG_MISSING_REST_SERVICE_PARAM = "<b>Missing initialization parameter. Cannot connect to API Server - missing param: %s</b>";
    protected static final String MISSING_INIT_PARAMETER = "Missing initialization parameter for app: ";

    protected final static String SESSION_JETAPP_TOKEN = "JetAppToken";
    protected final static String JET_APP_NAME = "j2-admin";

    protected static final String COOKIE_JETAPP_TOKEN = "JetAppToken";
    protected static final String COOKIE_JETAPP_REST_SERVICE = "JetAppRestService";
    protected final static String MSG_FAILED_AUTHENTICATE = "<b>Failed to authenticate. Cannot connect to API Server</b>";

    protected JetAppCredentials getCredentials() {
        return new JetAppCredentials("user", "user", "http://localhost/services", JET_APP_NAME);
    }
    protected boolean authenticate(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        String token = (String)request.getPortletSession(true).getAttribute(SESSION_JETAPP_TOKEN);
        if (token == null) {
            JetAppCredentials credentials = getCredentials();
            Cookie restCookie = new Cookie(COOKIE_JETAPP_REST_SERVICE, credentials.getEndpoint());
            restCookie.setPath("/");
            response.addProperty(restCookie);

            try {
                RestAuthClient client = new RestAuthClient(credentials);
                JetAppAuthResult result = client.login();
                if (result.isSuccess()) {
                    token = result.getToken();
                    Cookie tokenCookie = new Cookie(COOKIE_JETAPP_TOKEN, token);
                    tokenCookie.setPath("/");
                    response.addProperty(tokenCookie);
                    request.getPortletSession().setAttribute(SESSION_JETAPP_TOKEN, token);
                    return true;
                }
                log.error("Failed authentication in for app: " + JET_APP_NAME);
                response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                return false;
            }
            catch (Exception e) {
                log.error("Exception during authentication in Dashboard: " + e.getMessage(), e);
                response.getWriter().println(MSG_FAILED_AUTHENTICATE);
                return false;
            }
        }
        return true;
    }

}
