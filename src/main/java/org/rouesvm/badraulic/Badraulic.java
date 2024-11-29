package org.rouesvm.badraulic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.rouesvm.badraulic.Pack.PackReader;

import java.io.IOException;
import java.util.Map;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.getBlocks;

public class Badraulic implements ModInitializer, EventRegistrar {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            GeyserApi.api().eventBus().register(this, this);

            try {
                getBlocks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

