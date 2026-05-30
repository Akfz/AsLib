package n.paradox.aslib.register;

import n.paradox.aslib.datagen.sound.SoundDataEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// регистрирует звуки :/
// создавать instance и передавать в него списки, после register, добавить ничего нельзя, но можно взять id(зарегистрированные)
public final class SoundRegistry implements IRegistry {
    private final Map<String, Identifier> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, Identifier id звука (аля "aslib:test")
    private boolean isAllowToChange = true;
    private static final Logger logger = LoggerFactory.getLogger("ASLib - SoundRegistry");

    public Map<String, Identifier> getRegistryMap() {
        return Collections.unmodifiableMap(this.registryMap);
    }

    // для SoundData(генерирует sounds.json), только зарегистрированные выводит
    // без субтитров, звук тот же что и айди. МОЖЕТ БЫТЬ ПУСТ!
    public List<SoundDataEntry> generateSoundDataEntries() {
        return registryMap.values().stream()
                .filter(Objects::nonNull)
                .map(id -> new SoundDataEntry(id, Optional.empty(), List.of(id)))
                .toList();
    }

    @Override
    public boolean isAllowToChange() {
        return isAllowToChange;
    }

    @Override
    public void register() {
        isAllowToChange = false;

        for (Identifier i : registryMap.values()) {
            if (i == null) {
                logger.error("REGISTER -> NULL IDENTIFIER, SKIPPED");
                continue;
            }
            Registry.register(Registries.SOUND_EVENT, i, SoundEvent.of(i));
        }
    }

    public void addIdentifier(String helperID, Identifier id) {
        if (!isAllowToChange) return;
        if (id != null) {
            registryMap.put(helperID,id);
        } else {
            logger.error("Register {} sound is failed, identifier is null", helperID);
        }
    }

    public void removeIdentifier(String helperID) {
        if (!isAllowToChange) return;
        if (registryMap.remove(helperID) == null) {
            logger.error("Cant remove {} (sound), because its not exist", helperID);
        }
    }
}
