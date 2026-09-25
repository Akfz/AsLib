package v.akfz.aslib.util.af.registry;

import v.akfz.aslib.util.af.codec.BinaryAutoCodec;
import v.akfz.aslib.util.af.codec.BinaryCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry of {@link BinaryCodec}s keyed by class.
 * <p>
 * Used by {@link v.akfz.aslib.util.af.BinaryHelper} for top-level values and
 * by {@link v.akfz.aslib.util.af.codec.BinaryAutoCodec} for nested fields.
 * <p>
 * There's no id — files store the class name instead, so renaming a class
 * breaks read compatibility. That's the trade we make for not maintaining
 * a global id catalog.
 * <p>
 * Lookup order:
 * <ol>
 *   <li>codec explicitly registered here</li>
 *   <li>fallback to {@link BinaryAutoCodec} (reflection)</li>
 * </ol>
 */
public final class BinaryRegistry {

	private static final Map<Class<?>, BinaryCodec<?>> BY_TYPE = new ConcurrentHashMap<>();

	private BinaryRegistry() {}

	public static <T> void register(Class<T> type, BinaryCodec<T> codec) {
		BY_TYPE.put(type, codec);
	}

	public static void unregister(Class<?> type) {
		BY_TYPE.remove(type);
	}

	public static boolean isRegistered(Class<?> type) {
		return BY_TYPE.containsKey(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryCodec<T> get(Class<T> type) {
		return (BinaryCodec<T>) BY_TYPE.get(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryCodec<T> codecFor(Class<T> type) {
		BinaryCodec<T> explicit = (BinaryCodec<T>) BY_TYPE.get(type);
		if (explicit != null) return explicit;
		return (BinaryCodec<T>) BinaryAutoCodec.get(type);
	}

	public static void clear() {
		BY_TYPE.clear();
	}
}