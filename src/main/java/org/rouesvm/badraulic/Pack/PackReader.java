package org.rouesvm.badraulic.Pack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackReader {
    public static Map<String, Object> getMaterialInstances() {
        Map<String, Object> geysers = new HashMap<>();

        try {
            List<Path> jsonFiles = unzipPolymerPackAndGetBlockJsonFiles();
            if (!jsonFiles.isEmpty()) {
                System.out.println("Found JSON files:");
                jsonFiles.forEach(file -> {
                    try {
                        convertState(file.toFile(), geysers);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else System.out.println("No JSON files found in models/block.");
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

    private static Map<String, String> getItemTextureName(File input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> stringMap = new HashMap<>();
        JsonNode root = mapper.readTree(input);

        JsonNode textures = root.path("textures");

        if (textures.isObject()) {
            textures.fields().forEachRemaining(entry -> {
                String texture = entry.getValue().asText();
                String newTexture = texture;
                texture = texture.replace(":block", ":item/block");
                newTexture =  "textures/" + newTexture;
                newTexture = newTexture.replace(":item", "/item");
                newTexture = newTexture.replace(":block", "/item");
                stringMap.put(texture, newTexture);
            });
        } else if (root.isObject()) {
            JsonNode parent = root.get("parent");
            String texture = parent.textValue();
            String newTexture = texture;
            texture = texture.replace(":block", ":item/block");
            newTexture =  "textures/" + newTexture;
            newTexture = newTexture.replace(":block", "/item/block");
            stringMap.put(texture, newTexture);
        }

        return stringMap;
    }

    private static Map<String, String> getBlockTextureName(File input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> stringMap = new HashMap<>();
        JsonNode root = mapper.readTree(input);

        JsonNode textures = root.path("textures");
        ObjectNode textureMap = mapper.createObjectNode();
        if (textures.isObject()) {
            textures.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String texture = entry.getValue().asText();

                if (Objects.equals(key, "side")) {
                    textureMap.put("*", texture);
                    textureMap.put("north", texture);
                    textureMap.put("east", texture);
                    textureMap.put("south", texture);
                    textureMap.put("west", texture);
                    return;
                } else if (Objects.equals(key, "end")) {
                    textureMap.put("up", texture);
                    textureMap.put("down", texture);
                    return;
                }
                textureMap.put("#" + key, texture);
            });
        }

        JsonNode elements = root.path("elements");
        // Filament
        if (elements.isArray()) {
            for (JsonNode element : elements) {
                JsonNode faces = element.path("faces");
                if (faces.isObject()) {
                    faces.fields().forEachRemaining(entry -> {
                        JsonNode faceData = entry.getValue();
                        String textureKey = faceData.path("texture").asText();
                        String texture = textureMap.path(textureKey).asText();
                        String newTexture = texture;
                        newTexture =  "textures/" + newTexture;
                        newTexture = newTexture.replace("/block/custom", "/custom/block");

                        stringMap.put(texture, newTexture);
                    });
                }
            }
        } else {
            textureMap.fields().forEachRemaining(entry -> {
                String texture = entry.getValue().asText();
                String newTexture = texture;
                newTexture =  "textures/" + newTexture;
                newTexture = newTexture.replace(":block", "/block");
                stringMap.put(texture, newTexture);
            });
        }

        return stringMap;
    }

    private static void convertState(File input, Map<String, Object> geyser) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(input);

        JsonNode textures = root.path("textures");
        ObjectNode textureMap = mapper.createObjectNode();
        if (textures.isObject()) {
            textures.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String texture = entry.getValue().asText();

                if (Objects.equals(key, "side")) {
                    textureMap.put("*", texture);
                    textureMap.put("north", texture);
                    textureMap.put("east", texture);
                    textureMap.put("south", texture);
                    textureMap.put("west", texture);
                    return;
                } else if (Objects.equals(key, "end")) {
                    textureMap.put("up", texture);
                    textureMap.put("down", texture);
                    return;
                }

                textureMap.put("#" + key, texture);
            });
        }

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

                        geyser.put(realTexture, simplifyFace(simplifiedFaces));
                    });
                }
            }
        } else  {
            String realTexture = textureMap.path("*").asText();
            ObjectNode materialInstances = mapper.createObjectNode();
            materialInstances.putObject("*").put("texture", textureMap.path("*").asText());
            materialInstances.putObject("north").put("texture", textureMap.path("north").asText());
            materialInstances.putObject("east").put("texture", textureMap.path("east").asText());
            materialInstances.putObject("south").put("texture", textureMap.path("south").asText());
            materialInstances.putObject("west").put("texture", textureMap.path("west").asText());
            materialInstances.putObject("up").put("texture", textureMap.path("up").asText());
            materialInstances.putObject("down").put("texture", textureMap.path("down").asText());

            geyser.put(realTexture, simplifyFace(materialInstances));
        }
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


    private static List<Path> unzipPolymerPackAndGetBlockJsonFiles() throws IOException {
        String zipFilePath = "polymer/resource_pack.zip";
        String outputDir = "polymer/resource_pack_unzipped";
        unzip(zipFilePath, outputDir);
        return getBlockJsonFiles(outputDir);
    }

    public static List<Path> getCustomModelDataJsonFiles(String outputDir) throws IOException {
        return findJsonFiles(outputDir, "polymer");
    }

    private static List<Path> getItemJsonFiles(String outputDir) throws IOException {
        List<Path> item1 = findJsonFiles(outputDir, "models/custom/item");
        List<Path> item2 = findJsonFiles(outputDir, "models/item");
        List<Path> item3 = findJsonFiles(outputDir, "models/item/block");
        item2.addAll(item3);
        item2.addAll(item1);
        return item2;
    }

    private static List<Path> getBlockJsonFiles(String outputDir) throws IOException {
        List<Path> block1 = findJsonFiles(outputDir, "models/custom/block");
        List<Path> block2 = findJsonFiles(outputDir, "models/block");
        block2.addAll(block1);
        return block2;
    }
}