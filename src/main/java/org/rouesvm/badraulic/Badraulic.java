package org.rouesvm.badraulic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.rouesvm.badraulic.Mappings.item.ItemJsonConvertor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.getBlocks;
import static org.rouesvm.badraulic.Mappings.GeyserMappings.getItems;
import static org.rouesvm.badraulic.Pack.PackReader.getCustomModelDataJsonFiles;

public class Badraulic implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            try {
                getItems();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                getBlocks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Set<ObjectNode> getCustomModelData() throws IOException {
        Set<ObjectNode> instances = new HashSet<>();

        getCustomModelDataJsonFiles("polymer/resource_pack_unzipped").forEach(stuff -> {
            try {
                instances.add(ItemJsonConvertor.convertJsonToGeyserFormat(stuff.toFile()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return instances;
    }

    private static int findAssociatedNumber(JsonNode root, String searchKey) {
        for (JsonNode categoryNode : root) {
            if (categoryNode.has(searchKey)) {
                return categoryNode.get(searchKey).asInt();
            }
        }
        return -1;
    }
}

