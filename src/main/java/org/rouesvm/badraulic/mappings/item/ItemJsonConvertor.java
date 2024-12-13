package org.rouesvm.badraulic.mappings.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.rouesvm.badraulic.mappings.GeyserMappings.createAccurateGeyserTextures;
import static org.rouesvm.badraulic.pack.reader.PackReader.getItemTextures;

public class ItemJsonConvertor {
    public static void createFiles(HashMap<String, ObjectNode> customModelData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        for (var modEntry : customModelData.entrySet())
            mapper.writeValue(Paths.get("geyser_jsons/item", modEntry.getKey() + "_item_mappings.json").toFile(),
                    modEntry.getValue()
            );

        Map<String, String> stringSet = getItemTextures();

        Map<String, Object> modTextureData = new HashMap<>();
        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put("resource_pack_name", "geyser_custom");
        jsonObject.put("texture_name", "atlas.items");

        createAccurateGeyserTextures(stringSet, jsonObject);
        modTextureData.put("textureData", jsonObject);

        mapper.writeValue(Paths.get("geyser_jsons", "item_texture.json").toFile(),
                modTextureData.get("textureData"));
    }

    public static HashMap<String, ObjectNode> convertJsonToGeyserFormat(File input) throws Exception {
        if (!input.exists()) return null;

        HashMap<String, ObjectNode> modModels = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode inputRoot = mapper.readTree(input);

        inputRoot.fields().forEachRemaining(entry -> {
            String minecraftKey = "minecraft:" + entry.getKey();
            JsonNode innerMap = entry.getValue();

            innerMap.fields().forEachRemaining(innerEntry -> {
                String icon = innerEntry.getKey();
                int customModelData = innerEntry.getValue().asInt();

                String modName = icon.split(":")[0];
                if (modName.contains("/")) modName = "custom";

                ObjectNode itemsNode = modModels.computeIfAbsent(modName, key -> mapper.createObjectNode());
                ObjectNode itemsMap = (ObjectNode) itemsNode.get("items");

                if (itemsMap == null) {
                    itemsMap = mapper.createObjectNode();
                    itemsNode.put("format_version", "1");
                    itemsNode.set("items", itemsMap);
                }

                ArrayNode itemArray = (ArrayNode) itemsMap.get(minecraftKey);
                if (itemArray == null) {
                    itemArray = mapper.createArrayNode();
                    itemsMap.set(minecraftKey, itemArray);
                }

                ObjectNode itemObject = mapper.createObjectNode();
                String itemName = icon.replace("/", "_").replace(":", ".");
                itemObject.put("display_name", "item." + itemName);
                itemObject.put("name", "item." + itemName);
                itemObject.put("allow_offhand", true);
                itemObject.put("icon", icon);
                itemObject.put("custom_model_data", customModelData);
                itemObject.put("creative_category", 1);

                itemArray.add(itemObject);

                modModels.put(modName, itemsNode);
            });
        });

        return modModels;
    }
}
