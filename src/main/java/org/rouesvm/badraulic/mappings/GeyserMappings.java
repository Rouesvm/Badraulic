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
import java.util.stream.Collectors;

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

    public static double isSimilar(String key, String name) {
        key = key.toLowerCase();
        name = name.toLowerCase();

        boolean contains = key.contains(name) || name.contains(key);

        LinkedHashSet<String> keyParts = new LinkedHashSet<>(splitString(key, "[._/]"));
        LinkedHashSet<String> nameParts = new LinkedHashSet<>(splitString(name, "[._/]"));

        long matchCount = keyParts.stream().filter(nameParts::contains).count();

        boolean lengthMatch = key.length() == name.length();
        boolean letterCountCheck = key.length() >= name.length();

        int levenshteinDistance = calculateLevenshteinDistance(key, name);
        double normalizedLevenshtein = 1.0 - (double) levenshteinDistance / Math.max(key.length(), name.length());
        double jaccardSimilarity = calculateJaccardSimilarity(keyParts, nameParts);

        double matchScore = (matchCount * 0.3) +
                (normalizedLevenshtein * 0.4) +
                (jaccardSimilarity * 0.2);

        if (lengthMatch) matchScore += 0.2;
        if (letterCountCheck) matchScore += 0.1;
        if (contains) matchScore += 0.1;

        return (int) (matchScore * 100);
    }

    public static int calculateLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[str1.length()][str2.length()];
    }

    public static double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }


    public static String normalizeName(String name) {
        return name.replace("_side", "")
                .replace("_all", "")
                .replace(".json", "")
                .replace("item.", "")
                .replace("block.", "");
    }

}
