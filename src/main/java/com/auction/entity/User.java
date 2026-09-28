package com.auction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "app_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(nullable = false, unique = true)       private String mobile;
    @Column(nullable = false, unique = true)       private String email;
    @Column(nullable = false)                      private String password;   // BCrypt hash
    @Column(name = "company_name")                 private String companyName;
    private String representing;
    @Column(name = "gst_number")                   private String gstNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    @Column(name = "google_subject")               private String googleSubject;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles;
}