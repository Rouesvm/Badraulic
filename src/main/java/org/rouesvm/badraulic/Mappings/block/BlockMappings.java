package org.rouesvm.badraulic.Mappings.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockMappings {
    public static void createForBlock(
            Map<String, Map<String, Object>> modStateOverrides,
            Map<String, Object> instances,
            Map.Entry<RegistryKey<Block>, Block> entry
    ) {
        Block block = entry.getValue();

        if (!(block instanceof PolymerBlock polymerTexturedBlock)) return;

        ImmutableList<BlockState> state = block.getStateManager().getStates();

        state.forEach(blockState -> {
            BlockState noteblockState = polymerTexturedBlock.getPolymerBlockState(blockState);
            if (!(noteblockState.getBlock() instanceof NoteBlock)) return;

            Identifier identifier = entry.getKey().getValue();
            String modName = identifier.getNamespace();
            String blockName = identifier.getPath();

            String geyserState = BlockMappings.convertBlockFormat(noteblockState.toString());

            Map<String, Object> stateOverrides = modStateOverrides
                    .computeIfAbsent(modName, k -> new HashMap<>());

            List<Object> uniqueName = BlockMappings.getSimilarNames(instances, blockName);
            stateOverrides.put(geyserState, BlockMappings.createGeyserState(blockName, block, uniqueName));
        });
    }

    public static void createFiles(Set<String> stringSet, Map<String, Map<String, Object>> modGeysers) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Files.createDirectories(Paths.get("mod_jsons"));

        for (Map.Entry<String, Map<String, Object>> modEntry : modGeysers.entrySet()) {
            Map<String, Object> geyserConfig = Map.of(
                    "format_version", 1,
                    "blocks", Map.of(
                            "minecraft:note_block", Map.of(
                                    "name", modEntry.getKey(),
                                    "geometry", "geometry.geo_generic_cube",
                                    "included_in_creative_inventory", false,
                                    "only_override_states", true,
                                    "place_air", true,
                                    "state_overrides", modEntry.getValue()
                            )
                    )
            );

            mapper.writeValue(Paths.get("mod_jsons", modEntry.getKey() + "_geyser_config.json").toFile(),
                    geyserConfig
            );
        }

        Map<String, Object> modTextureData = new HashMap<>();

        Map<String, Object> jsonObject = new HashMap<>();
        createGeyserTextures(stringSet, jsonObject);
        modTextureData.put("textureData", jsonObject);

        mapper.writeValue(Paths.get("mod_jsons", "texture.json").toFile(),
                modTextureData.get("textureData")
        );
    }

    private static List<Object> getSimilarNames(Map<String, Object> instances, String name) {
        return instances.entrySet().stream()
                .filter(entry -> isSimilar(entry.getKey(), name))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private static boolean isSimilar(String key, String name) {
        boolean contains = key.toLowerCase().contains(name.toLowerCase());
        Set<String> strings = splitString(name);

        int amount = 0;

        for (String string : strings) {
            if (key.toLowerCase().contains(string.toLowerCase())) {
                if (amount < strings.size())
                    amount++;
            }
        }

        if (amount == strings.size()) return true;

        if (name.contains("block") && !contains)
            return key.toLowerCase().contains(name.toLowerCase().replace("_block", ""));
        else if (key.toLowerCase().contains("_") && name.contains("_"))
            return key.contains("_") && contains;
        else if (key.toLowerCase().contains("_") && !name.contains("_"))
            return false;

        return contains;
    }

    private static Set<String> splitString(String input) {
        input = input.replace(".json", "");
        String[] parts = input.split("_");
        return Set.of(parts);
    }

    private static Map<String, Object> createGeyserState(String name, Block block, List<Object> instances) {
        Map<String, Object> geyserDetails = new HashMap<>();
        geyserDetails.put("name", name);
        geyserDetails.put("display_name", name);
        geyserDetails.put("destructible_by_mining", block.getHardness() * 2);

        if (!instances.isEmpty() && instances.getFirst() != null)
            geyserDetails.put("material_instances", instances.getFirst());
        return geyserDetails;
    }

    public static void createGeyserTextures(Set<String> names, Map<String, Object> jsonObject) {
        names.forEach(string -> {
            Map<String, Object> textureDetails = new HashMap<>();
            textureDetails.put("texture", string);
            jsonObject.put(string, textureDetails);
        });
    }

    private static String convertBlockFormat(String input) {
        return input.replace("Block{", "").replace("}", "")
                .replace("minecraft:note_block[", "")
                .replace("]", "");
    }
}
