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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.*;

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

    public static void createFiles(Map<String, String> stringSet, Map<String, Map<String, Object>> modGeysers) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        for (var modEntry : modGeysers.entrySet()) {
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

            mapper.writeValue(Paths.get("geyser_jsons/block", modEntry.getKey() + "_block_mappings.json").toFile(),
                    geyserConfig);
        }

        Map<String, Object> modTextureData = new HashMap<>();

        Map<String, Object> jsonObject = new HashMap<>();
        createAccurateGeyserTextures(stringSet, jsonObject);
        modTextureData.put("textureData", jsonObject);

        mapper.writeValue(Paths.get("geyser_jsons", "terrain_texture.json").toFile(),
                modTextureData.get("textureData")
        );
    }

    private static List<Object> getSimilarNames(Map<String, Object> instances, String name) {
        return instances.entrySet().stream()
                .filter(entry -> isSimilar(entry.getKey(), normalizeName(name)))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
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

    private static String convertBlockFormat(String input) {
        return input.replace("Block{", "").replace("}", "")
                .replace("minecraft:note_block[", "")
                .replace("]", "");
    }
}
