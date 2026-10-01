package com.auction.config;

import com.auction.security.JpaPersistentTokenRepository;
import com.auction.service.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

@Configuration
public class RememberMeConfig {

    @Bean
    public PersistentTokenBasedRememberMeServices rememberMeServices(
            LoginService loginService,
            JpaPersistentTokenRepository persistentTokenRepository) {

        var service = new PersistentTokenBasedRememberMeServices(
                "auction-remember-key",
                loginService,
                persistentTokenRepository
        );

        service.setTokenValiditySeconds(14 * 24 * 3600);

        service.setParameter("remember-me");

        service.setCookieName("AUCTION_REMEMBER_ME");

        service.setUseSecureCookie(true);

        return service;
    }
}