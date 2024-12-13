package org.rouesvm.badraulic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.rouesvm.badraulic.Mappings.item.ItemJsonConvertor;
import org.rouesvm.badraulic.Pack.Creator.PackCreator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.rouesvm.badraulic.Mappings.GeyserMappings.*;
import static org.rouesvm.badraulic.Pack.Reader.PackReader.getCustomModelDataJsonFiles;
import static org.rouesvm.badraulic.Pack.Reader.PackReader.unzipPolymerPack;

public class Badraulic implements ModInitializer {

    @Override
    public void onInitialize() {
        try {
            createFilesForJsons();
            unzipPolymerPack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            try {
                getItems();
                getBlocks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                PackCreator.createBasePack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static HashMap<String, ObjectNode> getCustomModelData() throws IOException {
        HashMap<String, ObjectNode> instances = new HashMap<>();

        List<Path> list = getCustomModelDataJsonFiles("polymer/resource_pack_unzipped");
        if (!list.isEmpty()) {
            list.forEach(file -> {
                try {
                    HashMap<String, ObjectNode> node = ItemJsonConvertor.convertJsonToGeyserFormat(file.toFile());
                    if (node != null) instances.putAll(node);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return instances;
    }
}

