package org.rouesvm.badraulic.Mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.registry.Registries;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.rouesvm.badraulic.Pack.PackReader.getTextures;

public class GeyserMappings {
    public static void createJsonFiles(Map<String, Object> instances) throws IOException {
        Set<String> stringSet = getTextures();

        Map<String, Object> geysers = new HashMap<>();
        Map<String, Object> jsonObject = new HashMap<>();

        for (var entry : Registries.BLOCK.getEntrySet()) {
            Block block = entry.getValue();
            if (block instanceof PolymerBlock polymerTexturedBlock) {
                BlockState state = polymerTexturedBlock.getPolymerBlockState(block.getDefaultState());
                if (state.getBlock() instanceof NoteBlock) {
                    String geyserState = convertBlockFormat(state.toString());
                    String name = entry.getKey().getValue().getPath();

                    geysers.put(geyserState, createGeyserState(name, block, getSimilarNames(instances, name)));
                    createGeyserTextures(stringSet, jsonObject);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper jsonTexture = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), geysers);
        jsonTexture.writerWithDefaultPrettyPrinter().writeValue(new File("textureData.json"), jsonObject);
    }

    private static List<Object> getSimilarNames(Map<String, Object> instances, String name) {
        return instances.entrySet().stream()
                .filter(entry -> isSimilar(entry.getKey(), name))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private static boolean isSimilar(String key, String name) {
        boolean contains = key.toLowerCase().contains(name.toLowerCase());
        if (name.contains("block") && !contains)
            return key.toLowerCase().contains(name.toLowerCase().replace("_block", ""));
        else if (key.toLowerCase().contains("_") && name.contains("_"))
            return key.contains("_") && contains;
        else if (key.toLowerCase().contains("_") && !name.contains("_"))
            return false;
        else return contains;
    }

    private static Map<String, Object> createGeyserState(String name, Block block, List<Object> instances) {
        Map<String, Object> geyserDetails = new HashMap<>();
        geyserDetails.put("name", name);
        geyserDetails.put("display_name", name);
        geyserDetails.put("destructible_by_mining", block.getHardness() * 2);

        geyserDetails.put("material_instances", instances.getFirst());
        return geyserDetails;
    }

    private static void createGeyserTextures(Set<String> names, Map<String, Object> jsonObject) {
        names.forEach(string -> {
            Map<String, Object> textureDetails = new HashMap<>();
            textureDetails.put("texture", string);
            jsonObject.put(string, textureDetails);
        });

        System.out.println(jsonObject);
    }

    private static String extractTexture(String name) {
        String textureKey = "\"texture\": \"";
        int startIndex = name.indexOf(textureKey);

        if (startIndex != -1) {
            int endIndex = name.indexOf("\"", startIndex + textureKey.length());
            if (endIndex != -1) {
                return name.substring(startIndex + textureKey.length(), endIndex);
            }
        }

        return null;
    }

    private static String convertBlockFormat(String input) {
        return input.replace("Block{", "").replace("}", "")
                .replace("minecraft:note_block[", "")
                .replace("]", "");
    }
}
