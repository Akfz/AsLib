package n.paradox.aslib.register;

// все регистрировать сюда
public final class AsLibRegistries {
    private static boolean isRegistered = false;

    private static final BlockRegistry blockRegistry = new BlockRegistry();
    private static final ItemRegistry itemRegistry = new ItemRegistry();
    private static final SoundRegistry soundRegistry = new SoundRegistry();
    private static final CommandRegistry commandRegistry = new CommandRegistry();

    public static BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    public static ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public static SoundRegistry getSoundRegistry() {
        return soundRegistry;
    }

    public static CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public static IRegistry[] getRegistries() {
        return new IRegistry[]{blockRegistry,itemRegistry,soundRegistry,commandRegistry};
    }

    public static void Init() {
        if (isRegistered) return;
        for (IRegistry reg : getRegistries()) {
            reg.register();
        }
        isRegistered = true;
    }
}
