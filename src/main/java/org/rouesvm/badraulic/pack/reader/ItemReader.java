package org.rouesvm.badraulic.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.rouesvm.badraulic.pack.reader.PackReader.findJsonFiles;

public class ItemReader {
    public static Map<String, String> getItemTextureName(File input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> stringMap = new HashMap<>();
        JsonNode root = mapper.readTree(input);

        JsonNode textures = root.path("textures");

        if (input.getName().equals("paper.json")) {
            JsonNode arrayNode = root.get("overrides");
            if (arrayNode != null && arrayNode.isArray()) {
                arrayNode.forEach(jsonNode -> {
                    JsonNode modelNode = jsonNode.get("model");
                    String texture = modelNode.asText();
                    String newTexture = texture;
                    newTexture =  "textures/" + newTexture;
                    newTexture = newTexture.replace("custom/", "custom/item/");
                    stringMap.put(texture, newTexture);
                });
            }
        }

        // Separate ifs because lower is more accurate.

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

    public static List<Path> getItemJsonFiles(String outputDir) throws IOException {
        List<Path> item1 = findJsonFiles(outputDir, "models/custom/item");
        List<Path> item2 = findJsonFiles(outputDir, "models/item");
        List<Path> item3 = findJsonFiles(outputDir, "models/item/block");
        item2.addAll(item3);
        item2.addAll(item1);
        return item2;
    }
}
