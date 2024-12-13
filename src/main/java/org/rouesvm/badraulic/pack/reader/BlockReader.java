package org.rouesvm.badraulic.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.rouesvm.badraulic.pack.reader.PackReader.findJsonFiles;

public class BlockReader {
    public static Map<String, String> getBlockTextureName(File input) throws IOException {
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

    public static void convertState(File input, Map<String, Object> geyser) throws IOException {
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
            textureMap.fields().forEachRemaining(entry ->
                materialInstances.putObject(entry.getKey()).put("texture", entry.getValue().asText())
            );

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


    public static List<Path> getBlockJsonFiles() throws IOException {
        String outputDir = "polymer/resource_pack_unzipped";
        return getBlockJsonFiles(outputDir);
    }

    public static List<Path> getBlockJsonFiles(String outputDir) throws IOException {
        List<Path> block1 = findJsonFiles(outputDir, "models/custom/block");
        List<Path> block2 = findJsonFiles(outputDir, "models/block");
        List<Path> block3 = findJsonFiles(outputDir, "models/");
        block2.addAll(block3);
        block2.addAll(block1);
        return block2;
    }
}
