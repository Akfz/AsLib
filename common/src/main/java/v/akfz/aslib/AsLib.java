package v.akfz.aslib;

import java.util.List;
import java.util.ServiceLoader;

import v.akfz.aslib.command.CommandHandler;
import v.akfz.aslib.command.impl.DimensionCommand;
import v.akfz.aslib.event.api.EventBus;
import v.akfz.aslib.event.listener.TickUpdaterListener;
import v.akfz.aslib.initializer.generator.IRegistryLoader;
import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.bundle.BundleHeaderHandler;
import v.akfz.aslib.network.bundle.BundleHeaderPacket;
import v.akfz.aslib.network.bundle.BundlePayloadHandler;
import v.akfz.aslib.network.bundle.BundlePayloadPacket;
import v.akfz.db.generator.GenerateInitializer;
import v.akfz.db.generator.LoaderType;

//fix forge resourcepacks (не работают вроде)
@GenerateInitializer(loader = LoaderType.Both, modId = "aslib")
public final class AsLib {

    public static final EventBus EVENT_BUS = new EventBus();

    public static void init() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = AsLib.class.getClassLoader();
        }

        for (IRegistryLoader loader : ServiceLoader.load(IRegistryLoader.class, cl)) {
            try {
                loader.run();
            } catch (Throwable t) {
                System.err.println("[AsLib] Failed to execute registry loader: " + loader.getClass().getName());
                t.printStackTrace();
            }
        }

        EVENT_BUS.register(new TickUpdaterListener());

        AsLibNetworking.REGISTRY.register(new BundleHeaderPacket(0, List.of()), new BundleHeaderHandler());
        AsLibNetworking.REGISTRY.register(
                new BundlePayloadPacket(0, new byte[0]),
                new BundlePayloadHandler(AsLibNetworking.REGISTRY, AsLibNetworking.CODEC, AsLibNetworking.HANDLER)
        );

        CommandHandler.addCommand(new DimensionCommand());
    }
}