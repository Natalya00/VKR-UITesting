package com.example.runner.service;

import com.example.runner.dto.progress.ExerciseProgressResponse;
import com.example.runner.dto.progress.ModuleProgressResponse;
import com.example.runner.dto.progress.UserProgressResponse;
import com.example.runner.model.ExerciseAttempt;
import com.example.runner.model.User;
import com.example.runner.model.UserProgress;
import com.example.runner.repository.ExerciseAttemptRepository;
import com.example.runner.repository.UserProgressRepository;
import com.example.runner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressService Tests")
class ProgressServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private ExerciseAttemptRepository exerciseAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressService progressService;

    private User testUser;
    private UserProgress testProgress;
    private ExerciseAttempt testAttempt;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        testProgress = UserProgress.builder()
                .id(1L)
                .user(testUser)
                .moduleId("module-2")
                .exerciseId("exercise-1")
                .isCompleted(false)
                .attemptsCount(0)
                .build();

        testAttempt = ExerciseAttempt.builder()
                .id(1L)
                .user(testUser)
                .moduleId("module-2")
                .exerciseId("exercise-1")
                .codeSnapshot("test code")
                .isSuccess(true)
                .attemptsCount(1)
                .build();
    }

    @Test
    @DisplayName("Должен успешно отмечать упражнение как выполненное")
    void shouldMarkExerciseCompleteSuccessfully() {
        Long userId = 1L;
        String moduleId = "module-2";
        String exerciseId = "exercise-1";
        String codeSnapshot = "test code";
        Boolean isSuccess = true;
        String errorMessage = null;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId))
                .thenReturn(Optional.of(testProgress));
        when(exerciseAttemptRepository.save(any(ExerciseAttempt.class))).thenReturn(testAttempt);
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userId, moduleId, exerciseId, codeSnapshot, isSuccess, errorMessage
        );

        assertNotNull(response);
        assertEquals(exerciseId, response.getExerciseId());
        assertEquals(moduleId, response.getModuleId());
        assertTrue(response.getIsCompleted());
        assertEquals(1, response.getAttemptsCount());

        verify(userRepository).findById(userId);
        verify(exerciseAttemptRepository).save(any(ExerciseAttempt.class));
        verify(userProgressRepository).findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId);
        verify(userProgressRepository).save(any(UserProgress.class));
    }

    @Test
    @DisplayName("Должен создавать новый прогресс если его не существует")
    void shouldCreateNewProgressIfNotExists() {
        Long userId = 1L;
        String moduleId = "module-2";
        String exerciseId = "exercise-1";
        String codeSnapshot = "test code";
        Boolean isSuccess = true;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId))
                .thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);
        when(exerciseAttemptRepository.save(any(ExerciseAttempt.class))).thenReturn(testAttempt);

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userId, moduleId, exerciseId, codeSnapshot, isSuccess, null
        );

        assertNotNull(response);
        assertEquals(exerciseId, response.getExerciseId());
        assertEquals(moduleId, response.getModuleId());

        verify(userProgressRepository, times(1)).save(any(UserProgress.class)); 
        verify(exerciseAttemptRepository).save(any(ExerciseAttempt.class));
    }

    @Test
    @DisplayName("Должен обрабатывать неуспешную попытку")
    void shouldHandleUnsuccessfulAttempt() {
        Long userId = 1L;
        String moduleId = "module-2";
        String exerciseId = "exercise-1";
        String codeSnapshot = "test code";
        Boolean isSuccess = false;
        String errorMessage = "Compilation error";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId))
                .thenReturn(Optional.of(testProgress));
        when(exerciseAttemptRepository.save(any(ExerciseAttempt.class))).thenReturn(testAttempt);
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userId, moduleId, exerciseId, codeSnapshot, isSuccess, errorMessage
        );

        assertNotNull(response);
        assertEquals(exerciseId, response.getExerciseId());
        assertEquals(moduleId, response.getModuleId());
        assertFalse(response.getIsCompleted());
        assertEquals(1, response.getAttemptsCount());

        verify(exerciseAttemptRepository).save(argThat(attempt -> 
            !attempt.getIsSuccess() && "Compilation error".equals(attempt.getErrorMessage())
        ));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение если пользователь не найден")
    void shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> progressService.markExerciseComplete(userId, "module-2", "exercise-1", "code", true, null)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(exerciseAttemptRepository, never()).save(any());
        verify(userProgressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен не отмечать как выполненное уже выполненное упражнение")
    void shouldNotMarkAlreadyCompletedExercise() {
        testProgress.markCompleted(); 
        
        Long userId = 1L;
        String moduleId = "module-2";
        String exerciseId = "exercise-1";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId))
                .thenReturn(Optional.of(testProgress));
        when(exerciseAttemptRepository.save(any(ExerciseAttempt.class))).thenReturn(testAttempt);
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userId, moduleId, exerciseId, "code", true, null
        );

        assertTrue(response.getIsCompleted());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    @DisplayName("Должен получать прогресс пользователя")
    void shouldGetUserProgress() {
        Long userId = 1L;
        
        UserProgress progress1 = UserProgress.builder()
                .user(testUser)
                .moduleId("module-1")
                .exerciseId("exercise-1")
                .isCompleted(true)
                .attemptsCount(2)
                .completedAt(LocalDateTime.now())
                .build();
                
        UserProgress progress2 = UserProgress.builder()
                .user(testUser)
                .moduleId("module-2")
                .exerciseId("exercise-1")
                .isCompleted(false)
                .attemptsCount(1)
                .build();

        when(userProgressRepository.findByUserId(userId))
                .thenReturn(List.of(progress1, progress2));

        UserProgressResponse response = progressService.getUserProgress(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getTotalCompleted());
        assertEquals(196, response.getTotalExercises()); 
        assertTrue(response.getTotalPercentage() > 0);
        assertEquals(3, response.getModules().size());

        verify(userProgressRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Должен получать прогресс модуля")
    void shouldGetModuleProgress() {
        Long userId = 1L;
        String moduleId = "module-2";
        
        UserProgress progress1 = UserProgress.builder()
                .user(testUser)
                .moduleId(moduleId)
                .exerciseId("exercise-1")
                .isCompleted(true)
                .attemptsCount(2)
                .completedAt(LocalDateTime.now())
                .build();
                
        UserProgress progress2 = UserProgress.builder()
                .user(testUser)
                .moduleId(moduleId)
                .exerciseId("exercise-2")
                .isCompleted(false)
                .attemptsCount(1)
                .build();

        when(userProgressRepository.findByUserIdAndModuleId(userId, moduleId))
                .thenReturn(List.of(progress1, progress2));

        ModuleProgressResponse response = progressService.getModuleProgress(userId, moduleId);

        assertNotNull(response);
        assertEquals(moduleId, response.getModuleId());
        assertEquals("Взаимодействие с элементами", response.getModuleTitle());
        assertEquals(90, response.getTotalExercises());
        assertEquals(1, response.getCompletedExercises());
        assertTrue(response.getPercentage() > 0);
        assertEquals(2, response.getExercises().size());

        verify(userProgressRepository).findByUserIdAndModuleId(userId, moduleId);
    }

    @Test
    @DisplayName("Должен обрабатывать пустой прогресс пользователя")
    void shouldHandleEmptyUserProgress() {
        Long userId = 1L;
        when(userProgressRepository.findByUserId(userId)).thenReturn(List.of());

        UserProgressResponse response = progressService.getUserProgress(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(0, response.getTotalCompleted());
        assertEquals(196, response.getTotalExercises());
        assertEquals(0.0, response.getTotalPercentage());
        assertEquals(3, response.getModules().size());

        response.getModules().forEach(module -> {
            assertEquals(0, module.getCompletedExercises());
            assertEquals(0.0, module.getPercentage());
            assertTrue(module.getExercises().isEmpty());
        });
    }

    @Test
    @DisplayName("Должен обрабатывать неизвестный модуль")
    void shouldHandleUnknownModule() {
        Long userId = 1L;
        String unknownModuleId = "unknown-module";

        when(userProgressRepository.findByUserIdAndModuleId(userId, unknownModuleId))
                .thenReturn(List.of());

        ModuleProgressResponse response = progressService.getModuleProgress(userId, unknownModuleId);

        assertNotNull(response);
        assertEquals(unknownModuleId, response.getModuleId());
        assertEquals(unknownModuleId, response.getModuleTitle()); 
        assertEquals(0, response.getTotalExercises());
        assertEquals(0, response.getCompletedExercises());
        assertEquals(0.0, response.getPercentage());
        assertTrue(response.getExercises().isEmpty());
    }

    @Test
    @DisplayName("Должен правильно вычислять проценты")
    void shouldCalculatePercentagesCorrectly() {
        Long userId = 1L;
        
        List<UserProgress> moduleProgress = List.of(
            createCompletedProgress("exercise-1"),
            createCompletedProgress("exercise-2"),
            createCompletedProgress("exercise-3"),
            createCompletedProgress("exercise-4"),
            createCompletedProgress("exercise-5"),
            createCompletedProgress("exercise-6"),
            createCompletedProgress("exercise-7"),
            createCompletedProgress("exercise-8"),
            createCompletedProgress("exercise-9"),
            createCompletedProgress("exercise-10")
        );

        when(userProgressRepository.findByUserIdAndModuleId(userId, "module-2"))
                .thenReturn(moduleProgress);

        ModuleProgressResponse response = progressService.getModuleProgress(userId, "module-2");

        assertEquals(10, response.getCompletedExercises());
        assertEquals(90, response.getTotalExercises());
        assertEquals(11.11, response.getPercentage(), 0.01); 
    }

    @Test
    @DisplayName("Должен правильно обрабатывать названия модулей")
    void shouldHandleModuleTitlesCorrectly() {
        Long userId = 1L;
        
        String[] moduleIds = {"module-1", "module-2", "module-3"};
        String[] expectedTitles = {"XPath", "Взаимодействие с элементами", "Page Object Model (POM)"};

        for (int i = 0; i < moduleIds.length; i++) {
            when(userProgressRepository.findByUserIdAndModuleId(userId, moduleIds[i]))
                    .thenReturn(List.of());

            ModuleProgressResponse response = progressService.getModuleProgress(userId, moduleIds[i]);

            assertEquals(expectedTitles[i], response.getModuleTitle());
        }
    }

    @Test
    @DisplayName("Должен увеличивать счетчик попыток")
    void shouldIncrementAttemptsCount() {
        testProgress.setAttemptsCount(5); 
        
        Long userId = 1L;
        String moduleId = "module-2";
        String exerciseId = "exercise-1";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId))
                .thenReturn(Optional.of(testProgress));
        when(exerciseAttemptRepository.save(any(ExerciseAttempt.class))).thenReturn(testAttempt);
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userId, moduleId, exerciseId, "code", false, "error"
        );

        assertEquals(6, response.getAttemptsCount()); 
    }

    private UserProgress createCompletedProgress(String exerciseId) {
        return UserProgress.builder()
                .user(testUser)
                .moduleId("module-2")
                .exerciseId(exerciseId)
                .isCompleted(true)
                .attemptsCount(1)
                .completedAt(LocalDateTime.now())
                .build();
    }
}