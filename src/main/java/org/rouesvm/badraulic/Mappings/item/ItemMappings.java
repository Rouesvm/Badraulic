package org.rouesvm.badraulic.Mappings.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ItemMappings {
    public static void createForItem(Map.Entry<RegistryKey<Item>, Item> entry, Map<String, ObjectNode> modItemNodes) throws IOException {
        Item item = entry.getValue();

        if (!(item.asItem() instanceof PolymerItem polymerItem)) return;
        ItemStack itemStack = PolymerItemUtils.getPolymerItemStack(item.getDefaultStack(), PacketContext.create());
        if (itemStack == null) return;
        Optional<RegistryKey<Item>> optionalKey = itemStack.getRegistryEntry().getKey();
        if (optionalKey.isEmpty()) return;

        Identifier name = optionalKey.get().getValue();
        String realName = item.asItem().getTranslationKey();
        Identifier itemModel = polymerItem.getPolymerItemModel(itemStack, PacketContext.create());

        String modName = itemModel.getNamespace();

        ObjectNode itemsNode = modItemNodes.computeIfAbsent(modName, k -> new ObjectMapper().createObjectNode());
        ArrayNode itemNode = (ArrayNode) itemsNode.get(name.toString());
        if (itemNode == null) itemNode = createVanillaItem(itemsNode, name.toString());
        if (itemNode == null) return;

        createItem(itemNode, itemModel, realName);
        itemsNode.set(name.toString(), itemNode);
    }

    public static ObjectNode createBase() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        root.put("format_version", "1");
        return root;
    }

    public static ArrayNode createVanillaItem(ObjectNode itemsNode, String itemName) {
        return itemsNode.putArray(itemName);
    }

    public static void createItem(ArrayNode itemNode, Identifier itemModel, String realName) {
        ObjectNode smallBackpack = itemNode.addObject();
        smallBackpack.put("display_name", realName);
        smallBackpack.put("name", realName);
        smallBackpack.put("allow_offhand", true);
        smallBackpack.put("icon", itemModel.toString());
        smallBackpack.put("item_model_data", itemModel.toString());
        smallBackpack.put("creative_category", 1);
    }
}
