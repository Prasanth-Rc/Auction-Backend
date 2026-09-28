package com.auction.config;

import com.auction.security.*;
import com.auction.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final JsonAuthSuccessHandler successHandler;
    private final JsonAuthFailureHandler failureHandler;
    private final JsonLogoutSuccessHandler logoutSuccessHandler;
    private final PersistentTokenBasedRememberMeServices rememberMeServices;

    @Bean public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean public DaoAuthenticationProvider authProvider() {
        var p = new DaoAuthenticationProvider(loginService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean public CookieCsrfTokenRepository csrfTokenRepository() {
        var repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookiePath("/");
        repo.setCookieName("XSRF-TOKEN");
        repo.setHeaderName("X-XSRF-TOKEN");
        return repo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        var handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");

        http
                .authenticationProvider(authProvider())
                .csrf(c -> c
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(handler)
                        .ignoringRequestMatchers("/api/public/**"))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .rememberMe(r -> r.rememberMeServices(rememberMeServices).key("auction-remember-key"))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/public/**", "/api/signup", "/api/auth/google").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(l -> l
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .httpBasic(b -> b.disable());

        return http.build();
    }
}