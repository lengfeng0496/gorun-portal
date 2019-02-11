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

import java.net.URI;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JetspeedSSOUtils
{

    private static Logger log = LoggerFactory.getLogger(JetspeedSSOUtils.class);

    private JetspeedSSOUtils()
    {
    }

    public static List<JetspeedSSOSiteCredentials> getSubjectSSOSiteCredentials(SSOManager ssoManager)
    {
        List<JetspeedSSOSiteCredentials> ssoCredsList = new ArrayList<JetspeedSSOSiteCredentials>();

        try
        {
            Subject subject = JSSubject.getSubject(AccessController.getContext());
            Collection<SSOSite> ssoSites = ssoManager.getSitesForSubject(subject);

            if (ssoSites != null)
            {
                URI siteURI = null;
                String scheme = "http";
                String host = null;
                int port = 80;

                for (SSOSite ssoSite : ssoSites)
                {
                    siteURI = URI.create(ssoSite.getURL());

                    if (StringUtils.isNotEmpty(siteURI.getScheme()))
                    {
                        scheme = siteURI.getScheme();
                    }

                    host = siteURI.getHost();

                    if (StringUtils.isEmpty(host))
                    {
                        log.warn("Skipping invalid SSO site URI (no host): '{}'.", host);
                        continue;
                    }

                    if (siteURI.getPort() > 0)
                    {
                        port = siteURI.getPort();
                    }

                    Collection<SSOUser> ssoUsers = ssoManager.getRemoteUsers(ssoSite, subject);

                    if (ssoUsers != null)
                    {
                        for (SSOUser ssoUser : ssoUsers)
                        {
                            String realm = ssoSite.getRealm();
                            PasswordCredential pwc = ssoManager.getCredentials(ssoUser);

                            JetspeedSSOSiteCredentials ssoCreds = new JetspeedSSOSiteCredentials(siteURI, host, port, realm);
                            ssoCreds.setScheme(scheme);
                            ssoCreds.setChallengeResponseAuthentication(ssoSite.isChallengeResponseAuthentication());
                            ssoCreds.setFormAuthentication(ssoSite.isFormAuthentication());
                            ssoCreds.setFormUserField(ssoSite.getFormUserField());
                            ssoCreds.setFormPwdField(ssoSite.getFormPwdField());
                            ssoCreds.setUsername(pwc.getUserName());
                            ssoCreds.setPassword(pwc.getPassword());

                            ssoCredsList.add(ssoCreds);
                        }
                    }
                }
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

        return ssoCredsList;
    }

    public static SSOSite getBestSubjectSSOSiteByURL(SSOManager ssoManager, String url) {
        SSOSite ssoSite = null;

        try
        {
            ssoSite = ssoManager.getSiteByUrl(url);
        }
        catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.warn("Failed to retrieve sso site by url: " + url, e);
            }
            else
            {
                log.warn("Failed to retrieve sso site by url: '{}'. {}", url, e.toString());
            }
        }

        if (ssoSite == null)
        {
            try
            {
                Subject subject = JSSubject.getSubject(AccessController.getContext());
                Collection<SSOSite> ssoSites = ssoManager.getSitesForSubject(subject);

                if (ssoSites != null)
                {
                    ssoSite = getBestMatchedSSOSite(ssoSites, URI.create(url));
                }
            }
            catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.warn("Failed to retrieve sso site.", e);
                }
                else
                {
                    log.warn("Failed to retrieve sso site. {}", e.toString());
                }
            }
        }

        return ssoSite;
    }

    public static SSOSite getBestMatchedSSOSite(Collection<SSOSite> ssoSites, final URI uri)
    {
        if (ssoSites == null || ssoSites.isEmpty())
        {
            return null;
        }

        SSOSite ssoSite = null;
        URI siteURI = null;

        List<SSOSite> candidates = new ArrayList<SSOSite>();

        for (SSOSite candidate : ssoSites)
        {
            siteURI = URI.create(candidate.getURL());

            if (isEachFromSameOrigin(uri, siteURI))
            {
                candidates.add(candidate);
            }
        }

        if (!candidates.isEmpty())
        {
            if (candidates.size() == 1)
            {
                ssoSite = candidates.get(0);
            }
            else
            {
                Collections.sort(candidates, new Comparator<SSOSite>() {
                    @Override
                    public int compare(SSOSite site1, SSOSite site2)
                    {
                        URI uri1 = URI.create(site1.getURL());
                        URI uri2 = URI.create(site2.getURL());
                        int di1 = StringUtils.indexOfDifference(uri.getPath(), uri1.getPath());
                        int di2 = StringUtils.indexOfDifference(uri.getPath(), uri2.getPath());

                        if (di1 == di2)
                        {
                            return 0;
                        }
                        else if (di1 < di2)
                        {
                            return -1;
                        }
                        else
                        {
                            return 1;
                        }
                    }
                });

                ssoSite = candidates.get(candidates.size() - 1);
            }
        }

        return ssoSite;
    }

    public static JetspeedSSOSiteCredentials getBestMatchedSSOSiteCrendentials(Collection<JetspeedSSOSiteCredentials> ssoSiteCredsList, final URI uri)
    {
        if (ssoSiteCredsList == null || ssoSiteCredsList.isEmpty())
        {
            return null;
        }

        JetspeedSSOSiteCredentials ssoSiteCreds = null;
        URI siteURI = null;

        List<JetspeedSSOSiteCredentials> candidates = new ArrayList<JetspeedSSOSiteCredentials>();

        for (JetspeedSSOSiteCredentials candidate : ssoSiteCredsList)
        {
            siteURI = candidate.getBaseURI();

            if (isEachFromSameOrigin(uri, siteURI))
            {
                candidates.add(candidate);
            }
        }

        if (!candidates.isEmpty())
        {
            if (candidates.size() == 1)
            {
                ssoSiteCreds = candidates.get(0);
            }
            else
            {
                Collections.sort(candidates, new Comparator<JetspeedSSOSiteCredentials>() {
                    @Override
                    public int compare(JetspeedSSOSiteCredentials creds1, JetspeedSSOSiteCredentials creds2)
                    {
                        URI uri1 = creds1.getBaseURI();
                        URI uri2 = creds2.getBaseURI();
                        int di1 = StringUtils.indexOfDifference(uri.getPath(), uri1.getPath());
                        int di2 = StringUtils.indexOfDifference(uri.getPath(), uri2.getPath());

                        if (di1 == di2)
                        {
                            return 0;
                        }
                        else if (di1 < di2)
                        {
                            return -1;
                        }
                        else
                        {
                            return 1;
                        }
                    }
                });

                ssoSiteCreds = candidates.get(candidates.size() - 1);
            }
        }

        return ssoSiteCreds;
    }

    private static boolean isEachFromSameOrigin(URI uri1, URI uri2)
    {
        if (StringUtils.equals(uri1.getScheme(), uri2.getScheme()) &&
                        StringUtils.equals(uri1.getHost(), uri2.getHost()) &&
                        uri1.getPort() == uri2.getPort())
        {
            return true;
        }

        return false;
    }

}
