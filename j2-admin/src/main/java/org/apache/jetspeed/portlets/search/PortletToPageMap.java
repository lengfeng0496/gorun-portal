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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dtaylor on 12/5/15.
 */
public class PortletToPageMap  implements Serializable {
    private Map<String, List<String>> portletMap;
    private Map<String, List<String>> contentMap;

    public PortletToPageMap() {
        portletMap = new ConcurrentHashMap<>();
        contentMap = new ConcurrentHashMap<>();
    }

    public void putPortlet(String portlet, String pagePath) {
        List<String> pages = portletMap.get(portlet);
        if (pages == null) {
            pages = new ArrayList<>();
            portletMap.put(portlet, pages);
        }
        pages.add(pagePath);
    }

    public void putContent(String contentPath, String pagePath) {
        List<String> pages = contentMap.get(contentPath);
        if (pages == null) {
            pages = new ArrayList<>();
            contentMap.put(contentPath, pages);
        }
        pages.add(pagePath);
    }

    public List<String> getPortlet(String portlet) {
        return portletMap.get(portlet);
    }
    public List<String> getContent(String portlet) {
        return contentMap.get(portlet);
    }

    public int portletSize() {
        return portletMap.size();
    }

    public int contentSize() {
        return contentMap.size();
    }
}
