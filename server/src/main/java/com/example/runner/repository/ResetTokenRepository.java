package com.example.runner.repository;

import com.example.runner.model.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Репозиторий для работы с токенами сброса пароля
 * Предоставляет методы для поиска, проверки и удаления
 * токенов сброса пароля с учетом их статуса и срока действия.
 */
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    
    /**
     * Находит действительный токен по его строковому представлению
     * @param token строковое представление токена
     * @param now текущая дата и время для проверки срока действия
     * @return Optional с токеном, если он найден и действителен
     */
    Optional<ResetToken> findByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
    
    /**
     * Проверяет существование действительного токена
     * @param token строковое представление токена
     * @param now текущая дата и время для проверки срока действия
     * @return true, если действительный токен существует
     */
    boolean existsByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);
    
    /**
     * Удаляет все токены, срок действия которых истек
     * @param expiryDate дата, до которой токены считаются устаревшими
     */
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}

