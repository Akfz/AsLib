package n.paradox.aslib.register;

import n.paradox.aslib.register.util.FastBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//Регистрирует блоки и блок энтити, создавать instance и передавать в него списки
//после register, добавить ничего нельзя, но можно взять instance(зарегистрированные)
public final class BlockRegistry implements IRegistry {
    private final Map<String, BlockGroup> registryMap = new HashMap<>(); //String - хелпер, он НЕ влияет на реггер, Блоки должны наследоваться RegisterObject

    public record BlockGroup(
            Block block,
            Optional<BlockItem> blockItem,
            Optional<BlockEntityExpander> blockEntity
    ) {}
    public record BlockEntityExpander(BlockEntityType<?> blockEntityType, ResourceLocation blockEntityID){}

    private static final Logger logger = LoggerFactory.getLogger("ASLib - BlockRegistry");

    private boolean isAllowToChange = true;

    public Map<String, BlockGroup> getRegistryMap() {
        return Collections.unmodifiableMap(this.registryMap);
    }

    @Override
    public boolean isAllowToChange() {
        return isAllowToChange;
    }

    @Override
    public void register() {
        isAllowToChange = false;

        // регистрирует : если блок не null и если имплементирует в любом виде RegisterObject
        // если есть предмет, то регистрирует -> если имплементирует в любом виде RegisterObject, то без изменений
        // если НЕ имплементирует,но и не null, то сгенерируется имя, на основе блока, но если blockItem null,
        // тогда генерирует дефолтный.
        // если есть BlockEntity, регистрирует в любом случае
        for (BlockGroup group : registryMap.values()) {
            Block block = group.block();
            if (block == null) {
                logger.error("REGISTER -> NULL BLOCK, SKIPPED");
                continue;
            }
            if (!(block instanceof RegisterObject regBlock)) {
                logger.error("REGISTER -> NON RegisterObject BLOCK, SKIPPED");
                continue;
            }
            String nameBlock = regBlock.getRegisterName();
            String idBlock=regBlock.getModId();
            ResourceLocation id = new ResourceLocation(idBlock, nameBlock);
            Registry.register(BuiltInRegistries.BLOCK, id, block);

            group.blockEntity().ifPresent(blockEntityExpander ->
                    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityExpander.blockEntityID(), blockEntityExpander.blockEntityType()));

            if (group.blockItem().isPresent()) {
                BlockItem customItem = group.blockItem().get();

                if (customItem instanceof RegisterObject regItem) {
                    Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(regItem.getModId(), regItem.getRegisterName()), customItem);
                } else {
                    Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(idBlock, nameBlock + "_item"), customItem);
                }
            } else {
                FastBlockItem autoItem = new FastBlockItem(block, new Item.Properties(), nameBlock + "_item", idBlock);
                Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(autoItem.getModId(), autoItem.getRegisterName()), autoItem);
            }
        }
    }

    public void addBlock(String helperId, Block block) {
        if (!isAllowToChange) return;
        if (block == null) {
            logger.error("Register {} block is failed, block is null", helperId);
            return;
        }

        BlockGroup existing = registryMap.get(helperId);
        if (existing != null) {
            registryMap.put(helperId, new BlockGroup(block, existing.blockItem(), existing.blockEntity()));
        } else {
            registryMap.put(helperId, new BlockGroup(block, Optional.empty(), Optional.empty()));
        }
    }

    public void addBlockItem(String helperId, BlockItem blockItem) {
        if (!isAllowToChange) return;
        if (blockItem == null) {
            logger.error("Register {} blockItem is failed, blockItem is null", helperId);
            return;
        }

        BlockGroup existing = registryMap.get(helperId);
        if (existing != null) {
            registryMap.put(helperId, new BlockGroup(existing.block(), Optional.of(blockItem), existing.blockEntity()));
        } else {
            registryMap.put(helperId, new BlockGroup(null, Optional.of(blockItem), Optional.empty()));
        }
    }

    public void addBlockEntity(String helperId, BlockEntityExpander blockEntityExpander) {
        if (!isAllowToChange) return;
        if (blockEntityExpander == null || blockEntityExpander.blockEntityType() == null) {
            logger.error("Register {} blockEntity is failed, blockEntityExpander is null", helperId);
            return;
        }

        BlockGroup existing = registryMap.get(helperId);
        if (existing != null) {
            registryMap.put(helperId, new BlockGroup(existing.block(), existing.blockItem(), Optional.of(blockEntityExpander)));
        } else {
            registryMap.put(helperId, new BlockGroup(null, Optional.empty(), Optional.of(blockEntityExpander)));
        }
    }

    public void removeBlock(String helperId) {
        if (!isAllowToChange) return;

        BlockGroup existing = registryMap.get(helperId);
        if (existing == null || existing.block() == null) {
            logger.error("Cant remove {} (block), because its not exist", helperId);
            return;
        }

        if (existing.blockItem().isEmpty() && existing.blockEntity().isEmpty()) {
            registryMap.remove(helperId);
        } else {
            registryMap.put(helperId, new BlockGroup(null, existing.blockItem(), existing.blockEntity()));
        }
    }

    public void removeBlockItem(String helperId) {
        if (!isAllowToChange) return;

        BlockGroup existing = registryMap.get(helperId);
        if (existing == null || existing.blockItem().isEmpty()) {
            logger.error("Cant remove {} (blockItem), because its not exist", helperId);
            return;
        }

        if (existing.block() == null && existing.blockEntity().isEmpty()) {
            registryMap.remove(helperId);
        } else {
            registryMap.put(helperId, new BlockGroup(existing.block(), Optional.empty(), existing.blockEntity()));
        }
    }

    public void removeEntityBlock(String helperId) {
        if (!isAllowToChange) return;

        BlockGroup existing = registryMap.get(helperId);
        if (existing == null || existing.blockEntity().isEmpty()) {
            logger.error("Cant remove {} (blockEntity), because its not exist", helperId);
            return;
        }

        if (existing.block() == null && existing.blockItem().isEmpty()) {
            registryMap.remove(helperId);
        } else {
            registryMap.put(helperId, new BlockGroup(existing.block(), existing.blockItem(), Optional.empty()));
        }
    }
}
