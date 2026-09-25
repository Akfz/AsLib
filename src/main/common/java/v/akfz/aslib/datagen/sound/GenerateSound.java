package v.akfz.aslib.datagen.sound;

import v.akfz.aslib.datagen.api.DataProvider;

/**
 * DataProvider wrapper for {@link SoundData}.
 * <p>
 * Usage example:
 * <pre>
 * SoundData soundData = new SoundData("mymod")
 *     .addEntry(SoundDataEntry.of(new ResourceLocation("mymod", "sound1"), new ResourceLocation("mymod", "sounds/sound1")))
 *     .addEntry(SoundDataEntry.of(new ResourceLocation("mymod", "sound2"), "My Mod: Sound 2", new ResourceLocation("mymod", "sounds/sound2")));
 *
 * new GenerateSound(soundData).run("common");
 * </pre>
 */
public class GenerateSound extends DataProvider {
	private final SoundData data;

	public GenerateSound(SoundData data) {
		this.data = data;
	}

	@Override
	protected void registerDataSerializable() {
		add(data);
	}
}