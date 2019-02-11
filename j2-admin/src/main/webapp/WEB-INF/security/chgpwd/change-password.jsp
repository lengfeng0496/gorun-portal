<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@page import="org.apache.jetspeed.portlets.security.ChangePasswordPortlet"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.security.resources.ChgPwdResources" />
<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>
<c_rt:set var="responsive" value='${requestContext.getAttribute("org.apache.jetspeed.theme.responsive")}'/>
<c:choose>
  <c:when test='${responsive}'>
    <c:choose>
      <c:when test="${pageContext.request.userPrincipal != null}">

        <c:set var="whyKey"><%=ChangePasswordPortlet.WHY%></c:set>
        <c:set var="why" value="${requestScope[whyKey]}"/>
        <c:set var="requiredKey"><%=ChangePasswordPortlet.REQUIRED%></c:set>
        <c:set var="required" value="${requestScope[requiredKey]}"/>
        <c:set var="errorMeagesKey"><%=ChangePasswordPortlet.ERROR_MESSAGES%></c:set>
        <c:set var="errorMessages" value="${requestScope[errorMessagesKey]}"/>

        <c:if test="${why != null}">
          <i><c:out value="${why}"/></i>
          <br/>
        </c:if>
        <c:if test="${errorMessages != null}">
          <ul>
            <c:forEach items="${errorMessages}" var="error">
              <li style="color:red"><c:out value="${error}"/></li>
            </c:forEach>
          </ul>
        </c:if>

        <c_rt:set var="passwordChangedKey" value="<%=ChangePasswordPortlet.PASSWORD_CHANGED%>"/>
        <c:set var="p" value="${requestScope[passwordChangedKey]}"/>
        <c:if test="${requestScope[passwordChangedKey] != null}">
          <br>
          <i><fmt:message key="chgpwd.message.passwordChanged"/></i>
          <br><br>
        </c:if>

        <form class="form-horizontal" method="POST" action='<portlet:actionURL/>'>
          <div class="form-group">
            <label for="<%=ChangePasswordPortlet.CURRENT_PASSWORD%>" class="col-sm-3 control-label"><fmt:message key="chgpwd.label.currentPassword"/></label>
            <div class="col-sm-9"><input type="password" class="form-control" name="<%=ChangePasswordPortlet.CURRENT_PASSWORD%>"></div>
          </div>
          <div class="form-group">
            <label for="<%=ChangePasswordPortlet.NEW_PASSWORD%>" class="col-sm-3 control-label"><fmt:message key="chgpwd.label.newPassword"/></label>
            <div class="col-sm-9"><input type="password" class="form-control" name="<%=ChangePasswordPortlet.NEW_PASSWORD%>"></div>
          </div>
          <div class="form-group">
            <label for="<%=ChangePasswordPortlet.NEW_PASSWORD_AGAIN%>" class="col-sm-3 control-label"><fmt:message key="chgpwd.label.newPasswordAgain"/></label>
            <div class="col-sm-9"><input type="password" class="form-control" name="<%=ChangePasswordPortlet.NEW_PASSWORD_AGAIN%>"></div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label">&nbsp;</label>
            <div class="col-sm-9">
              <button type="submit" class="btn btn-default"><fmt:message key="chgpwd.label.save"/></button>
              <c:if test="${why != null}">
                <c:choose>
                  <c:when test="${required == null}">
                    &nbsp;&nbsp;
                    <c_rt:set var="cancelItem" value="<%=ChangePasswordPortlet.CANCELLED%>"/>
                    <input type="checkbox" style="display:none" name="<c:out value="${cancelItem}"/>">
                    <button type="submit"
                            onClick="this.form.<c:out value="${cancelItem}"/>.checked=true"><fmt:message key="chgpwd.label.cancel" /></button>
                  </c:when>
                  <c:otherwise>
                    <br/><br/>
                    <a href='<c:url context="${portalContextPathInUrlTag}" value="/login/logout"/>'><fmt:message key="chgpwd.label.Logout"/></a>
                  </c:otherwise>
                </c:choose>
              </c:if>
            </div>
          </div>
        </form>
      </c:when>
      <c:otherwise>
        <fmt:message key="chgpwd.error.notLoggedOn"/><br>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <div class="portlet-section-text">
      <c:choose>
        <c:when test="${pageContext.request.userPrincipal != null}">

          <c:set var="whyKey"><%=ChangePasswordPortlet.WHY%></c:set>
          <c:set var="why" value="${requestScope[whyKey]}"/>
          <c:set var="requiredKey"><%=ChangePasswordPortlet.REQUIRED%></c:set>
          <c:set var="required" value="${requestScope[requiredKey]}"/>
          <c:set var="errorMe
    agesKey"><%=ChangePasswordPortlet.ERROR_MESSAGES%></c:set>
          <c:set var="errorMessages" value="${requestScope[errorMessagesKey]}"/>

          <c:if test="${why != null}">
            <i><c:out value="${why}"/></i>
            <br/>
          </c:if>
          <c:if test="${errorMessages != null}">
            <ul>
              <c:forEach items="${errorMessages}" var="error">
                <li style="color:red"><c:out value="${error}"/></li>
              </c:forEach>
            </ul>
          </c:if>

          <c_rt:set var="passwordChangedKey" value="<%=ChangePasswordPortlet.PASSWORD_CHANGED%>"/>
          <c:set var="p" value="${requestScope[passwordChangedKey]}"/>
          <c:if test="${requestScope[passwordChangedKey] != null}">
            <br>
            <i><fmt:message key="chgpwd.message.passwordChanged"/></i>
            <br><br>
          </c:if>

          <form method="POST" action='<portlet:actionURL/>'>
            <table border="0">
              <tr>
                <td><fmt:message key="chgpwd.label.currentPassword"/></td>
                <td><input type="password" size="30" name="<%=ChangePasswordPortlet.CURRENT_PASSWORD%>"></td>
              </tr>
              <tr>
                <td><fmt:message key="chgpwd.label.newPassword"/></td>
                <td><input type="password" size="30" name="<%=ChangePasswordPortlet.NEW_PASSWORD%>"></td>
              </tr>
              <tr>
                <td><fmt:message key="chgpwd.label.newPasswordAgain"/></td>
                <td><input type="password" size="30" name="<%=ChangePasswordPortlet.NEW_PASSWORD_AGAIN%>"></td>
              </tr>
              <tr>
                <td colspan="2">
                  <input type="submit" value="<fmt:message key="chgpwd.label.save"/>">
                  <c:if test="${why != null}">
                    <c:choose>
                      <c:when test="${required == null}">
                        &nbsp;&nbsp;
                        <c_rt:set var="cancelItem" value="<%=ChangePasswordPortlet.CANCELLED%>"/>
                        <input type="checkbox" style="display:none" name="<c:out value="${cancelItem}"/>">
                        <input type="submit"
                               value="<fmt:message key="chgpwd.label.cancel"/>"
                               onClick="this.form.<c:out value="${cancelItem}"/>.checked=true">
                      </c:when>
                      <c:otherwise>
                        <br/><br/>
                        <a href='<c:url context="${portalContextPathInUrlTag}" value="/login/logout"/>'><fmt:message key="chgpwd.label.Logout"/></a>
                      </c:otherwise>
                    </c:choose>
                  </c:if>
                </td>
              </tr>
            </table>
          </form>
        </c:when>
        <c:otherwise>
          <fmt:message key="chgpwd.error.notLoggedOn"/><br>
        </c:otherwise>
      </c:choose>
    </div>
  </c:otherwise>
</c:choose>
