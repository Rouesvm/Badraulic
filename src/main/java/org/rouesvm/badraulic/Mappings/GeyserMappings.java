package org.rouesvm.badraulic.Mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minecraft.registry.Registries;
import org.rouesvm.badraulic.Badraulic;
import org.rouesvm.badraulic.Mappings.block.BlockMappings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.rouesvm.badraulic.Pack.PackReader.*;

public class GeyserMappings {
    public static void getBlocks() throws IOException {
        Map<String, Object> instances = getMaterialInstances();
        Map<String, Map<String, Object>> modStateOverrides = new HashMap<>();

        Registries.BLOCK.getEntrySet().forEach(entry ->
                BlockMappings.createForBlock(modStateOverrides, instances, entry));

        Map<String, String> stringSet = getBlockTextures();
        BlockMappings.createFiles(stringSet, modStateOverrides);
    }

    public static void getItems() throws IOException {
        Set<ObjectNode> customModelData = Badraulic.getCustomModelData();

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        for (ObjectNode modEntry : customModelData) {
            Path modFilePath = Paths.get("mod_jsons", "stuff" + "_mappings.json");
            Files.createDirectories(modFilePath.getParent());
            mapper.writeValue(modFilePath.toFile(), modEntry);
        }

        Map<String, String> stringSet = getItemTextures();

        Map<String, Object> modTextureData = new HashMap<>();
        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put("resource_pack_name", "geyser_custom");
        jsonObject.put("texture_name", "atlas.items");

        createAccurateGeyserTextures(stringSet, jsonObject);
        modTextureData.put("textureData", jsonObject);

        mapper.writeValue(Paths.get("mod_jsons", "item_texture.json").toFile(),
                modTextureData.get("textureData")
        );
    }

    public static void createAccurateGeyserTextures(Map<String, String> names, Map<String, Object> jsonObject) {
        Map<String, Object> jsonMap = new HashMap<>();

        names.forEach((string, string1) -> {
            Map<String, Object> textureDetails = new HashMap<>();
            textureDetails.put("textures", string1);
            jsonMap.put(string, textureDetails);
        });

        jsonObject.put("texture_data", jsonMap);
    }

    private static Set<String> splitString(String input) {
        input = input.replace(".json", "");
        String[] parts = input.split("[._/]");
        return new HashSet<>(Arrays.asList(parts));
    }

    public static boolean isSimilar(String key, String name) {
        boolean contains = key.toLowerCase().contains(name.toLowerCase()) ||
                name.toLowerCase().contains(key.toLowerCase());
        Set<String> keyParts = splitString(key);
        Set<String> nameParts = splitString(name);

        long matchCount = nameParts.stream().filter(keyParts::contains).count();

        boolean lengthMatch = key.length() == name.length();
        boolean letterCountCheck = key.length() >= name.length();

        return (lengthMatch && contains) || (contains && letterCountCheck) || matchCount >= nameParts.size();
    }

    public static String normalizeName(String name) {
        return name.replace("_on", "")
                .replace(".json", "")
                .replace("item.", "")
                .replace("block.", "")
                .replace("item.", "");
    }

}
