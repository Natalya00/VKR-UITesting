package com.example.runner.service;

import com.example.runner.dto.auth.AuthResponse;
import com.example.runner.dto.auth.LoginRequest;
import com.example.runner.dto.auth.RegisterRequest;
import com.example.runner.model.RevokedToken;
import com.example.runner.model.User;
import com.example.runner.repository.RevokedTokenRepository;
import com.example.runner.repository.UserRepository;
import com.example.runner.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
@DisplayName("Должен успешно регистрировать нового пользователя")
void shouldRegisterNewUserSuccessfully() {
    User savedUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .build();

    when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser); 
    when(jwtTokenProvider.generateAccessToken(1L, "test@example.com"))
            .thenReturn("accessToken");
    when(jwtTokenProvider.generateRefreshToken(1L, "test@example.com"))
            .thenReturn("refreshToken");

    AuthResponse result = authService.register(registerRequest, httpServletResponse);

    assertNotNull(result);
    assertEquals(1L, result.getUser().getId());
    assertEquals("test@example.com", result.getUser().getEmail());
    assertEquals("accessToken", result.getAccessToken());
    assertEquals("refreshToken", result.getRefreshToken());
}

    @Test
    @DisplayName("Должен выбрасывать исключение при регистрации существующего email")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(registerRequest, httpServletResponse)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен успешно выполнять вход пользователя")
    void shouldLoginUserSuccessfully() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail()))
                .thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(testUser.getId(), testUser.getEmail()))
                .thenReturn("refreshToken");

        AuthResponse result = authService.login(loginRequest, httpServletResponse);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testUser.getEmail(), result.getUser().getEmail());
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при неверных учетных данных")
    void shouldThrowExceptionForInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest, httpServletResponse)
        );

        assertEquals("Неверный email или пароль", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение если пользователь не найден после аутентификации")
    void shouldThrowExceptionWhenUserNotFoundAfterAuthentication() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest, httpServletResponse)
        );

        assertEquals("Неверный email или пароль", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("Должен успешно выполнять выход пользователя")
    void shouldLogoutUserSuccessfully() {
        String accessToken = "validAccessToken";
        String refreshToken = "validRefreshToken";
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(accessToken)).thenReturn(testUser.getId());
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(testUser.getId());
        when(jwtTokenProvider.hashToken(accessToken)).thenReturn("hashedAccessToken");
        when(jwtTokenProvider.hashToken(refreshToken)).thenReturn("hashedRefreshToken");
        when(jwtTokenProvider.getExpiryDateFromToken(accessToken)).thenReturn(expiryDate);
        when(jwtTokenProvider.getExpiryDateFromToken(refreshToken)).thenReturn(expiryDate);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        authService.logout(accessToken, refreshToken, httpServletResponse);

        verify(jwtTokenProvider, times(2)).validateToken(anyString());
        verify(jwtTokenProvider, times(2)).getUserIdFromToken(anyString());
        verify(jwtTokenProvider, times(2)).hashToken(anyString());
        verify(jwtTokenProvider, times(2)).getExpiryDateFromToken(anyString());
        verify(revokedTokenRepository, times(2)).save(any(RevokedToken.class));
    }

    @Test
    @DisplayName("Должен обрабатывать выход с невалидными токенами")
    void shouldHandleLogoutWithInvalidTokens() {
        String accessToken = "invalidAccessToken";
        String refreshToken = "invalidRefreshToken";

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        authService.logout(accessToken, refreshToken, httpServletResponse);

        verify(jwtTokenProvider, times(2)).validateToken(anyString());
        verify(revokedTokenRepository, never()).save(any(RevokedToken.class));
    }

    @Test
    @DisplayName("Должен успешно обновлять токены")
    void shouldRefreshTokensSuccessfully() {
        String refreshToken = "validRefreshToken";
        String tokenHash = "hashedRefreshToken";

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.hashToken(refreshToken)).thenReturn(tokenHash);
        when(revokedTokenRepository.existsByTokenHash(tokenHash)).thenReturn(false);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(testUser.getId(), testUser.getEmail()))
                .thenReturn("newAccessToken");
        when(jwtTokenProvider.generateRefreshToken(testUser.getId(), testUser.getEmail()))
                .thenReturn("newRefreshToken");

        AuthResponse result = authService.refreshTokens(refreshToken, httpServletResponse);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(testUser.getEmail(), result.getUser().getEmail());
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).hashToken(refreshToken);
        verify(revokedTokenRepository).existsByTokenHash(tokenHash);
        verify(jwtTokenProvider).isRefreshToken(refreshToken);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение для невалидного refresh токена")
    void shouldThrowExceptionForInvalidRefreshToken() {
        String refreshToken = "invalidRefreshToken";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshTokens(refreshToken, httpServletResponse)
        );

        assertEquals("Неверный refresh token", exception.getMessage());
        verify(jwtTokenProvider).validateToken(refreshToken);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение для отозванного refresh токена")
    void shouldThrowExceptionForRevokedRefreshToken() {
        String refreshToken = "revokedRefreshToken";
        String tokenHash = "hashedRefreshToken";

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.hashToken(refreshToken)).thenReturn(tokenHash);
        when(revokedTokenRepository.existsByTokenHash(tokenHash)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshTokens(refreshToken, httpServletResponse)
        );

        assertEquals("Refresh token был отозван", exception.getMessage());
        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).hashToken(refreshToken);
        verify(revokedTokenRepository).existsByTokenHash(tokenHash);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение если токен не является refresh токеном")
    void shouldThrowExceptionIfTokenIsNotRefreshToken() {
        String accessToken = "accessToken";
        String tokenHash = "hashedAccessToken";

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.hashToken(accessToken)).thenReturn(tokenHash);
        when(revokedTokenRepository.existsByTokenHash(tokenHash)).thenReturn(false);
        when(jwtTokenProvider.isRefreshToken(accessToken)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshTokens(accessToken, httpServletResponse)
        );

        assertEquals("Это не refresh token", exception.getMessage());
        verify(jwtTokenProvider).isRefreshToken(accessToken);
    }

    @Test
    @DisplayName("Должен успешно получать текущего пользователя")
    void shouldGetCurrentUserSuccessfully() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        AuthResponse.UserInfo result = authService.getCurrentUser(testUser.getId());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение если пользователь не найден")
    void shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.getCurrentUser(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository).findById(userId);
    }
}