package v.akfz.aslib.resourcepack;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource reload listener registry. Register a callback here and it fires
 * on every resource/data reload.
 * <p>
 * Usage:
 * <pre>{@code
 * ReloadHelper.onReload("mymod", () -> MyData.reload());
 * // or with the resource manager:
 * ReloadHelper.onReload("mymod", mgr -> MyData.reload(mgr));
 * }</pre>
 */
public final class ReloadHelper {

	private static final Map<String, ResourceReloadListener> globalListeners = new ConcurrentHashMap<>();

	private ReloadHelper() {}

	public static void onReload(String id, ResourceReloadListener listener) {
		globalListeners.put(id, listener);
	}

	public static void onReload(String id, Runnable action) {
		globalListeners.put(id, mgr -> action.run());
	}

	public static void onReload(ResourceReloadListener listener) {
		onReload("listener_" + System.identityHashCode(listener), listener);
	}

	public static void unregister(String id) {
		globalListeners.remove(id);
	}

	static Collection<ResourceReloadListener> getGlobalListeners() {
		return globalListeners.values();
	}
}