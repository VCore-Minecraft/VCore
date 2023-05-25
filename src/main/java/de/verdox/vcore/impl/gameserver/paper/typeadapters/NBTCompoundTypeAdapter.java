package de.verdox.vcore.impl.gameserver.paper.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;

import java.io.IOException;

public class NBTCompoundTypeAdapter extends TypeAdapter<ReadWriteNBT> {
    @Override
    public void write(JsonWriter out, ReadWriteNBT value) throws IOException {
        out.beginObject();
        if(value != null){
            out.name("nbt");
            out.value(value.toString());
        }
        out.endObject();
    }

    @Override
    public ReadWriteNBT read(JsonReader in) throws IOException {

        String fieldName = null;
        ReadWriteNBT readWriteNBT = NBT.createNBTObject();

        in.beginObject();

        while (in.hasNext()) {
            var token = in.peek();
            if (token.equals(JsonToken.NAME))
                fieldName = in.nextName();
            else if ("nbt".equals(fieldName)) {
                in.peek();
                readWriteNBT = NBT.parseNBT(in.nextString());
            }
        }

        in.endObject();
        return readWriteNBT;
    }
}
