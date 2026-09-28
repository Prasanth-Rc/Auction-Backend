package com.auction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "persistent_logins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersistentLogin {

    @Id @Column(length = 64, nullable = false)      private String series;
    @Column(length = 64, nullable = false)          private String username;
    @Column(length = 64, nullable = false)          private String token;
    @Column(name = "last_used", nullable = false)   private LocalDateTime lastUsed;
}