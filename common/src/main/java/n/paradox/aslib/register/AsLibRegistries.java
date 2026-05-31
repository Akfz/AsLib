package n.paradox.aslib.register;

import n.paradox.aslib.AsLib;

import java.util.Objects;

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
        Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> Objects.requireNonNull(stream
                        .skip(1)
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .orElse(null)));

        if (callerClass == null || !AsLib.class.isAssignableFrom(callerClass)) {
            String callerName = callerClass != null ? callerClass.getName() : "Unknown";
            throw new SecurityException("Calling the Init() method is prohibited for the class: " + callerName);
        }

        for (IRegistry reg : getRegistries()) {
            reg.register();
        }
        isRegistered = true;
    }
}
