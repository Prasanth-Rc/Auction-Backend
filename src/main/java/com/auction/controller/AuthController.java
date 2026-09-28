package com.auction.controller;

import com.auction.entity.User;
import com.auction.repository.UserRepository;
import com.auction.service.GoogleService;
import com.auction.service.LoginService;
import com.auction.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final GoogleService googleService;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final PersistentTokenBasedRememberMeServices rememberMeServices;

    public record GoogleLoginRequest(@NotBlank String idToken, boolean rememberMe) {}

    /** Primes the XSRF-TOKEN cookie — called by Login.jsx on mount. */
    @GetMapping("/public/auth/me")
    public ResponseEntity<?> primeCsrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) token.getToken();
        return ResponseEntity.status(401).body(Map.of("authenticated", false));
    }

    /** SIGNUP — save credentials to DB. */
    @PostMapping("/signup")
    public ResponseEntity<RegistrationService.AuthResponse> signup(
            @Valid @RequestBody RegistrationService.SignupRequest req) {
        return ResponseEntity.ok(registrationService.signup(req));
    }

    /** GOOGLE SIGN-IN. */
    @PostMapping("/auth/google")
    public ResponseEntity<?> google(@Valid @RequestBody GoogleLoginRequest req,
                                    HttpServletRequest httpReq,
                                    HttpServletResponse httpRes) {

        var gu = googleService.verify(req.idToken());

        User u = userRepository.findByEmailNative(gu.email()).orElseGet(() -> {
            var created = registrationService.registerGoogleUser(gu.sub(), gu.email(), gu.name());
            return userRepository.findById(created.userId()).orElseThrow();
        });

        UserDetails details = loginService.loadUserByUsername(u.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpReq, httpRes);

        if (req.rememberMe()) {
            rememberMeServices.loginSuccess(httpReq, httpRes, auth);
        }

        var body = loginService.findAuthResponseByEmail(u.getEmail());
        return ResponseEntity.ok(Map.of("success", true, "user", body));
    }
}