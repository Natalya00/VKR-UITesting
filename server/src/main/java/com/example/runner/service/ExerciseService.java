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

@Service
public class ExerciseService {
    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

    private final ObjectMapper objectMapper;
    private final Map<String, ModuleData> modules = new ConcurrentHashMap<>();

    public ExerciseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources("classpath:data/exercises/module-*.json");

        log.info("ExerciseService init: найдено {} файлов модулей", resources.length);
        for (Resource resource : resources) {
            log.info("Доступен модульный JSON: {}", resource.getFilename());
        }
    }

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

    public List<ModuleData> getAllModules() {
        return new ArrayList<>(modules.values());
    }

    public ModuleData getModule(String moduleId) {
        return loadModule(moduleId);
    }

    public List<BlockData> getBlocks(String moduleId) {
        ModuleData module = loadModule(moduleId);
        return module != null ? module.getBlocks() : List.of();
    }

    public BlockData getBlock(String moduleId, String blockId) {
        ModuleData module = loadModule(moduleId);
        if (module == null) return null;

        return module.getBlocks().stream()
            .filter(b -> b.getBlockId().equals(blockId))
            .findFirst()
            .orElse(null);
    }

    public List<ExerciseData> getExercises(String moduleId) {
        return getBlocks(moduleId).stream()
            .flatMap(b -> b.getExercises().stream())
            .toList();
    }

    public List<ExerciseData> getExercisesByBlock(String moduleId, String blockId) {
        BlockData block = getBlock(moduleId, blockId);
        return block != null ? block.getExercises() : List.of();
    }

    public List<ExerciseData> getExercisesByDifficulty(String moduleId, Difficulty difficulty) {
        return getBlocks(moduleId).stream()
            .filter(b -> b.getDifficulty() == difficulty)
            .flatMap(b -> b.getExercises().stream())
            .toList();
    }

    public List<ExerciseData> getExercises(String moduleId, String blockId, Difficulty difficulty) {
        BlockData block = getBlock(moduleId, blockId);
        if (block == null) return List.of();

        return block.getExercises().stream()
            .filter(e -> e.getDifficulty() == difficulty)
            .toList();
    }

    public ExerciseData getExercise(String moduleId, String exerciseId) {
        return getExercises(moduleId).stream()
            .filter(e -> e.getId().equals(exerciseId))
            .findFirst()
            .orElse(null);
    }

    public Set<Difficulty> getAvailableDifficulties(String moduleId, String blockId) {
        BlockData block = getBlock(moduleId, blockId);
        if (block == null) return Set.of();

        return block.getExercises().stream()
            .map(ExerciseData::getDifficulty)
            .collect(Collectors.toSet());
    }
}
