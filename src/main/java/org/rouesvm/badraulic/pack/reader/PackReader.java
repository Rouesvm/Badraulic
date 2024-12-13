package org.rouesvm.badraulic.pack.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.rouesvm.badraulic.pack.reader.BlockReader.*;
import static org.rouesvm.badraulic.pack.reader.ItemReader.getItemJsonFiles;
import static org.rouesvm.badraulic.pack.reader.ItemReader.getItemTextureName;

public class PackReader {
    public static Map<String, Object> getMaterialInstances() {
        Map<String, Object> geysers = new HashMap<>();

        try {
            List<Path> jsonFiles = getBlockJsonFiles();
            if (!jsonFiles.isEmpty()) {
                jsonFiles.forEach(file -> {
                    try {
                        convertState(file.toFile(), geysers);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return geysers;
    }

    public static Map<String, String> getBlockTextures() throws IOException {
        String outputDir = "polymer/resource_pack_unzipped";

        Map<String, String> geysers = new HashMap<>();

        getBlockJsonFiles(outputDir).forEach(file -> {
            try {
                geysers.putAll(getBlockTextureName(file.toFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return geysers;
    }

    public static Map<String, String> getItemTextures() throws IOException {
        String outputDir = "polymer/resource_pack_unzipped";

        Map<String, String> geysers = new HashMap<>();

        getItemJsonFiles(outputDir).forEach(file -> {
            try {
                Map<String, String> textures = getItemTextureName(file.toFile());
                geysers.putAll(textures);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return geysers;
    }

    public static void unzip(String zipFilePath, String outputDir) throws IOException {
        Path outputPath = Path.of(outputDir);
        Files.createDirectories(outputPath);

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Path.of(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = outputPath.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    public static List<Path> findJsonFiles(String rootDir, String targetSubfolder) throws IOException {
        List<Path> jsonFiles = new ArrayList<>();
        Path startPath = Path.of(rootDir);

        Stream<Path> files = Files.walk(startPath);
        try (files) {
            files.filter(path -> {
                        Path relativePath = startPath.relativize(path);
                        return relativePath.toString().contains(targetSubfolder) &&
                                path.toString().endsWith(".json");
                    }).forEach(jsonFiles::add);
        }

        return jsonFiles;
    }

    public static List<Path> findJsonFiles(String rootDir) throws IOException {
        List<Path> jsonFiles = new ArrayList<>();
        Path startPath = Path.of(rootDir);

        Stream<Path> files = Files.walk(startPath);
        try (files) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(jsonFiles::add);
        }

        return jsonFiles;
    }

    public static void unzipPolymerPack() throws IOException {
        String zipFilePath = "polymer/resource_pack.zip";
        String outputDir = "polymer/resource_pack_unzipped";
        unzip(zipFilePath, outputDir);
    }

    public static List<Path> getCustomModelDataJsonFiles(String outputDir) throws IOException {
        return findJsonFiles(outputDir, "polymer");
    }
}