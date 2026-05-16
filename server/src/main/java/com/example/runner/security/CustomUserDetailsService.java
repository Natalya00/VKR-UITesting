package com.example.runner.security;

import com.example.runner.model.User;
import com.example.runner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Пользовательский сервис для Spring Security
 * Отвечает за загрузку данных пользователя для процесса аутентификации.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Репозиторий для поиска пользователей */
    private final UserRepository userRepository;

    /**
     * Загружает пользователя по email адресу
     * @param email email адрес пользователя (используется как username)
     * @return объект UserDetails для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));

        return UserDetailsImpl.fromUser(user);
    }
}
