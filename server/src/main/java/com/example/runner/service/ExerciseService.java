package com.example.runner.service;

import com.example.runner.model.BlockData;
import com.example.runner.model.Difficulty;
import com.example.runner.model.ExerciseData;
import com.example.runner.model.ModuleData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для управления упражнениями и модулями тренажера
 * 
 * Предоставляет функциональность для:
 * - Загрузки и кэширования данных о модулях из файловой системы
 * - Управления иерархической структурой модулей, блоков и упражнений
 * - Фильтрации упражнений по сложности и блокам
 * - Получения метаданных о доступных уровнях сложности
 * - Динамической загрузки упражнений из JSON файлов
 */
@Service
public class ExerciseService {
    /** Логгер для отслеживания операций загрузки упражнений */
    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

    /** Jackson ObjectMapper для десериализации JSON файлов */
    private final ObjectMapper objectMapper;
    
    /** Кэш загруженных модулей */
    private final Map<String, ModuleData> modules = new ConcurrentHashMap<>();

    /**
     * Конструктор сервиса
     * @param objectMapper Jackson ObjectMapper для работы с JSON
     */
    public ExerciseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Инициализация сервиса после создания бина
     * @throws IOException если возникают проблемы с доступом к файловой системе
     */
    @PostConstruct
    public void init() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources("classpath:data/exercises/module-*.json");

        log.info("ExerciseService init: найдено {} файлов модулей", resources.length);
        for (Resource resource : resources) {
            log.info("Доступен модульный JSON: {}", resource.getFilename());
        }
    }

    /**
     * Загружает упражнения для конкретного блока модуля
     * 
     * Процесс загрузки:
     * - Формирует паттерн поиска файлов упражнений
     * - Сканирует файловую систему на предмет соответствующих JSON файлов
     * - Десериализует каждый файл в объект ExerciseData
     * - Сортирует упражнения по порядковому номеру
     * 
     * @param moduleId идентификатор модуля (например, "module-1")
     * @param blockId идентификатор блока (например, "block1")
     * @return список упражнений блока, отсортированный по порядку
     */
    private List<ExerciseData> loadBlockExercises(String moduleId, String blockId) {
        String pattern = String.format("classpath:data/exercises/%s/%s/exercise-*.json", moduleId, blockId);
        var resolver = new PathMatchingResourcePatternResolver();

        log.info("Загрузка упражнений для модуля={}, блок={}", moduleId, blockId);

        try {
            Resource[] resources = resolver.getResources(pattern);
            log.info("Найдено ресурсов: {}", resources.length);

            if (resources.length == 0) {
                log.error("Для модуля {} и блока {} не найдено отдельных файлов упражнений по шаблону {}", moduleId, blockId, pattern);
                return List.of();
            }

            List<ExerciseData> exercises = new ArrayList<>();
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    ExerciseData exercise = objectMapper.readValue(is, ExerciseData.class);
                    exercises.add(exercise);
                    log.info("Загружено упражнение: {}", exercise.getId());
                }
            }

            exercises.sort(Comparator.comparingInt(e -> Optional.ofNullable(e.getOrder()).orElse(0)));
            log.info("Загружено {} упражнений для модуля {} блока {}", exercises.size(), moduleId, blockId);
            return exercises;
        } catch (IOException e) {
            log.error("Ошибка загрузки упражнений для модуля {} блока {}", moduleId, blockId, e);
            return List.of();
        }
    }

    /**
     * Загружает данные модуля с полной иерархией блоков и упражнений
     * 
     * Алгоритм загрузки:
     * - Проверяет кэш на наличие уже загруженного модуля
     * - Загружает метаданные модуля из _index.json
     * - Для каждого блока загружает его метаданные и упражнения
     * - Вычисляет количество упражнений в каждом блоке
     * - Кэширует полностью загруженный модуль
     * - Обрабатывает ошибки и возвращает null при неудаче
     * 
     * @param moduleId идентификатор модуля для загрузки
     * @return полностью загруженные данные модуля или null при ошибке
     */
    private ModuleData loadModule(String moduleId) {
        ModuleData cached = modules.get(moduleId);
        if (cached != null) {
            return cached;
        }

        String indexPath = String.format("data/exercises/%s/_index.json", moduleId);
        ClassPathResource indexResource = new ClassPathResource(indexPath);
        if (indexResource.exists()) {
            try (InputStream is = indexResource.getInputStream()) {
                ModuleData moduleMeta = objectMapper.readValue(is, ModuleData.class);
                List<BlockData> fullBlocks = new ArrayList<>();

                if (moduleMeta.getBlocks() != null) {
                    for (BlockData blockMeta : moduleMeta.getBlocks()) {
                        String blockIndexPath = String.format("data/exercises/%s/%s/_index.json", moduleId, blockMeta.getBlockId());
                        ClassPathResource blockIndexResource = new ClassPathResource(blockIndexPath);

                        BlockData block;
                        if (blockIndexResource.exists()) {
                            try (InputStream bis = blockIndexResource.getInputStream()) {
                                block = objectMapper.readValue(bis, BlockData.class);
                            }
                        } else {
                            block = blockMeta;
                        }

                        List<ExerciseData> exercises = loadBlockExercises(moduleId, block.getBlockId());
                        block.setExercises(exercises);
                        block.setExerciseCount(exercises.size());
                        fullBlocks.add(block);
                    }
                }

                moduleMeta.setBlocks(fullBlocks);
                modules.put(moduleId, moduleMeta);
                log.info("Модуль {} загружен из новой иерархии", moduleId);
                return moduleMeta;
            } catch (IOException e) {
                log.error("Ошибка загрузки модуля {} из новой структуры", moduleId, e);
            }
        } else {
            log.error("Для модуля {} не найден _index.json по пути {}", moduleId, indexPath);
        }
        return null;
    }

    /**
     * Получает список всех доступных модулей
     * @return список всех загруженных модулей из кэша
     */
    public List<ModuleData> getAllModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Получает данные конкретного модуля
     * @param moduleId идентификатор модуля
     * @return данные модуля или null если модуль не найден
     */
    public ModuleData getModule(String moduleId) {
        return loadModule(moduleId);
    }

    /**
     * Получает все блоки упражнений для указанного модуля
     * @param moduleId идентификатор модуля
     * @return список блоков модуля или пустой список если модуль не найден
     */
    public List<BlockData> getBlocks(String moduleId) {
        ModuleData module = loadModule(moduleId);
        return module != null ? module.getBlocks() : List.of();
    }

    /**
     * Получает данные конкретного блока упражнений
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return данные блока или null если блок не найден
     */
    public BlockData getBlock(String moduleId, String blockId) {
        ModuleData module = loadModule(moduleId);
        if (module == null) return null;

        return module.getBlocks().stream()
            .filter(b -> b.getBlockId().equals(blockId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Получает все упражнения модуля (из всех блоков)
     * @param moduleId идентификатор модуля
     * @return плоский список всех упражнений модуля
     */
    public List<ExerciseData> getExercises(String moduleId) {
        return getBlocks(moduleId).stream()
            .flatMap(b -> b.getExercises().stream())
            .toList();
    }

    /**
     * Получает упражнения конкретного блока
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return список упражнений блока или пустой список если блок не найден
     */
    public List<ExerciseData> getExercisesByBlock(String moduleId, String blockId) {
        BlockData block = getBlock(moduleId, blockId);
        return block != null ? block.getExercises() : List.of();
    }

    /**
     * Получает упражнения модуля с фильтрацией по уровню сложности
     * @param moduleId идентификатор модуля
     * @param difficulty уровень сложности (EASY, MEDIUM, HARD)
     * @return список упражнений указанной сложности
     */
    public List<ExerciseData> getExercisesByDifficulty(String moduleId, Difficulty difficulty) {
        return getBlocks(moduleId).stream()
            .filter(b -> b.getDifficulty() == difficulty)
            .flatMap(b -> b.getExercises().stream())
            .toList();
    }

    /**
     * Получает упражнения конкретного блока с фильтрацией по сложности
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @param difficulty уровень сложности для фильтрации
     * @return список упражнений блока указанной сложности
     */
    public List<ExerciseData> getExercises(String moduleId, String blockId, Difficulty difficulty) {
        BlockData block = getBlock(moduleId, blockId);
        if (block == null) return List.of();

        return block.getExercises().stream()
            .filter(e -> e.getDifficulty() == difficulty)
            .toList();
    }

    /**
     * Получает конкретное упражнение по его идентификатору
     * @param moduleId идентификатор модуля
     * @param exerciseId идентификатор упражнения
     * @return данные упражнения или null если упражнение не найдено
     */
    public ExerciseData getExercise(String moduleId, String exerciseId) {
        return getExercises(moduleId).stream()
            .filter(e -> e.getId().equals(exerciseId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Получает множество доступных уровней сложности для блока
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return множество доступных уровней сложности или пустое множество если блок не найден
     */
    public Set<Difficulty> getAvailableDifficulties(String moduleId, String blockId) {
        BlockData block = getBlock(moduleId, blockId);
        if (block == null) return Set.of();

        return block.getExercises().stream()
            .map(ExerciseData::getDifficulty)
            .collect(Collectors.toSet());
    }
}
