package v.akfz.aslib;

import java.util.List;

import v.akfz.aslib.command.CommandHandler;
import v.akfz.aslib.command.impl.DimensionCommand;
import v.akfz.aslib.event.api.EventBus;
import v.akfz.aslib.event.listener.TickUpdaterListener;
import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.bundle.BundleHeaderHandler;
import v.akfz.aslib.network.bundle.BundleHeaderPacket;
import v.akfz.aslib.network.bundle.BundlePayloadHandler;
import v.akfz.aslib.network.bundle.BundlePayloadPacket;
import v.akfz.db.generator.GenerateInitializer;
import v.akfz.db.generator.LoaderType;

@GenerateInitializer(loader = LoaderType.Both, modId = "aslib")
public final class AsLib {

    public static final EventBus EVENT_BUS = new EventBus();

    public void init() {
        EVENT_BUS.register(new TickUpdaterListener());

        AsLibNetworking.REGISTRY.register(new BundleHeaderPacket(0, List.of()), new BundleHeaderHandler());
        AsLibNetworking.REGISTRY.register(
                new BundlePayloadPacket(0, new byte[0]),
                new BundlePayloadHandler(AsLibNetworking.REGISTRY, AsLibNetworking.CODEC, AsLibNetworking.HANDLER)
        );

        CommandHandler.addCommand(new DimensionCommand());
    }
}
