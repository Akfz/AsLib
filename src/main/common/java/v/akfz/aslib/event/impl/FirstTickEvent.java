package v.akfz.aslib.event.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.event.api.Event;

/**
 * First tick event (you can implement, but dont call it)
 * Guide about events {@link Event}
 */
public class FirstTickEvent extends Event {
	@Nullable private final Minecraft clientMc;
	@Nullable private final MinecraftServer server;

	public final boolean client;

	public FirstTickEvent(@Nullable Minecraft client, @Nullable MinecraftServer server) {
		this.clientMc = client;
		this.server = server;
		this.client = client != null;
	}

	public static FirstTickEvent client(Minecraft mc) {
		return new FirstTickEvent(mc, null);
	}

	public static FirstTickEvent server(MinecraftServer srv) {
		return new FirstTickEvent(null, srv);
	}

	@Nullable public Minecraft getClient() { return clientMc; }
	@Nullable public MinecraftServer getServer() { return server; }
}
