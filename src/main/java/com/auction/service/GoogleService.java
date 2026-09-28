package com.auction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client.id:}")
    private String googleClientId;

    public record GoogleUser(String sub, String email, String name) {}

    public GoogleUser verify(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null)
                throw new IllegalStateException("Google token verification failed");

            JsonNode node = objectMapper.readTree(resp.getBody());

            String aud = node.path("aud").asText();
            if (!googleClientId.isBlank() && !googleClientId.equals(aud))
                throw new IllegalStateException("Google token audience mismatch");

            String sub   = node.path("sub").asText();
            String email = node.path("email").asText();
            String name  = node.path("name").asText(null);

            if (sub.isBlank() || email.isBlank())
                throw new IllegalStateException("Google token missing subject/email");

            return new GoogleUser(sub, email, name);

        } catch (Exception e) {
            throw new IllegalStateException("Google sign-in failed: " + e.getMessage(), e);
        }
    }
}