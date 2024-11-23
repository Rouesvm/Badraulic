package org.rouesvm.badraulic;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Badraulic implements ModInitializer, EventRegistrar {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            GeyserApi.api().eventBus().register(this, this);

            try {
                createJsonFiles();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void createJsonFiles() throws IOException {
        Map<String, Object> geysers = new HashMap<>();
        Map<String, Object> jsonObject = new HashMap<>();

        for (var entry : Registries.BLOCK.getEntrySet()) {
            Block block = entry.getValue();
            if (block instanceof PolymerBlock polymerTexturedBlock) {
                BlockState state = polymerTexturedBlock.getPolymerBlockState(block.getDefaultState());
                String geyserState = convertBlockFormat(state.toString());
                String name = entry.getKey().getValue().getPath();

                geysers.put(geyserState, createGeyserState(name, block));
                createGeyserTextures(name, jsonObject);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper jsonTexture = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), geysers);
        jsonTexture.writerWithDefaultPrettyPrinter().writeValue(new File("textureData.json"), jsonObject);
    }

    public static String convertBlockFormat(String input) {
        return input.replace("Block{", "").replace("}", "")
                .replace("minecraft:note_block[", "")
                .replace("]", "");
    }

    private static void createGeyserTextures(String name, Map<String, Object> jsonObject) {
        Map<String, Object> textureDetails = new HashMap<>();
        textureDetails.put("textures", "textures/blocks/" + name);
        jsonObject.put(name, textureDetails);
    }

    private static Map<String, Object> createGeyserState(String name, Block block) {
        Map<String, Object> textureMap = new HashMap<>();
        textureMap.put("texture", name);

        Map<String, Object> materialInstancesMap = new HashMap<>();
        materialInstancesMap.put("*", textureMap);

        Map<String, Object> geyserDetails = new HashMap<>();
        geyserDetails.put("name", name);
        geyserDetails.put("display_name", name);
        geyserDetails.put("destructible_by_mining", block.getHardness() * 2);
        geyserDetails.put("material_instances", materialInstancesMap);

        return geyserDetails;
    }
}

