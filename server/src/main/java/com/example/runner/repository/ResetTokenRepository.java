package com.example.runner.repository;

import com.example.runner.model.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
    boolean existsByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}

