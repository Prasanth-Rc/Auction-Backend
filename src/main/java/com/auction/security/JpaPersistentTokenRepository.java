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

    @Override
    @Transactional
    public void createNewToken(PersistentRememberMeToken token) {

        PersistentLogin login = PersistentLogin.builder()
                .series(token.getSeries())
                .username(token.getUsername())
                .token(token.getTokenValue())
                .lastUsed(
                        LocalDateTime.ofInstant(
                                token.getDate().toInstant(),
                                ZoneId.systemDefault()
                        )
                )
                .build();

        repo.save(login);
    }

    @Override
    @Transactional
    public void updateToken(
            String series,
            String tokenValue,
            Date lastUsed) {

        repo.findById(series).ifPresent(login -> {

            login.setToken(tokenValue);

            login.setLastUsed(
                    LocalDateTime.ofInstant(
                            lastUsed.toInstant(),
                            ZoneId.systemDefault()
                    )
            );
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PersistentRememberMeToken getTokenForSeries(
            String seriesId) {

        return repo.findById(seriesId)
                .map(login ->
                        new PersistentRememberMeToken(
                                login.getUsername(),
                                login.getSeries(),
                                login.getToken(),
                                Date.from(
                                        login.getLastUsed()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()
                                )
                        )
                )
                .orElse(null);
    }

    @Override
    @Transactional
    public void removeUserTokens(String username) {

        repo.deleteByUsername(username);
    }
}