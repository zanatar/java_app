package ru.tsystems.tchallenge.service.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class AuthenticationFilter extends GenericFilterBean {

    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTH_TOKEN_PREFIX = "BEARER ";
    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final UserAuthentication authentication = authenticateByTokenPayloadIfPossible((HttpServletRequest) request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }


    private UserAuthentication authenticateByTokenPayloadIfPossible(final HttpServletRequest request) {
        final String header = request.getHeader(AUTH_HEADER_NAME);
        if (header == null || header.isEmpty() || !header.startsWith(AUTH_TOKEN_PREFIX)) {
            return null;
        }
        final String authToken = header.split(" ")[1];
        try {
            return authenticationManager.authenticateByToken(authToken);
        } catch (final Exception exception) {
            return null;
        }
    }

}
