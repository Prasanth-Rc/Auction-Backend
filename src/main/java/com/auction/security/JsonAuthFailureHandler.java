package com.auction.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JsonAuthFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(),
                Map.of("success", false, "message", "Invalid username or password"));
    }
}