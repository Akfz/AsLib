package n.paradox.aslib.register;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Регистрирует предметы (blockitem лучше через BlockRegistry), создавать instance и передавать в него списки
//после register, добавить ничего нельзя, но можно взять instance(зарегистрированные)
public final class ItemRegistry implements IRegistry {
    private final Map<String, Item> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, Предметы должны наследоваться RegisterObject
    private boolean isAllowToChange = true;
    private static final Logger logger = LoggerFactory.getLogger("ASLib - ItemRegistry");

    public Map<String, Item> getRegistryMap() {
        return Collections.unmodifiableMap(this.registryMap);
    }

    @Override
    public boolean isAllowToChange() {
        return isAllowToChange;
    }

    @Override
    public void register() {
        isAllowToChange = false;

        for (Item si : registryMap.values()) {
            if (si == null) {
                logger.error("REGISTER -> NULL ITEM, SKIPPED");
                continue;
            }
            if (si instanceof RegisterObject i) {
                Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(i.getModId(), i.getRegisterName()), si);
            } else {
                logger.error("REGISTER -> ITEM NOT INSTANCE OF RegisterObject, SKIPPED");
            }
        }
    }

    public void addItem(String helperID, Item item) {
        if (!isAllowToChange) return;
        if (item != null) {
            registryMap.put(helperID,item);
        } else {
            logger.error("Register {} item is failed, item is null", helperID);
        }
    }

    public void removeItem(String helperID) {
        if (!isAllowToChange) return;
        if (registryMap.remove(helperID) == null) {
            logger.error("Cant remove {} (item), because its not exist", helperID);
        }
    }
}
