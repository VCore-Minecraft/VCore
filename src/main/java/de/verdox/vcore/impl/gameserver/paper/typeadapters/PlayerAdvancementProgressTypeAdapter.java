package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.verdox.vcore.impl.gameserver.paper.data.PlayerAdvancementProgress;
import de.verdox.vpipeline.api.NetworkLogger;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PlayerAdvancementProgressTypeAdapter extends TypeAdapter<PlayerAdvancementProgress> {
    @Override
    public void write(JsonWriter out, PlayerAdvancementProgress value) throws IOException {
        out.beginObject();
        out.name("key");
        out.value(value.advancementKey().toString());
        out.name("progress");
        out.beginArray();
        for (String awardedCriterion : value.awardedCriteria())
            out.value(awardedCriterion);
        out.endArray();
        out.endObject();
    }

    @Override
    public PlayerAdvancementProgress read(JsonReader in) throws IOException {
        String fieldName = null;

        NamespacedKey key = null;
        Set<String> awardedCriteria = new HashSet<>();

        in.beginObject();

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("key".equals(fieldName)) {
                in.peek();
                var split = in.nextString().split(":");
                key = new NamespacedKey(split[0], split[1]);
            } else if ("progress".equals(fieldName)) {
                in.peek();
                in.beginArray();
                while (in.hasNext()) {
                    in.peek();
                    var criterion = in.nextString();
                    awardedCriteria.add(criterion);
                }
                in.endArray();
            }
        }

        in.endObject();

        if (key == null)
            return null;
        return new PlayerAdvancementProgress(key, awardedCriteria);
    }
}
