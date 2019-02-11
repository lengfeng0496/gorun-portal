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

<%--
WARNING:

  This example is provided to demonstrate the SSO feature of SSOReverseProxyIFrame portlet.
  This is not for production use!
  You should consult with other examples if you want to implement an authentication.

--%>

<%@ page language="java"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%!
private Map userInfoMap = new HashMap();

public void jspInit()
{
    userInfoMap.put("manager", "manager");
    userInfoMap.put("admin", "admin");
}

private boolean authenticate(HttpServletRequest req)
{
    try
    {
        String username = req.getParameter("user");
        String password = req.getParameter("pass");

        if (username != null && password != null)
        {
            Base64 decoder = new Base64();
            username = new String(decoder.decode(username));
            password = new String(decoder.decode(password));

            if (password.equals(userInfoMap.get(username)))
            {
                req.getSession().setAttribute("examples.formauth.username", username);
                return true;
            }
        }
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
    
    return false;
}
%>
<%
if (authenticate(request))
{
    response.sendRedirect("urlauth_success.jsp");
}
else
{
%>
<html>
<head>
<title>URL Authentication Example</title>
</head>
<body>

<p>
This example is provided to demonstrate the SSO feature of SSOIFramePortlet.<br/>
<strong>This is not for production use! You should consult with other examples <br/>if you want to implement an authentication.</strong> 
</p>
<hr/>

<p>Please access with a URL having proper authentication information.</p>

</body>
</html>
<%
}
%>
