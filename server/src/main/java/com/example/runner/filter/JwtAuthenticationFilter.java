package com.example.runner.filter;

import com.example.runner.repository.RevokedTokenRepository;
import com.example.runner.security.JwtTokenProvider;
import com.example.runner.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Фильтр для обработки JWT аутентификации
 * 
 * Основные функции:
 * - Извлекает access токен из HTTP-only cookies
 * - Валидирует токен и проверяет черный список
 * - Устанавливает контекст безопасности Spring Security
 * - Обрабатывает ошибки без прерывания цепочки фильтров
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Имя cookie с access токеном */
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    /** Провайдер для работы с JWT токенами */
    private final JwtTokenProvider jwtTokenProvider;
    
    /** Репозиторий для проверки отозванных токенов */
    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Конструктор фильтра
     * @param jwtTokenProvider провайдер JWT токенов
     * @param revokedTokenRepository репозиторий отозванных токенов
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   RevokedTokenRepository revokedTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Основная логика фильтра
     * 
     * 1. Извлекает access токен из cookies
     * 2. Валидирует токен (подпись, срок действия)
     * 3. Проверяет отсутствие в черном списке
     * 4. Извлекает данные пользователя из токена
     * 5. Устанавливает контекст аутентификации
     * 
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = getAccessTokenFromCookie(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                String tokenHash = jwtTokenProvider.hashToken(accessToken);
                if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
                String email = jwtTokenProvider.getEmailFromToken(accessToken);

                UserDetailsImpl userDetails = new UserDetailsImpl(userId, email, "");

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает access токен из HTTP cookies
     * @param request HTTP запрос
     * @return access токен или null если не найден
     */
    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}