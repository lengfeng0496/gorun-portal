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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.jetapp.dto.DtoError;
import org.w3c.dom.Element;

import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * Base Jet App portlet, providing a little help for managing Jet App CSS and JS resource by injecting
 * web resources into portal header phase. Jetspeed handles duplicated resources by ID, ensuring only
 * one resource is included. Recommended to use with a web resource optimizer.
 * In J2-Admin, we employ WRO4J
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class JetAppPortlet extends GenericPortlet {

    protected static Log log = LogFactory.getLog(JetAppPortlet.class);

    // aggregation flags to limit contributions per page
    protected final static String JETAPP_ANGULAR_FLAG = "jetapp.angular.flag";
    protected final static String JETAPP_HEADERS_FLAG = "jetapp.headers.flag";

    // Angular Bootstrap script
    protected final String ANGULAR = "<script>\n    angular.element(document).ready(function() {\n" +
            "        angular.bootstrap(document, ['j2admin']);\n" +
            "    });\n</script>\n";

    // Web Resource IDs
    protected static final String J2_ADMIN_CSS_ID = "j2admin_css";
    protected static final String J2_ADMIN_EXTERNAL_SCRIPT_ID = "j2admin_ext_script";
    protected static final String J2_ADMIN_INTERNAL_SCRIPT_ID = "j2admin_int_script";

    // use merged and minified resources when releasing
    public static final boolean DEV_MODE = false;

    // Web Resources
    protected static String[][] PRODUCTION_STYLES = {
         {"/wro/J2_ADMIN_CSS.css", J2_ADMIN_CSS_ID},
    };

    protected static String[][] DEV_STYLES = {
            {"/jetapp/j2-admin-skin.css", J2_ADMIN_CSS_ID}
    };

    protected static String[][] PRODUCTION_SCRIPTS = {
            {"/wro/J2_ADMIN_EXTERNAL_JS.js", J2_ADMIN_EXTERNAL_SCRIPT_ID},
            {"/wro/J2_ADMIN_INTERNAL_JS.js", J2_ADMIN_INTERNAL_SCRIPT_ID}
    };

    protected static String[][] DEV_SCRIPTS = {
            { "/wro/J2_ADMIN_EXTERNAL_JS.js", J2_ADMIN_EXTERNAL_SCRIPT_ID},
            { "/jetapp/scripts/TextMessages.js", "j2admin_text"},
            { "/jetapp/scripts/ServerService.js", "j2admin_server"},
            { "/jetapp/scripts/RestApiService.js", "j2admin_services"},
            { "/jetapp/scripts/PortletService.js", "j2admin_portlet"},
            { "/jetapp/scripts/StatisticsRestServices.js", "j2admin_statistics"},
            { "/jetapp/app.js", "j2admin_app"},
            { "/jetapp/scripts/controllers.js", "j2admin_controllers"},
            { "/jetapp/scripts/chartControllers.js", "j2admin_chart_controllers"},
            { "/jetapp/scripts/directives.js", "j2admin_directives"},
            { "/jetapp/scripts/filters.js", "j2admin_filters"}
    };

    protected final static String PREFS_VIEW = "View";
    protected final static String PREFS_EDIT = "Edit";

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        response.setContentType("text/html");
        if (authenticate(request, response)) {
            String url = request.getPreferences().getValue(PREFS_VIEW, "/jetapp/views/profiler-admin.jsp" );
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    protected boolean authenticate(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        return true;
    }

    protected void addJavaScript(RenderResponse response, String scriptPath, String scriptId) {
        Element headElem = response.createElement("script");
        headElem.setAttribute("language", "javascript");
        if (scriptId != null) {
            headElem.setAttribute("id", scriptId);
        }
        headElem.setAttribute("src", scriptPath);
        headElem.setAttribute("type", "text/javascript");
        response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
    }

    protected void addStyleLink(RenderResponse response, String cssPath, String cssId) {
        Element headElem = response.createElement("link");
        headElem.setAttribute("rel", "stylesheet");
        if (cssId != null) {
            headElem.setAttribute("id", cssId);
        }
        headElem.setAttribute("href", cssPath);
        headElem.setAttribute("type", "text/css");
        response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
    }

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
        super.doHeaders(request, response);

        if (alreadyContributedHeaders(request))
            return;

        String[][] styles = (DEV_MODE) ? DEV_STYLES : DEV_STYLES;
        String[][] scripts = (DEV_MODE) ? DEV_SCRIPTS : PRODUCTION_SCRIPTS;

        for (String[] pair : styles) {
            addStyleLink(response, request.getContextPath() + pair[0], pair[1]);
        }

        for (String[] pair : scripts) {
            addJavaScript(response, request.getContextPath() + pair[0], pair[1]);
        }

        includeAngluar(request, response);
    }

    protected void includeAngluar(RenderRequest request, RenderResponse response) {
        // contribute Angular once
        try {
            response.getWriter().write(ANGULAR.toCharArray());
        }
        catch (IOException e) {
            log.error("Failed to include Angular bits", e);
        }
    }

    public String createError(int httpCode, String message) throws IOException {
        DtoError error = new DtoError(httpCode, message);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, error);
        return writer.toString();
    }

    protected static HttpServletRequest getServletRequest(RenderRequest request) {
            Object context = request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
            if (context != null) {
                try {
                    Method getRequest = context.getClass().getMethod("getRequest");
                    if (getRequest != null) {
                        return (HttpServletRequest)getRequest.invoke(context);
                    }
                }
                catch (Exception e2) {
                    log.error("Failed to retrieve portal servlet request: " + e2.getMessage(), e2);
                    return null;
                }
            }
        return null;
    }

    // JBossAS7...
    //        try {
    //            return (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
    //        }
    //        catch (Exception e) {
    //            log.error("Failed to retrieve portal servlet request: " + e.getMessage(), e);
    //        }



    protected boolean alreadyContributedHeaders(RenderRequest renderRequest) {
        HttpServletRequest request = getServletRequest(renderRequest);
        if (request == null)
            return false;
        Boolean contributed = (Boolean)request.getAttribute(JETAPP_HEADERS_FLAG);
        if (contributed == null || contributed == false) {
            request.setAttribute(JETAPP_HEADERS_FLAG, Boolean.TRUE);
            return false;
        }
        return true;
    }

    private static final int BLOCK_SIZE = 4096;

    public static void drain(Reader r, Writer w) throws IOException {
        char[] bytes = new char[BLOCK_SIZE];
        try {
            int length = r.read(bytes);
            while (length != -1) {
                if (length != 0) {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally {
            bytes = null;
        }

    }


}



