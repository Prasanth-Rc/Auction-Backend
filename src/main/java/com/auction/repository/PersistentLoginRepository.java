package com.auction.repository;

import com.auction.entity.PersistentLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersistentLoginRepository extends JpaRepository<PersistentLogin, String> {
}