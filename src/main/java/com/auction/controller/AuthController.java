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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

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
    @GetMapping("/public/csrf")
    public ResponseEntity<?> csrf(CsrfToken token) {
        return ResponseEntity.ok(Map.of(
                "token", token.getToken()
        ));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        LoginService.AuthResponse user =
                loginService.findAuthResponseByEmail(authentication.getName());

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "user", user
        ));
    }

    /** SIGNUP — save credentials to DB. */
    @PostMapping("/signup")
    public ResponseEntity<RegistrationService.AuthResponse> signup(
            @Valid @RequestBody RegistrationService.SignupRequest req) {
        return ResponseEntity.ok(registrationService.signup(req));
    }

    /** GOOGLE SIGN-IN. */
    @PostMapping("/auth/google")
    public ResponseEntity<?> google(
            @Valid @RequestBody GoogleLoginRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        try {
            // 1. Verify Google ID token
            var gu = googleService.verify(req.idToken());

            // 2. Check whether email already exists
            User u = userRepository.findByEmailNative(gu.email())
                    .orElse(null);

            // 3. DO NOT INSERT a new user
            if (u == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "message", "This email is not registered with us. Please sign up first."
                        ));
            }

            // 4. Existing user only - load authorities
            UserDetails details =
                    loginService.loadUserByUsername(u.getEmail());

            // 5. Create authentication
            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            details,
                            null,
                            details.getAuthorities()
                    );

            // 6. Store authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);

            securityContextRepository.saveContext(
                    SecurityContextHolder.getContext(),
                    httpReq,
                    httpRes
            );

            // 7. Remember me if selected
            if (req.rememberMe()) {
                rememberMeServices.loginSuccess(
                        httpReq,
                        httpRes,
                        auth
                );
            }

            // 8. Return existing user details
            var body =
                    loginService.findAuthResponseByEmail(u.getEmail());

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "user", body
                    )
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Google sign-in failed. Please try again."
                    ));
        }
    }
}