package n.paradox.aslib.network;

import n.paradox.aslib.network.registry.PacketCodec;
import n.paradox.aslib.network.registry.PacketRegistry;
import n.paradox.aslib.network.registry.PacketRegistryHandler;

public class AsLibNetworking {
    public static final PacketRegistry REGISTRY = new PacketRegistry();
    public static final NetworkTransport SENDER;
    public static final PacketCodec CODEC;
    public static final PacketRegistryHandler HANDLER;

    static {
        SENDER = new NetworkTransport(REGISTRY);
        CODEC = new PacketCodec(REGISTRY);
        HANDLER = new PacketRegistryHandler(REGISTRY);
    }
}
