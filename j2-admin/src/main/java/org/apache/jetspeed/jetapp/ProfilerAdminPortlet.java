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
import org.apache.jetspeed.jetapp.dto.DtoAdminBasePrefs;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class ProfilerAdminPortlet extends JetAppPortlet {

    protected static Log log = LogFactory.getLog(ProfilerAdminPortlet.class);

    protected static final String PREFS_ROWS_PER_PAGE = "rowsPerPage";

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        super.doView(request, response);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        if (authenticate(request, response)) {
            PortletRequestDispatcher dispatcher = null;
            String url = request.getPreferences().getValue(PREFS_EDIT, "/jetapp/views/profiler-admin-edit.jsp" );
            dispatcher = getPortletContext().getRequestDispatcher(url);
            dispatcher.include(request, response);
        }
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();
        if (resourceID == null) {
            response.addProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            response.getWriter().println(createError(400, "invalid resource id"));
            return;
        }
        if (resourceID.equals("writePrefs")) {
            StringWriter writer = new StringWriter();
            drain(request.getReader(), writer);
            String json = writer.toString();
            ObjectMapper writeMapper = new ObjectMapper();
            DtoAdminBasePrefs update = writeMapper.readValue(json, DtoAdminBasePrefs.class);
            request.getPreferences().setValue(PREFS_ROWS_PER_PAGE , Integer.toString(update.getRowsPerPage()));
            request.getPreferences().store();
        }
        DtoAdminBasePrefs prefs = new DtoAdminBasePrefs();
        prefs.setRowsPerPage(Integer.parseInt(request.getPreferences().getValue(PREFS_ROWS_PER_PAGE , "20")));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, prefs);
        response.getWriter().println(writer);
    }



}
