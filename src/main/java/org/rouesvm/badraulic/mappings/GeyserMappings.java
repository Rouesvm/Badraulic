package org.rouesvm.badraulic.mappings;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minecraft.registry.Registries;
import org.rouesvm.badraulic.Badraulic;
import org.rouesvm.badraulic.mappings.block.BlockMappings;
import org.rouesvm.badraulic.mappings.item.ItemJsonConvertor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.rouesvm.badraulic.pack.reader.PackReader.*;

public class GeyserMappings {
    public static void createFilesForJsons() throws IOException {
        Files.createDirectories(Paths.get("geyser_jsons"));
        Files.createDirectories(Paths.get("geyser_jsons/block"));
        Files.createDirectories(Paths.get("geyser_jsons/item"));
    }

    public static void getBlocks() throws IOException {
        Map<String, Object> instances = getMaterialInstances();
        Map<String, Map<String, Object>> modStateOverrides = new HashMap<>();

        Registries.BLOCK.getEntrySet().forEach(entry ->
                BlockMappings.createForBlock(modStateOverrides, instances, entry));

        Map<String, String> stringSet = getBlockTextures();
        BlockMappings.createFiles(stringSet, modStateOverrides);
    }

    public static void getItems() throws IOException {
        Files.createDirectories(Paths.get("geyser_jsons"));

        HashMap<String, ObjectNode> customModelData = Badraulic.getCustomModelData();
        ItemJsonConvertor.createFiles(customModelData);
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

    public static Set<String> splitString(String input, String toSplit) {
        input = input.replace(".json", "");
        String[] parts = input.split(toSplit);
        return new HashSet<>(Arrays.asList(parts));
    }

    public static boolean isSimilar(String key, String name) {
        boolean contains = key.toLowerCase().contains(name.toLowerCase()) ||
                name.toLowerCase().contains(key.toLowerCase());

        LinkedHashSet<String> keyParts = new LinkedHashSet<>(splitString(key, "[._/]"));
        LinkedHashSet<String> nameParts = new LinkedHashSet<>(splitString(name, "[._/]"));

        long matchCount = keyParts.stream()
                .filter(nameParts::contains)
                .count();

        boolean lengthMatch = key.length() == name.length();
        boolean letterCountCheck = key.length() >= name.length();

        if (key.contains("_on") ^ name.contains("_on")) {
            return false;
        }

        return (lengthMatch && contains)
                || (contains && letterCountCheck)
                || matchCount >= nameParts.size();
    }

    public static String normalizeName(String name) {
        return name.replace("_side", "")
                .replace("_all", "")
                .replace(".json", "")
                .replace("item.", "")
                .replace("block.", "");
    }

}
