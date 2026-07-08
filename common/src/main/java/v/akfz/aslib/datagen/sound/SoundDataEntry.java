package v.akfz.aslib.datagen.sound;


import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

// soundID, айди звука (аля aslib:test), а sounds это все возможные звуки которые воспроизведет (вроде как рандомно)
public record SoundDataEntry(ResourceLocation soundID, Optional<String> subtitle, List<ResourceLocation> sounds){}
