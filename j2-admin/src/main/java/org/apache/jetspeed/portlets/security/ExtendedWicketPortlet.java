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
package org.apache.jetspeed.portlets.security;

import org.apache.jetspeed.JetspeedActions;
import org.apache.wicket.protocol.http.portlet.WicketPortlet;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;

public class ExtendedWicketPortlet extends WicketPortlet {

    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        if ( !request.getWindowState().equals(WindowState.MINIMIZED))
        {
            PortletMode curMode = request.getPortletMode();
            if (JetspeedActions.EDIT_DEFAULTS_MODE.equals(curMode))
            {
                //request.setAttribute(PARAM_EDIT_PAGE, DEFAULT_EDIT_DEFAULTS_PAGE);
                doEdit(request, response);
            }
            else
            {
                super.doDispatch(request, response);
            }
        }

    }
}
