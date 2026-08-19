package v.akfz.aslib.datagen.sound;


import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 *  soundID is the sound ID (like aslib:test), while sounds represents all the possible sounds that will be played (seemingly at random).
 */
public record SoundDataEntry(ResourceLocation soundID, Optional<String> subtitle, List<ResourceLocation> sounds){}
