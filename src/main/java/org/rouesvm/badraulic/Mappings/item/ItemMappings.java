package org.rouesvm.badraulic.Mappings.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemMappings {
    public static void createForItem(Map.Entry<RegistryKey<Item>, Item> entry, Map<String, ObjectNode> modItemNodes) throws IOException {
        Item item = entry.getValue();

        if (!(item.asItem() instanceof PolymerItem )) return;
        ItemStack itemStack = PolymerItemUtils.getPolymerItemStack(item.getDefaultStack(), PacketContext.get());
        if (itemStack == null) return;
        Optional<RegistryKey<Item>> optionalKey = itemStack.getRegistryEntry().getKey();
        if (optionalKey.isEmpty()) return;

        Identifier name = optionalKey.get().getValue();
        String realName = item.asItem().getTranslationKey();
        CustomModelDataComponent itemModel = itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);

        String modName = name.getNamespace();

        ObjectNode itemsNode = modItemNodes.computeIfAbsent(modName, k -> new ObjectMapper().createObjectNode());
        ArrayNode itemNode = (ArrayNode) itemsNode.get(name.toString());
        if (itemNode == null) itemNode = createVanillaItem(itemsNode, name.toString());
        if (itemNode == null) return;

        createItem(itemNode, itemModel.value(), realName, PolymerItemUtils.getPolymerIdentifier(itemStack).toString());
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

    public static void createItem(ArrayNode itemNode, int itemModel, String realName, String secondOne) {
        ObjectNode smallBackpack = itemNode.addObject();
        smallBackpack.put("display_name", realName);
        smallBackpack.put("name", realName);
        smallBackpack.put("allow_offhand", true);
        smallBackpack.put("icon", secondOne);
        smallBackpack.put("custom_model_data", itemModel);
        smallBackpack.put("creative_category", 1);
    }
}
