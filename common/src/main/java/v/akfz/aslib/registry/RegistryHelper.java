package v.akfz.aslib.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public final class RegistryHelper {

    private RegistryHelper() {}

    // ебанный рот
    private static final Class<?> SUPPLIER_CLASS;
    private static final Method OF_METHOD;

    static {
        Class<?> tempClass = null;
        Method tempMethod = null;
        try {
            String[] possibleNames = {
                    "net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier",
                    "net.minecraft.block.entity.BlockEntityType$BlockEntityFactory"              // хз зачем, но пусть будет и yarn
            };

            for (String name : possibleNames) {
                try {
                    tempClass = Class.forName(name);
                    break;
                } catch (ClassNotFoundException ignored) {}
            }

            if (tempClass == null) {
                throw new IllegalStateException("[AsLib] Could not find BlockEntitySupplier or BlockEntityFactory class");
            }

            tempMethod = BlockEntityType.Builder.class.getMethod("of", tempClass, Block[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SUPPLIER_CLASS = tempClass;
        OF_METHOD = tempMethod;
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    public static <T extends Entity> EntityType<T> createEntity(
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            float width,
            float height,
            String key
    ) {
        return EntityType.Builder.of(factory, category)
                .sized(width, height)
                .build(key);
    }

    public static <T extends Entity> EntityType<T> createEntity(
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            float width,
            float height,
            int updateInterval,
            int trackingRange,
            String key
    ) {
        return EntityType.Builder.of(factory, category)
                .sized(width, height)
                .clientTrackingRange(trackingRange)
                .updateInterval(updateInterval)
                .build(key);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntity(
            BlockEntityFactory<T> factory,
            Block... blocks
    ) {
        if (SUPPLIER_CLASS == null || OF_METHOD == null) {
            throw new IllegalStateException("[AsLib] RegistryHelper reflection fields are not initialized!");
        }
        try {
            Object supplierProxy = Proxy.newProxyInstance(
                    BlockEntityType.class.getClassLoader(),
                    new Class<?>[]{SUPPLIER_CLASS},
                    (proxy, method, args) -> factory.create((BlockPos) args[0], (BlockState) args[1])
            );

            BlockEntityType.Builder<T> builder = (BlockEntityType.Builder<T>) OF_METHOD.invoke(null, supplierProxy, blocks);
            return builder.build(null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to dynamically instantiate BlockEntityType", e);
        }
    }

    public static SoundEvent createSound(String id) {
        return createSound(new ResourceLocation(id));
    }

    public static SoundEvent createSound(ResourceLocation rl) {
        return SoundEvent.createVariableRangeEvent(rl);
    }

    public static SoundEvent createFixedSound(ResourceLocation rl, float range) {
        return SoundEvent.createFixedRangeEvent(rl, range);
    }

    public static BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.of();
    }

    public static Item.Properties itemProperties() {
        return new Item.Properties();
    }

    public static CreativeModeTab createCreativeTab(
            CreativeModeTab.Row row,
            int column,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems
    ) {
        return CreativeModeTab.builder(row, column)
                .title(title)
                .icon(icon)
                .displayItems(displayItems)
                .build();
    }
}