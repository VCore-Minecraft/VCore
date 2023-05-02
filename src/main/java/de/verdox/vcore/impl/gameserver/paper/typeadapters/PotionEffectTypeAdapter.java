package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.Objects;

public class PotionEffectTypeAdapter extends TypeAdapter<PotionEffect> {
    @Override
    public void write(JsonWriter out, PotionEffect value) throws IOException {
        out.beginObject();
        out.name("duration");
        out.value(value.getDuration());
        out.name("amplifier");
        out.value(value.getAmplifier());
        out.name("type");
        out.value(value.getType().getName());
        out.name("hasIcon");
        out.value(value.hasIcon());
        out.name("hasParticles");
        out.value(value.hasParticles());
        out.endObject();
    }

    @Override
    public PotionEffect read(JsonReader in) throws IOException {
        String fieldName = null;

        var duration = 0;
        var amplifier = 0;
        PotionEffectType type = null;
        var hasIcon = false;
        var hasParticles = false;

        in.beginObject();

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("duration".equals(fieldName)) {
                in.peek();
                duration = in.nextInt();
            } else if ("amplifier".equals(fieldName)) {
                in.peek();
                amplifier = in.nextInt();
            } else if ("type".equals(fieldName)) {
                in.peek();
                type = PotionEffectType.getByName(in.nextString());
            } else if ("hasIcon".equals(fieldName)) {
                in.peek();
                hasIcon = in.nextBoolean();
            } else if ("hasParticles".equals(fieldName)) {
                in.peek();
                hasParticles = in.nextBoolean();
            }
        }

        in.endObject();

        if (type == null)
            return null;
        return new PotionEffect(type, duration, amplifier, hasParticles, hasIcon);
    }
}
