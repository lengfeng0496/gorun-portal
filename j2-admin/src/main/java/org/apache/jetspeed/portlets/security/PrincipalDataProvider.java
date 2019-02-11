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

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.security.Group;
import org.apache.jetspeed.security.GroupManager;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.JetspeedPrincipalManager;
import org.apache.jetspeed.security.Role;
import org.apache.jetspeed.security.RoleManager;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.UserSubjectPrincipal;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PrincipalDataProvider extends SortableDataProvider<JetspeedPrincipal> implements IDataProvider<JetspeedPrincipal>
{
    
    private static final long serialVersionUID = -5676288154198656171L;

    public enum OrderBy
    {
        NAME_ASC, NAME_DESC
    }

    private OrderBy order = OrderBy.NAME_ASC;
    private List<JetspeedPrincipal> principalList = new ArrayList<JetspeedPrincipal>();
    private boolean roleFilter = false;
    private boolean groupFilter = false;
    private String filteredRole;
    private String filteredGroup;
    private UserSubjectPrincipal currentUser;

    public PrincipalDataProvider(UserSubjectPrincipal currentUser, JetspeedPrincipalManager manager, String search)
    {
        this.currentUser = currentUser;
        refresh(manager, search);
    }

    public PrincipalDataProvider(UserSubjectPrincipal currentUser, JetspeedPrincipalManager manager, JetspeedPrincipalManager roleOrGroupManager, String search, String filteredRoleOrGroup, boolean isRoleFilter, RoleManager roleManager)
    {
        this.currentUser = currentUser;
        if (isRoleFilter) { // can be either role or group filter
            this.filteredRole = filteredRoleOrGroup;
            this.filteredGroup = "";
            this.roleFilter = true;
            this.groupFilter = false;
        }
        else {
            this.filteredGroup = filteredRoleOrGroup;
            this.filteredRole = "";
            this.roleFilter = false;
            this.groupFilter = true;
        }
        refresh(manager, roleOrGroupManager, search, roleManager);
    }

    public Iterator<? extends JetspeedPrincipal> iterator(int first, int count)
    {
        int last = first + count;
        if (last > principalList.size())
            last = principalList.size() - 1;
        if (last < 0)
            last = 0;
        return principalList.subList(first, last).iterator();
    }

    public IModel model(JetspeedPrincipal object)
    {
        return new Model((Serializable) object);
    }

    public int size()
    {
        return principalList.size();
    }

    public OrderBy getOrderBy()
    {
        return order;
    }

    public void setOrderBy(OrderBy orderBy)
    {
        this.order = orderBy;
    }

    public void sort()
    {
        Collections.sort(principalList, new PrincipalComparator(getOrderBy()));
    }

    public void refresh(JetspeedPrincipalManager manager, String searchString)
    {
        principalList = (List<JetspeedPrincipal>) manager.getPrincipals(searchString);
    }

    public void refresh(JetspeedPrincipalManager manager, JetspeedPrincipalManager assocManager, String searchString, RoleManager roleManager)
    {
        if (roleFilter && !StringUtils.isEmpty(filteredRole))
        {
            if (assocManager.getPrincipal(filteredRole) == null)
            {
                principalList = new ArrayList<JetspeedPrincipal>();
            }
            else
            {
                principalList = new ArrayList<JetspeedPrincipal>();                
                List<JetspeedPrincipal> localList = (List<JetspeedPrincipal>) manager.getPrincipals(searchString);
                for (JetspeedPrincipal principal : localList)
                {
                    try
                    {
                        List<Role> roles = ((RoleManager) assocManager).getRolesForUser(principal.getName());
                        for (Role role : roles)
                        {
                            if (role.getName().equals(filteredRole))
                            {
                                principalList.add(principal);
                                break;
                            }
                        }
                    }
                    catch (SecurityException e)
                    {
                        principalList = new ArrayList<JetspeedPrincipal>();
                    }
                }
            }
        }
        else if (groupFilter && !StringUtils.isEmpty(filteredGroup))
        {
            if (!filteredGroup.equals(JetspeedPrincipalManagementPortlet.GROUP_MANAGER_PREFIX_FLAG) && assocManager.getPrincipal(filteredGroup) == null)
            {
                principalList = new ArrayList<JetspeedPrincipal>();
            }
            else
            {
                principalList = new ArrayList<JetspeedPrincipal>();
                if (filteredGroup.equals(JetspeedPrincipalManagementPortlet.GROUP_MANAGER_PREFIX_FLAG) && roleManager != null) {
                    try {
                        List<Role> roles = roleManager.getRolesForUser(currentUser.getName());
                        for (Role role : roles)
                        {
                            if (role.getName().startsWith(JetspeedPrincipalManagementPortlet.GROUP_MANAGER_PREFIX))
                            {
                                String targetGroup = role.getName().substring(JetspeedPrincipalManagementPortlet.GROUP_MANAGER_PREFIX.length());
                                // filter users by target group
                                List<JetspeedPrincipal> localList = (List<JetspeedPrincipal>) manager.getPrincipals(searchString);
                                for (JetspeedPrincipal principal : localList) {
                                    List<Group> groups = ((GroupManager) assocManager).getGroupsForUser(principal.getName());
                                    for (Group group : groups) {
                                        if (group.getName().equals(targetGroup)) {
                                            principalList.add(principal);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SecurityException e) {
                        principalList = new ArrayList<JetspeedPrincipal>();
                    }
                }
                else {
                    List<JetspeedPrincipal> localList = (List<JetspeedPrincipal>) manager.getPrincipals(searchString);
                    for (JetspeedPrincipal principal : localList) {
                        try {
                            List<Group> groups = ((GroupManager) assocManager).getGroupsForUser(principal.getName());
                            for (Group group : groups) {
                                if (group.getName().equals(filteredGroup)) {
                                    principalList.add(principal);
                                    break;
                                }
                            }
                        } catch (SecurityException e) {
                            principalList = new ArrayList<JetspeedPrincipal>();
                        }
                    }
                }
            }
        }
        else
        {
            principalList = (List<JetspeedPrincipal>) manager.getPrincipals(searchString);
        }
    }

    public class PrincipalComparator implements Comparator<JetspeedPrincipal>
    {
        OrderBy orderBy;

        public PrincipalComparator(OrderBy order)
        {
            this.orderBy = order;
        }

        public int compare(JetspeedPrincipal principal, JetspeedPrincipal otherPrincipal)
        {
            if (orderBy == OrderBy.NAME_ASC)
            {
                return principal.getName().compareToIgnoreCase(otherPrincipal.getName());
            }
            else
            {
                return otherPrincipal.getName().compareToIgnoreCase(principal.getName());
            }
        }
    }
}
