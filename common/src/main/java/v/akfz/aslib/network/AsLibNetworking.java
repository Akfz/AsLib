package v.akfz.aslib.network;

import v.akfz.aslib.network.codec.PacketCodec;
import v.akfz.aslib.network.registry.PacketRegistry;
import v.akfz.aslib.network.registry.PacketRegistryHandler;

/**
 * Main entry point for the AsLib networking system.
 * Provides access to packet registration, network transport, codec, and handling.
 *
 * WARNING! NETWORKING ON VERSION 1 IS TESTING, IT MAY BE UNSTABLE!
 */
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