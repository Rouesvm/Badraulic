package org.rouesvm.badraulic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;

import java.io.IOException;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.getBlocks;
import static org.rouesvm.badraulic.Mappings.GeyserMappings.getItems;

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
}

