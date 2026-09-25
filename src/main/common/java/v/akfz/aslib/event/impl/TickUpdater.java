package v.akfz.aslib.event.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.event.api.Event;

/**
 * So so so.. What we have here..
 * <p>
 * Guide about events {@link Event}
 */
public class TickUpdater extends Event {

    @Nullable private final Minecraft clientMc;
    @Nullable private final MinecraftServer server;

    public final boolean client;

    public TickUpdater(@Nullable Minecraft client, @Nullable MinecraftServer server) {
        this.clientMc = client;
        this.server = server;
        this.client = client != null;
    }

    public static TickUpdater client(Minecraft mc) {
        return new TickUpdater(mc, null);
    }

    public static TickUpdater server(MinecraftServer srv) {
        return new TickUpdater(null, srv);
    }

    @Nullable public Minecraft getClient() { return clientMc; }
    @Nullable public MinecraftServer getServer() { return server; }
}
