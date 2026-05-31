package com.elcom.adminconsolebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

public class ApiPrefixFilter implements Filter {

    private String uriPrefix;

    public ApiPrefixFilter(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestURI = req.getRequestURI();

        // Nếu URL bắt đầu bằng /adminconsole-api, tiến hành loại bỏ prefix này
        if (requestURI.startsWith("/" + uriPrefix)) {
            String newURI = requestURI.substring(uriPrefix.length() + 1);
            if (newURI.isEmpty()) {
                newURI = "/";
            }

            // Tạo wrapper để ghi đè phương thức getRequestURI() và getServletPath()
            String finalNewURI = newURI;
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(req) {
                @Override
                public String getRequestURI() {
                    return finalNewURI;
                }

                @Override
                public String getServletPath() {
                    return finalNewURI;
                }
            };

            chain.doFilter(wrappedRequest, response);
        } else {
            // Nếu không có prefix, xử lý bình thường
            chain.doFilter(request, response);
        }
    }
}