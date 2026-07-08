package v.akfz.aslib.datagen.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import v.akfz.aslib.datagen.api.DataSerializable;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// генерирует sounds.json, нужно списком передавать звуки ( или через SoundRegistry )
public class SoundData extends DataSerializable {
    private final List<SoundDataEntry> entries = new ArrayList<>();
    public SoundData(String modId) {
        super(new ResourceLocation(modId, "sounds")); //.json сам генерирует
    }

    public void addEntry(SoundDataEntry entry) {
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public JsonElement serialize() {
        JsonObject rootJson = new JsonObject();

        for (SoundDataEntry entry : entries) {
            String eventKey = entry.soundID().getPath();
            JsonObject soundEventJson = new JsonObject();
            JsonArray soundsArray = new JsonArray();

            for (ResourceLocation soundFileId : entry.sounds()) {
                soundsArray.add(soundFileId.toString());
            }

            soundEventJson.add("sounds", soundsArray);

            entry.subtitle().ifPresent(sub -> soundEventJson.addProperty("subtitle", sub));
            rootJson.add(eventKey, soundEventJson);
        }

        return rootJson;
    }
}
