package v.akfz.aslib.datagen.sound;

import net.minecraft.resources.ResourceLocation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * soundID is the sound ID (like aslib:test), while sounds represents all the possible sounds that will be played (seemingly at random).
 */
public record SoundDataEntry(ResourceLocation soundID, Optional<String> subtitle, List<ResourceLocation> sounds) {

	public static SoundDataEntry of(ResourceLocation soundID, ResourceLocation... sounds) {
		return new SoundDataEntry(soundID, Optional.empty(), Arrays.asList(sounds));
	}

	public static SoundDataEntry of(ResourceLocation soundID, String subtitle, ResourceLocation... sounds) {
		return new SoundDataEntry(soundID, Optional.ofNullable(subtitle), Arrays.asList(sounds));
	}

	public static SoundDataEntry of(ResourceLocation soundID, List<ResourceLocation> sounds) {
		return new SoundDataEntry(soundID, Optional.empty(), sounds);
	}

	public static SoundDataEntry of(ResourceLocation soundID, String subtitle, List<ResourceLocation> sounds) {
		return new SoundDataEntry(soundID, Optional.ofNullable(subtitle), sounds);
	}
}