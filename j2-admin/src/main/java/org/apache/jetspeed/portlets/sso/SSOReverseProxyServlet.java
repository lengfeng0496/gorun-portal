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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.portals.applications.webcontent2.proxy.HttpClientContextBuilder;
import org.apache.portals.applications.webcontent2.proxy.command.InitHttpRequestCommand;
import org.apache.portals.applications.webcontent2.proxy.impl.ProxyProcessingChain;
import org.apache.portals.applications.webcontent2.proxy.servlet.SimpleReverseProxyServlet;

/**
 * Extending {@link SimpleReverseProxyServlet} in order to replace the default initializing command
 * by a custom initializing command, {@link SSOInitHttpRequestCommand}, which can read
 * Jetspeed SSO Site credentials.
 * Also, this class customizes the default {@link HttpClientContextBuilder} by a custom one,
 * {@link JetspeedHttpClientContextBuilder}, in order to build custom authentication states
 * based on the Jetspeed SSO Site credentials.
 */
public class SSOReverseProxyServlet extends SimpleReverseProxyServlet
{

    private static final long serialVersionUID = 1L;

    /**
     * Zero-argument default constructor.
     */
    public SSOReverseProxyServlet()
    {
        super();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Also, it sets a custom {@link HttpClientContextBuilder}, {@link JetspeedHttpClientContextBuilder},
     * in order to build custom authentication states based on the Jetspeed SSO Site credentials.
     * </p>
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        setHttpClientContextBuilder(new JetspeedHttpClientContextBuilder());
        super.init(servletConfig);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Also, it replaces the default initializing command by a custom initializing command,
     * {@link SSOInitHttpRequestCommand}, which can read Jetspeed SSO Site credentials.
     * </p>
     */
    @Override
    protected ProxyProcessingChain createProxyServiceCommand() {
        ProxyProcessingChain proxyChain = super.createProxyServiceCommand();
        ProxyProcessingChain processingChain = (ProxyProcessingChain) proxyChain.getCommand(1);
        int index = processingChain.getCommandIndex(InitHttpRequestCommand.class);

        if (index != -1) {
            processingChain.removeCommand(index);
            processingChain.addCommand(index, new SSOInitHttpRequestCommand());
        }

        return proxyChain;
    }
}
