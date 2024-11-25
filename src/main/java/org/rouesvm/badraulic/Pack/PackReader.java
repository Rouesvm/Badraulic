package org.rouesvm.badraulic.Pack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackReader {
    public static Map<String, Object> getMaterialInstances() {
        String zipFilePath = "polymer/resource_pack.zip";
        String outputDir = "polymer/resource_pack_unzipped";

        Map<String, Object> geysers = new HashMap<>();

        try {
            unzip(zipFilePath, outputDir);
            List<Path> jsonFiles = findJsonFiles(outputDir, "models/custom/block");
            if (!jsonFiles.isEmpty()) {
                System.out.println("Found JSON files:");
                jsonFiles.forEach(file -> {
                    try {
                        geysers.put(file.toFile().getName(), convertState(file.toFile()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                System.out.println("No JSON files found in models/block.");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        new File(outputDir).delete();
        return geysers;
    }

    public static ObjectNode convertState(File input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(input);

        // Extract textures
        JsonNode textures = root.path("textures");
        ObjectNode textureMap = mapper.createObjectNode();
        if (textures.isObject()) {
            textures.fields().forEachRemaining(entry -> {
                String key = "#" + entry.getKey(); // Prefix with '#' to match face texture keys
                String value = entry.getValue().asText();
                textureMap.put(key, value);
            });
        }

        // Extract material_instances with faces
        JsonNode elements = root.path("elements");
        if (elements.isArray()) {
            for (JsonNode element : elements) {
                JsonNode faces = element.path("faces");
                if (faces.isObject()) {
                    ObjectNode simplifiedFaces = mapper.createObjectNode();
                    faces.fields().forEachRemaining(entry -> {
                        String face = entry.getKey();
                        JsonNode faceData = entry.getValue();
                        String textureKey = faceData.path("texture").asText();
                        String realTexture = textureMap.path(textureKey).asText();

                        ObjectNode faceObject = mapper.createObjectNode();
                        faceObject.put("texture", realTexture);
                        simplifiedFaces.set(face, faceObject);
                    });

                    return simplifyFace(simplifiedFaces);
                }
            }
        }

        return null;
    }

    public static ObjectNode simplifyFace(ObjectNode faces) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        if (faces.isObject()) {
            String commonTexture = null;
            boolean allSame = true;

            for (JsonNode face : faces) {
                String texture = face.path("texture").asText();
                if (commonTexture == null) {
                    commonTexture = texture;
                } else if (!commonTexture.equals(texture)) {
                    allSame = false;
                    break;
                }
            }

            if (allSame && commonTexture != null) {
                ObjectNode mergedTexture = mapper.createObjectNode();
                mergedTexture.put("texture", commonTexture);
                output.set("*", mergedTexture);
            } else {
                return faces;
            }
        }

        return output;
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

        Files.walk(startPath)
                .filter(path -> {
                    Path relativePath = startPath.relativize(path);
                    return relativePath.toString().contains(targetSubfolder) &&
                            path.toString().endsWith(".json");
                })
                .forEach(jsonFiles::add);

        return jsonFiles;
    }
}