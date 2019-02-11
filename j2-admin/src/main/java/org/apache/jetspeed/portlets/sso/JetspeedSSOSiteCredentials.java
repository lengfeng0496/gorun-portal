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

import java.io.Serializable;
import java.net.URI;

public class JetspeedSSOSiteCredentials implements Serializable
{

    private static final long serialVersionUID = 1L;

    private URI baseURI;
    private String host;
    private int port = -1;
    private String realm;
    private String scheme;

    private String username;
    private String password;

    private boolean challengeResponseAuthentication = true;
    private boolean formAuthentication = false;
    private String formUserField;
    private String formPwdField;

    public JetspeedSSOSiteCredentials()
    {
        this(null);
    }

    public JetspeedSSOSiteCredentials(URI baseURI)
    {
        this(baseURI, null);
    }

    public JetspeedSSOSiteCredentials(URI baseURI, String host)
    {
        this(baseURI, host, -1);
    }

    public JetspeedSSOSiteCredentials(URI baseURI, String host, int port)
    {
        this(baseURI, host, port, null);
    }

    public JetspeedSSOSiteCredentials(URI baseURI, String host, int port, String realm)
    {
        this.baseURI = baseURI;
        this.host = host;
        this.port = port;
        this.realm = realm;
    }

    public URI getBaseURI()
    {
        return baseURI;
    }

    public void setBaseURI(URI baseURI)
    {
        this.baseURI = baseURI;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isChallengeResponseAuthentication()
    {
        return challengeResponseAuthentication;
    }

    public void setChallengeResponseAuthentication(boolean challengeResponseAuthentication)
    {
        this.challengeResponseAuthentication = challengeResponseAuthentication;
    }

    public boolean isFormAuthentication()
    {
        return formAuthentication;
    }

    public void setFormAuthentication(boolean formAuthentication)
    {
        this.formAuthentication = formAuthentication;
    }

    public String getFormUserField()
    {
        return formUserField;
    }

    public void setFormUserField(String formUserField)
    {
        this.formUserField = formUserField;
    }

    public String getFormPwdField()
    {
        return formPwdField;
    }

    public void setFormPwdField(String formPwdField)
    {
        this.formPwdField = formPwdField;
    }
}
