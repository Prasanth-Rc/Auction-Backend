package com.auction.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final PersistentTokenBasedRememberMeServices rememberMeServices;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse res,
                                Authentication auth) throws IOException {

        if (auth != null) rememberMeServices.logout(req, res, auth);

        for (String name : new String[]{"JSESSIONID", "XSRF-TOKEN", "AUCTION_REMEMBER_ME"}) {
            Cookie c = new Cookie(name, null);
            c.setPath("/");
            c.setMaxAge(0);
            res.addCookie(c);
        }

        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), Map.of("success", true, "message", "Logged out"));
    }
}