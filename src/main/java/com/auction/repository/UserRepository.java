package com.auction.repository;

import com.auction.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /* ─────────── SIGNUP ─────────── */

    @Query(value = "SELECT EXISTS(SELECT 1 FROM auction.app_user WHERE LOWER(email) = LOWER(:email))",
            nativeQuery = true)
    boolean existsByEmailNative(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM auction.app_user WHERE mobile = :mobile)",
            nativeQuery = true)
    boolean existsByMobileNative(@Param("mobile") String mobile);

    @Modifying @Transactional
    @Query(value = """
            INSERT INTO auction.app_user
              (full_name, mobile, email, password, company_name, representing,
               gst_number, address, city, state, pincode, google_subject)
            VALUES
              (:fullName, :mobile, :email, :password, :companyName, :representing,
               :gstNumber, :address, :city, :state, :pincode, :googleSubject)
            """, nativeQuery = true)
    void insertUserNative(@Param("fullName") String fullName,
                          @Param("mobile") String mobile,
                          @Param("email") String email,
                          @Param("password") String password,
                          @Param("companyName") String companyName,
                          @Param("representing") String representing,
                          @Param("gstNumber") String gstNumber,
                          @Param("address") String address,
                          @Param("city") String city,
                          @Param("state") String state,
                          @Param("pincode") String pincode,
                          @Param("googleSubject") String googleSubject);

    @Query(value = """
            SELECT id FROM auction.app_user WHERE LOWER(email) = LOWER(:email) LIMIT 1
            """, nativeQuery = true)
    Long findIdByEmailNative(@Param("email") String email);

    @Modifying @Transactional
    @Query(value = "INSERT INTO auction.user_roles (user_id, role) VALUES (:userId, :role)",
            nativeQuery = true)
    void insertRoleNative(@Param("userId") Long userId, @Param("role") String role);

    /* ─────────── LOGIN ─────────── */

    @Query(value = """
            SELECT * FROM auction.app_user
            WHERE LOWER(email) = LOWER(:login) OR mobile = :login
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findByEmailOrMobileNative(@Param("login") String login);

    @Query(value = """
            SELECT * FROM auction.app_user WHERE LOWER(email) = LOWER(:email) LIMIT 1
            """, nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);
}