package org.rouesvm.badraulic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.rouesvm.badraulic.Pack.PackReader;

import java.io.IOException;
import java.util.Map;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.createJsonFiles;

public class Badraulic implements ModInitializer, EventRegistrar {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            GeyserApi.api().eventBus().register(this, this);

            Map<String, Object> materialInstances = PackReader.getMaterialInstances();

            try {
                createJsonFiles(materialInstances);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

