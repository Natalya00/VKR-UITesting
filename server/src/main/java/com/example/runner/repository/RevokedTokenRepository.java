package com.example.runner.repository;

import com.example.runner.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    RevokedToken findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);
}
