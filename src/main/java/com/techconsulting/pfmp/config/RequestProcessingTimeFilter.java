package com.techconsulting.pfmp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@Slf4j
public class RequestProcessingTimeFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String requestId = req.getRequestId(); // Ensure Jakarta Servlet 6.0+

        try {
            filterChain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus(); // Capture HTTP response status

            log.info("Request [{}] {} processed in {} ms | Status: {}",
                    requestId, request.getRequestURI(), duration, status);
        }
    }
}
