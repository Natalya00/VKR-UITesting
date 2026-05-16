package com.example.runner.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Утилита для разбиения JSON-файла модуля на отдельные файлы.
 */
public class Module1Splitter {

    /** Идентификатор модуля для обработки */
    private static final String MODULE_ID = "module-1";

    /**
     * Основной метод для запуска процесса разбиения модуля.
     * @param args аргументы командной строки
     * @throws IOException при ошибках работы с файловой системой
     */
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        Path baseDir = Path.of("server", "src", "main", "resources", "data", "exercises");
        Path moduleJson = baseDir.resolve(MODULE_ID + ".json");

        if (!Files.exists(moduleJson)) {
            System.err.println("Файл " + moduleJson + " не найден. Проверьте путь.");
            return;
        }

        JsonNode root = mapper.readTree(moduleJson.toFile());
        ArrayNode blocks = (ArrayNode) root.path("blocks");

        if (blocks.isEmpty()) {
            System.err.println("В module-1.json не найден массив blocks.");
            return;
        }

        Path moduleDir = baseDir.resolve(MODULE_ID);
        Files.createDirectories(moduleDir);

        com.fasterxml.jackson.databind.node.ObjectNode moduleIndex = mapper.createObjectNode();
        moduleIndex.put("moduleId", root.path("moduleId").asText());
        moduleIndex.put("title", root.path("title").asText());

        ArrayNode moduleBlocks = mapper.createArrayNode();

        for (JsonNode blockNode : blocks) {
            String blockId = blockNode.path("blockId").asText();
            String title = blockNode.path("title").asText();
            String description = blockNode.path("description").asText();
            String difficulty = blockNode.path("difficulty").asText();

            ArrayNode exercises = (ArrayNode) blockNode.path("exercises");
            int exerciseCount = exercises.size();

            com.fasterxml.jackson.databind.node.ObjectNode blockMeta = mapper.createObjectNode();
            blockMeta.put("blockId", blockId);
            blockMeta.put("title", title);
            blockMeta.put("difficulty", difficulty);
            blockMeta.put("description", description);
            blockMeta.put("exerciseCount", exerciseCount);
            moduleBlocks.add(blockMeta);
        }

        moduleIndex.set("blocks", moduleBlocks);
        writeJson(writer, moduleDir.resolve("_index.json"), moduleIndex);

        for (JsonNode blockNode : blocks) {
            String blockId = blockNode.path("blockId").asText();
            String title = blockNode.path("title").asText();
            String description = blockNode.path("description").asText();
            String difficulty = blockNode.path("difficulty").asText();

            ArrayNode exercises = (ArrayNode) blockNode.path("exercises");
            int exerciseCount = exercises.size();

            Path blockDir = moduleDir.resolve(blockId);
            Files.createDirectories(blockDir);

            com.fasterxml.jackson.databind.node.ObjectNode blockIndex = mapper.createObjectNode();
            blockIndex.put("blockId", blockId);
            blockIndex.put("title", title);
            blockIndex.put("difficulty", difficulty);
            blockIndex.put("description", description);
            blockIndex.put("exerciseCount", exerciseCount);
            writeJson(writer, blockDir.resolve("_index.json"), blockIndex);

            int idx = 1;
            for (JsonNode exNode : exercises) {
                String id = exNode.path("id").asText();

                String fileName = "exercise-" + idx + ".json";
                if (!id.isEmpty()) {
                    int lastDash = id.lastIndexOf('-');
                    if (lastDash != -1) {
                        String tail = id.substring(lastDash + 1);
                        if (tail.chars().allMatch(Character::isDigit)) {
                            fileName = "exercise-" + tail + ".json";
                        }
                    }
                }

                writeJson(writer, blockDir.resolve(fileName), exNode);
                idx++;
            }
        }

        Path backup = baseDir.resolve(MODULE_ID + ".json.backup");
        if (!Files.exists(backup)) {
            Files.copy(moduleJson, backup);
        }

        System.out.println("Разбиение модуля завершено.");
        System.out.println("Метафайлы и упражнения записаны в " + moduleDir.toAbsolutePath());
    }

    /**
     * Записывает JSON-узел в файл с форматированием.
     * @param writer объект для записи JSON с форматированием
     * @param path путь к файлу для записи
     * @param node JSON-узел для записи
     * @throws IOException при ошибках записи файла
     */
    private static void writeJson(ObjectWriter writer, Path path, JsonNode node) throws IOException {
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            Files.createDirectories(file.toPath().getParent());
        }
        String json = writer.writeValueAsString(node);
        Files.writeString(path, json + System.lineSeparator(), StandardCharsets.UTF_8);
    }
}

