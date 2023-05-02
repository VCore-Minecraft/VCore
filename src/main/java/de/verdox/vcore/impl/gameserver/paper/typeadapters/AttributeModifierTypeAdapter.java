package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.verdox.vpipeline.api.NetworkLogger;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;
import java.util.UUID;

public class AttributeModifierTypeAdapter extends TypeAdapter<AttributeModifier> {
    @Override
    public void write(JsonWriter out, AttributeModifier value) throws IOException {
        out.beginObject();
        out.name("uuid");
        out.value(value.getUniqueId().toString());
        out.name("name");
        out.value(value.getName());
        out.name("amount");
        out.value(value.getAmount());
        out.name("operation");
        out.value(value.getOperation().name());
        if (value.getSlot() != null) {
            out.name("slot");
            out.value(value.getSlot().name());
        }
        out.endObject();
    }

    @Override
    public AttributeModifier read(JsonReader in) throws IOException {

        String fieldName = null;

        UUID uuid = null;
        String name = null;
        double amount = 0;
        AttributeModifier.Operation operation = null;
        EquipmentSlot slot = null;

        in.beginObject();

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("uuid".equals(fieldName)) {
                in.peek();
                uuid = UUID.fromString(in.nextString());
            } else if ("name".equals(fieldName)) {
                in.peek();
                name = in.nextString();
            } else if ("amount".equals(fieldName)) {
                in.peek();
                amount = in.nextDouble();
            } else if ("operation".equals(fieldName)) {
                in.peek();
                operation = AttributeModifier.Operation.valueOf(in.nextString());
            } else if ("slot".equals(fieldName)) {
                in.peek();
                slot = EquipmentSlot.valueOf(in.nextString());
            }
        }

        in.endObject();

        if(uuid == null || name == null || operation == null)
            return null;
        return new AttributeModifier(uuid, name, amount, operation, slot);
    }
}
