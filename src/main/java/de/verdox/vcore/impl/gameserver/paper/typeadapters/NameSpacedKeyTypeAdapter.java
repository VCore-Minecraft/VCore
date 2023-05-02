package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.verdox.vcore.impl.gameserver.paper.util.PaperUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Objects;

public class NameSpacedKeyTypeAdapter extends TypeAdapter<NamespacedKey> {
    @Override
    public void write(JsonWriter out, NamespacedKey value) throws IOException {
        out.beginObject();
        out.name("namespace");
        out.value(value.getNamespace());
        out.name("key");
        out.value(value.getKey());
        out.endObject();
    }

    @Override
    public NamespacedKey read(JsonReader in) throws IOException {
        String fieldName = null;

        String namespace = null;
        String key = null;

        in.beginObject();

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("key".equals(fieldName)) {
                in.peek();
                key = in.nextString();
            } else if ("namespace".equals(fieldName)) {
                in.peek();
                namespace = in.nextString();
            }
        }

        in.endObject();

        if(key == null || namespace == null)
            return null;
        return new NamespacedKey(namespace, key);
    }
}
