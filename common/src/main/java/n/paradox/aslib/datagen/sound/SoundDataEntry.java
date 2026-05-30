package n.paradox.aslib.datagen.sound;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

// soundID, айди звука (аля aslib:test), а sounds это все возможные звуки которые воспроизведет (вроде как рандомно)
public record SoundDataEntry(Identifier soundID, Optional<String> subtitle, List<Identifier> sounds){}
