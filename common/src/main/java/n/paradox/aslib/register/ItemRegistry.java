package n.paradox.aslib.register;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Регистрирует предметы (blockitem лучше через BlockRegistry), можно создать instance (для закрытой регистрации)
//но рекомендуется через AsLibRegistries, после register добавить ничего нельзя, но можно взять instance(зарегистрированные)
public final class ItemRegistry implements IRegistry {
    private final Map<String, Item> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, Предметы должны наследоваться RegisterObject
    private boolean isAllowToChange = true;

    public Map<String, Item> getRegistryMap() {
        return Collections.unmodifiableMap(this.registryMap);
    }

    @Override
    public boolean isAllowToChange() {
        return isAllowToChange;
    }

    @Override
    public void register() {
        if (!isAllowToChange) return;
        isAllowToChange = false;

        for (Item si : registryMap.values()) {
            if (si == null) {
                System.err.println("ASLib - ItemRegistry : REGISTER -> NULL ITEM, SKIPPED");
                continue;
            }
            if (si instanceof RegisterObject i) {
                Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(i.getModId(), i.getRegisterName()), si);
            } else {
                System.err.println("ASLib - ItemRegistry : REGISTER -> ITEM NOT INSTANCE OF RegisterObject, SKIPPED");
            }
        }
    }

    public void addItem(String helperID, Item item) {
        if (!isAllowToChange) return;
        if (item != null) {
            registryMap.put(helperID,item);
        } else {
            System.err.println("ASLib - ItemRegistry : Register " + helperID + " item is failed, item is null");
        }
    }

    public void removeItem(String helperID) {
        if (!isAllowToChange) return;
        if (registryMap.remove(helperID) == null) {
            System.err.println("ASLib - ItemRegistry : Cant remove " + helperID + " (item), because its not exist");
        }
    }
}