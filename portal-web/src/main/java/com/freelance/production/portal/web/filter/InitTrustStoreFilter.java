package com.freelance.production.portal.web.filter;

import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by yang on 2017/8/9.
 */
public class InitTrustStoreFilter implements Filter {

    private String trustStoreFilePath;

    public void init(FilterConfig filterConfig) throws ServletException {
        if (StringUtils.isNotEmpty(trustStoreFilePath)) {
            System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }

    public void setTrustStoreFilePath(String trustStoreFilePath) {
        this.trustStoreFilePath = trustStoreFilePath;
    }
}
