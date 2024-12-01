package org.rouesvm.badraulic.Mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minecraft.registry.Registries;
import org.rouesvm.badraulic.Mappings.block.BlockMappings;
import org.rouesvm.badraulic.Mappings.item.ItemMappings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.rouesvm.badraulic.Pack.PackReader.*;

public class GeyserMappings {
    public static void getBlocks() throws IOException {
        Map<String, Object> instances = getMaterialInstances();
        Map<String, Map<String, Object>> modStateOverrides = new HashMap<>();

        Registries.BLOCK.getEntrySet().forEach(entry ->
                BlockMappings.createForBlock(modStateOverrides, instances, entry));

        Set<String> stringSet = getTextures();
        BlockMappings.createFiles(stringSet, modStateOverrides);
    }

    public static void getItems() throws IOException {
        Map<String, ObjectNode> modItemNodes = new HashMap<>();

        Registries.ITEM.getEntrySet().forEach(entry -> {
            try {
                ItemMappings.createForItem(entry, modItemNodes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        for (Map.Entry<String, ObjectNode> modEntry : modItemNodes.entrySet()) {
            ObjectNode base = ItemMappings.createBase();
            ObjectNode itemBase = base.putObject("items");

            String modName = modEntry.getKey();
            ObjectNode modNode = modEntry.getValue();

            itemBase.setAll(modNode);

            Path modFilePath = Paths.get("mod_jsons", modName + "_mappings.json");
            Files.createDirectories(modFilePath.getParent());
            mapper.writeValue(modFilePath.toFile(), base);
        }
    }
}
