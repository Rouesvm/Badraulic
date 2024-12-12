package org.rouesvm.badraulic.Mappings.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class ItemJsonConvertor {
    public static ObjectNode convertJsonToGeyserFormat(File input) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode inputRoot = mapper.readTree(input);

        ObjectNode outputRoot = mapper.createObjectNode();
        outputRoot.put("format_version", "1");

        ObjectNode itemsNode = mapper.createObjectNode();
        outputRoot.set("items", itemsNode);

        Iterator<Map.Entry<String, JsonNode>> fields = inputRoot.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();

            String minecraftKey = "minecraft:" + entry.getKey();
            itemsNode.putArray(minecraftKey);

            ArrayNode itemArray = (ArrayNode) itemsNode.get(minecraftKey);
            JsonNode innerMap = entry.getValue();

            innerMap.fields().forEachRemaining(innerEntry -> {
                String icon = innerEntry.getKey();
                int customModelData = innerEntry.getValue().asInt();

                ObjectNode itemObject = mapper.createObjectNode();
                String itemName = icon.replace("/", "_").replace(":", ".");
                itemObject.put("display_name", "item." + itemName);
                itemObject.put("name", "item." + itemName);
                itemObject.put("allow_offhand", true);
                itemObject.put("icon", icon);
                itemObject.put("custom_model_data", customModelData);
                itemObject.put("creative_category", 1);

                itemArray.add(itemObject);
            });
        }

        return outputRoot;
    }
}
