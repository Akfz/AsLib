package n.paradox.aslib;

import n.paradox.aslib.event.api.EventBus;
import n.paradox.aslib.initializer.generator.GenerateInitializer;
import n.paradox.aslib.initializer.generator.IRegistryLoader;
import n.paradox.aslib.initializer.generator.InitializerClass;
import n.paradox.aslib.initializer.generator.LoaderType;
import n.paradox.aslib.event.listener.ExecutionSideListener;
import n.paradox.aslib.event.listener.FirstTickListener;

import java.util.ServiceLoader;

//MainClass всегда в sharedCode
@GenerateInitializer(loader = LoaderType.Both, modId = "aslib")
public final class AsLib implements InitializerClass {
    public static final EventBus EVENT_BUS = new EventBus();

    @Override
    public void init() {
        //EVENT_BUS.register(new FirstTickListener());
        //EVENT_BUS.register(new ExecutionSideListener());

        //это копировать не нужно, оно работает глобально
        try {
            ServiceLoader.load(IRegistryLoader.class, AsLib.class.getClassLoader()).forEach(Runnable::run);
        } catch (Exception e) {
            System.err.println("[AsLib] Failed to run automated Fabric SPI registrars: " + e.getMessage());
        }
    }
}