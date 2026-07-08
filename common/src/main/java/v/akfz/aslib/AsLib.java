package v.akfz.aslib;

import v.akfz.aslib.event.api.EventBus;
import v.akfz.aslib.initializer.generator.GenerateInitializer;
import v.akfz.aslib.initializer.generator.IRegistryLoader;
import v.akfz.aslib.initializer.generator.InitializerClass;
import v.akfz.aslib.initializer.generator.LoaderType;

import java.util.ServiceLoader;

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