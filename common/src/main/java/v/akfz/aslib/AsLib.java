package v.akfz.aslib;

import v.akfz.aslib.command.CommandHandler;
import v.akfz.aslib.command.impl.DimensionCommand;
import v.akfz.aslib.event.api.EventBus;
import v.akfz.aslib.event.listener.TickUpdaterListener;
import v.akfz.aslib.initializer.LoaderEnvironment;
import v.akfz.aslib.initializer.generator.GenerateInitializer;
import v.akfz.aslib.initializer.generator.IRegistryLoader;
import v.akfz.aslib.initializer.generator.InitializerClass;
import v.akfz.aslib.initializer.generator.LoaderType;
import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.bundle.BundleHeaderHandler;
import v.akfz.aslib.network.bundle.BundleHeaderPacket;
import v.akfz.aslib.network.bundle.BundlePayloadHandler;
import v.akfz.aslib.network.bundle.BundlePayloadPacket;

import java.util.List;
import java.util.ServiceLoader;

@GenerateInitializer(loader = LoaderType.Both, modId = "aslib")
public final class AsLib implements InitializerClass {
    public static final EventBus EVENT_BUS = new EventBus();

    @Override
    public void init() {
        EVENT_BUS.register(new TickUpdaterListener());

        AsLibNetworking.REGISTRY.register(new BundleHeaderPacket(0, List.of()), new BundleHeaderHandler());
        AsLibNetworking.REGISTRY.register(
                new BundlePayloadPacket(0, new byte[0]),
                new BundlePayloadHandler(AsLibNetworking.REGISTRY, AsLibNetworking.CODEC, AsLibNetworking.HANDLER)
        );

        //вообще лучше через GenerateRegistries и RegisterModule, но можно и напрямую
        CommandHandler.addCommand(new DimensionCommand());

        if (!LoaderEnvironment.getFastLoader().isForgeLike()) {
            try {
                ServiceLoader.load(IRegistryLoader.class, AsLib.class.getClassLoader()).forEach(Runnable::run);
            } catch (Exception e) {
                System.err.println("[AsLib] Failed to run automated SPI registrars: " + e.getMessage());
            }
        }
    }
}