package v.akfz.aslib.util;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Global access to the currently running {@link MinecraftServer}.
 * <p>
 * For common code that needs a server reference but can't be bothered with
 * listener plumbing. Side-safe: on a client without a world loaded, returns null.
 * <p>
 * Lifecycle is maintained by {@code MinecraftSrvMixin} — set on construction,
 * cleared on stop. Don't call the setters yourself unless you know what you're
 * doing.
 */
public final class ServerHolder {

	private static volatile MinecraftServer server;

	private ServerHolder() {}

	@Nullable
	public static MinecraftServer get() {
		return server;
	}

	public static boolean isRunning() {
		return server != null;
	}

	public static MinecraftServer require() {
		MinecraftServer s = server;
		if (s == null) {
			throw new IllegalStateException("Server is not running");
		}
		return s;
	}

	public static void set(MinecraftServer s) {
		server = s;
	}

	public static void clear() {
		server = null;
	}
}