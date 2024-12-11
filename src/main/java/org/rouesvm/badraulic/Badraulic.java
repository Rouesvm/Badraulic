package org.rouesvm.badraulic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;

import java.io.IOException;

import static org.rouesvm.badraulic.Badraulic.getCustomModelData;
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

            try {
                Badraulic.getCustomModelData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void getCustomModelData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(getCustomModelDataJsonFiles("polymer/resource_pack_unzipped").getFirst().toFile());

        root.fields().forEachRemaining(entry -> {
            JsonNode nestedObject = entry.getValue();

            if (nestedObject.isObject()) {
                nestedObject.fields().forEachRemaining(nestedEntry -> {
                    String nestedKey = nestedEntry.getKey(); // Key inside nested object
                    int associatedNumber = findAssociatedNumber(root, nestedKey);

                    if (associatedNumber != -1) {
                        System.out.println("Key: " + nestedKey + ", Associated Number: " + associatedNumber);
                    } else {
                        System.out.println("Key not found: " + nestedKey);
                    }
                });
            }
        });
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

