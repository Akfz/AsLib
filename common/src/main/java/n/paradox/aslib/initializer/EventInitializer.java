package n.paradox.aslib.initializer;

public interface EventInitializer {
    default void preInit() {}
    default void Init() {}
    default void onStarted() {}
    default void onStopped() {}
}

