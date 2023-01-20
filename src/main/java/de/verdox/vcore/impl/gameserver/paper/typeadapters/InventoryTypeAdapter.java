package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.verdox.vcore.impl.gameserver.paper.util.PaperUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Objects;

public class InventoryTypeAdapter extends TypeAdapter<Inventory> {
    @Override
    public void write(JsonWriter out, Inventory value) throws IOException {
        out.beginObject();
        out.name("storageContents");
        var base64 = PaperUtil.itemStackArrayToBase64(value.getStorageContents());
        out.value(base64);
        out.name("type");
        out.value(value.getType().name());
        out.name("maxStackSize");
        out.value(value.getMaxStackSize());
        out.endObject();
    }

    @Override
    public Inventory read(JsonReader in) throws IOException {
        String fieldName = null;

        ItemStack[] items = null;
        InventoryType type = null;
        int maxStackSize = 64;

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("storageContents".equals(fieldName)) {
                in.peek();
                var base64 = in.nextString();
                items = PaperUtil.itemStackArrayFromBase64(base64);
            } else if ("type".equals(fieldName)) {
                in.peek();
                type = InventoryType.valueOf(in.nextString());
            } else if ("maxStackSize".equals(fieldName)) {
                in.peek();
                maxStackSize = in.nextInt();
            }
        }

        Objects.requireNonNull(items, "No Items found during deserialization");
        Objects.requireNonNull(type, "No Inventory type found during deserialization");
        var inventory = Bukkit.createInventory(null, type);
        inventory.setStorageContents(items);
        inventory.setMaxStackSize(maxStackSize);
        return inventory;
    }
}
