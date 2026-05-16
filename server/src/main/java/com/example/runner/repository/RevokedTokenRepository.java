package com.example.runner.repository;

import com.example.runner.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с отозванными JWT токенами
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    /** Находит отозванный токен по его хешу */
    RevokedToken findByTokenHash(String tokenHash);

    /** Проверяет существование отозванного токена по хешу */
    boolean existsByTokenHash(String tokenHash);
}
