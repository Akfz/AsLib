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

/**
 * Utility class providing helper methods for instantiating Minecraft registry objects
 * such as entities, block entities, sound events, properties, and creative tabs.
 */
public final class RegistryHelper {

    private RegistryHelper() {}

    private static final Class<?> SUPPLIER_CLASS;
    private static final Method OF_METHOD;

    static {
        Class<?> tempClass = null;
        Method tempMethod = null;
        try {
            String[] possibleNames = {
                    "net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier",
                    "net.minecraft.block.entity.BlockEntityType$BlockEntityFactory"
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

    /**
     * Functional interface used to construct new {@link BlockEntity} instances.
     *
     * @param <T> Type of the block entity.
     */
    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    /**
     * Creates a standard {@link EntityType} builder and builds the entity type.
     *
     * @param factory  Factory creating the entity instance.
     * @param category Classification category for spawning and behavior.
     * @param width    Hitbox width.
     * @param height   Hitbox height.
     * @param key      Entity type registry path key.
     * @param <T>      Entity type class.
     * @return Constructed {@link EntityType}.
     */
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

    /**
     * Creates an {@link EntityType} with customized network tracking range and update interval settings.
     *
     * @param factory        Factory creating the entity instance.
     * @param category       Classification category.
     * @param width          Hitbox width.
     * @param height         Hitbox height.
     * @param updateInterval Tick frequency of entity position updates sent to clients.
     * @param trackingRange  Distance in chunks at which players receive entity updates.
     * @param key            Entity type registry path key.
     * @param <T>            Entity type class.
     * @return Constructed {@link EntityType}.
     */
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

    /**
     * Dynamically instantiates a {@link BlockEntityType} using dynamic proxying
     * to ensure compatibility across different mapping environments (Mojang / Yarn).
     *
     * @param factory Factory creating the block entity instance.
     * @param blocks  Valid blocks bound to this block entity type.
     * @param <T>      Block entity type class.
     * @return Constructed {@link BlockEntityType}.
     */
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

    /**
     * Creates a variable-range {@link SoundEvent} from a string identifier.
     *
     * @param id String representation of the sound ResourceLocation.
     * @return New {@link SoundEvent}.
     */
    public static SoundEvent createSound(String id) {
        return createSound(new ResourceLocation(id));
    }

    /**
     * Creates a variable-range {@link SoundEvent}.
     *
     * @param rl ResourceLocation identifier of the sound.
     * @return New {@link SoundEvent}.
     */
    public static SoundEvent createSound(ResourceLocation rl) {
        return SoundEvent.createVariableRangeEvent(rl);
    }

    /**
     * Creates a fixed-range {@link SoundEvent}.
     *
     * @param rl    ResourceLocation identifier of the sound.
     * @param range Maximum audible distance in blocks.
     * @return New {@link SoundEvent}.
     */
    public static SoundEvent createFixedSound(ResourceLocation rl, float range) {
        return SoundEvent.createFixedRangeEvent(rl, range);
    }

    /**
     * Instantiates new default {@link BlockBehaviour.Properties}.
     *
     * @return New block properties instance.
     */
    public static BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.of();
    }

    /**
     * Instantiates new default {@link Item.Properties}.
     *
     * @return New item properties instance.
     */
    public static Item.Properties itemProperties() {
        return new Item.Properties();
    }

    /**
     * Builds and returns a new {@link CreativeModeTab}.
     *
     * @param row          Tab row placement in the creative menu.
     * @param column       Tab column placement in the creative menu.
     * @param title        Displayed tab name.
     * @param icon         Supplier providing the tab icon stack.
     * @param displayItems Display items generator logic.
     * @return Constructed {@link CreativeModeTab}.
     */
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