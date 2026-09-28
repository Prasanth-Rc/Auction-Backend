package com.auction.security;

import com.auction.entity.PersistentLogin;
import com.auction.repository.PersistentLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JpaPersistentTokenRepository implements PersistentTokenRepository {

    private final PersistentLoginRepository repo;

    @Override @Transactional
    public void createNewToken(PersistentRememberMeToken t) {
        repo.save(PersistentLogin.builder()
                .series(t.getSeries()).username(t.getUsername())
                .token(t.getTokenValue())
                .lastUsed(LocalDateTime.ofInstant(t.getDate().toInstant(), ZoneId.systemDefault()))
                .build());
    }

    @Override @Transactional
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        repo.findById(series).ifPresent(p -> {
            p.setToken(tokenValue);
            p.setLastUsed(LocalDateTime.ofInstant(lastUsed.toInstant(), ZoneId.systemDefault()));
        });
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return repo.findById(seriesId)
                .map(p -> new PersistentRememberMeToken(
                        p.getUsername(), p.getSeries(), p.getToken(),
                        Date.from(p.getLastUsed().atZone(ZoneId.systemDefault()).toInstant())))
                .orElse(null);
    }

    @Override @Transactional
    public void removeUserTokens(String username) {
        repo.findAll().stream()
                .filter(p -> p.getUsername().equals(username))
                .forEach(repo::delete);
    }
}