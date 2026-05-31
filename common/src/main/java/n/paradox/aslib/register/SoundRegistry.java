package n.paradox.aslib.register;

import n.paradox.aslib.datagen.sound.SoundDataEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.*;

// регистрирует звуки :/
// можно создать instance (для закрытой регистрации), но рекомендуется через AsLibRegistries, после register, добавить ничего нельзя, но можно взять id(зарегистрированные)
public final class SoundRegistry implements IRegistry {
    private final Map<String, ResourceLocation> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, ResourceLocation id звука (аля "aslib:test")
    private boolean isAllowToChange = true;

    public Map<String, ResourceLocation> getRegistryMap() {
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
        if (!isAllowToChange) return;
        isAllowToChange = false;

        for (ResourceLocation i : registryMap.values()) {
            if (i == null) {
                System.err.println("ASLib - SoundRegistry : REGISTER -> NULL ResourceLocation, SKIPPED");
                continue;
            }
            Registry.register(BuiltInRegistries.SOUND_EVENT, i, SoundEvent.createVariableRangeEvent(i));
        }
    }

    public void addIdentifier(String helperID, ResourceLocation id) {
        if (!isAllowToChange) return;
        if (id != null) {
            registryMap.put(helperID,id);
        } else {
            System.err.println("ASLib - SoundRegistry : Register " + helperID + " sound is failed, ResourceLocation is null");
        }
    }

    public void removeIdentifier(String helperID) {
        if (!isAllowToChange) return;
        if (registryMap.remove(helperID) == null) {
            System.err.println("ASLib - SoundRegistry : Cant remove " + helperID + " (sound), because its not exist");
        }
    }
}