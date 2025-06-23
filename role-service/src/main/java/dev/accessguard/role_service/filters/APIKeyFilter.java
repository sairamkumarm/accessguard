package dev.accessguard.role_service.filters;

import dev.accessguard.role_service.services.APICheckService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logging.TenantContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class APIKeyFilter extends OncePerRequestFilter {

    private final APICheckService apiCheckService;

    public APIKeyFilter(APICheckService apiCheckService){
        this.apiCheckService = apiCheckService;
    }

    private static final List<String> SKIPPED_PATH_SUBSTRINGS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"  // Add more as needed
    );


    private boolean shouldSkipFilter(String path) {
        return SKIPPED_PATH_SUBSTRINGS.stream().anyMatch(path::contains);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String rawAPIKey = request.getHeader("X-API-KEY");
        String tenantName = request.getHeader("X-TENANT-NAME");
        if(shouldSkipFilter(request.getRequestURI())){
            filterChain.doFilter(request,response);
            return;
        }
        if (rawAPIKey == null || tenantName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing tenantName or API Key");
            return;
        }
        if(rawAPIKey.isEmpty() || tenantName.isEmpty()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing tenantName or API Key");
            return;
        }
        TenantContext.setTenant(tenantName);
        if(apiCheckService.checkAPIKeyExistence(tenantName,rawAPIKey)){
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(tenantName,null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_TENANT")));
            SecurityContextHolder.getContext().setAuthentication(token);
            filterChain.doFilter(request,response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Invalid Credentials");
            return;
        }
    }
}
