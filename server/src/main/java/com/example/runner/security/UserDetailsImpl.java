package com.example.runner.security;

import com.example.runner.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Реализация UserDetails для Spring Security
 * Представляет аутентифицированного пользователя в системе безопасности.
 * Содержит основную информацию о пользователе и его правах.
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /** Уникальный идентификатор пользователя */
    private Long id;
    
    /** Email адрес пользователя (используется как username) */
    private String email;
    
    /** Хэш пароля пользователя */
    private String password;

    /**
     * Возвращает права пользователя
     * @return коллекция с одной ролью ROLE_USER
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_USER");
    }

    /**
     * Возвращает имя пользователя (в данном случае - email)
     * @return email адрес пользователя
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Проверяет, не истек ли срок действия аккаунта
     * @return всегда true (аккаунты не истекают)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирован ли аккаунт
     * @return всегда true (аккаунты не блокируются)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истек ли срок действия пароля
     * @return всегда true (пароли не истекают)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, активен ли аккаунт
     * @return всегда true (все аккаунты активны)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Создает экземпляр UserDetailsImpl из модели User
     * @param user модель пользователя
     * @return экземпляр UserDetailsImpl с данными пользователя
     */
    public static UserDetailsImpl fromUser(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash()
        );
    }
}
