package com.example.runner.repository;

import com.example.runner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 * Предоставляет методы для поиска пользователей по email
 * и проверки существования учетных записей.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Находит пользователя по email адресу */
    Optional<User> findByEmail(String email);

    /** Проверяет существование пользователя с указанным email */
    boolean existsByEmail(String email);
}
