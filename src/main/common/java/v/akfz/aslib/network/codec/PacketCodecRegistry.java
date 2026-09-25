package v.akfz.aslib.network.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-class codec registry. One codec per class, cached.
 * <p>
 * If a class isn't registered, {@link #codecFor} falls back to
 * {@link PacketAutoCodec} — reflection with annotations.
 * <p>
 * Nested objects inside {@code PacketAutoCodec} go through this registry,
 * so custom codecs registered here are used everywhere the type appears.
 */
public final class PacketCodecRegistry {

	private static final Map<Class<?>, PacketTypeCodec<?>> BY_TYPE = new ConcurrentHashMap<>();

	private PacketCodecRegistry() {}

	public static <T> void register(Class<T> type, PacketTypeCodec<T> codec) {
		BY_TYPE.put(type, codec);
	}

	public static void unregister(Class<?> type) {
		BY_TYPE.remove(type);
	}

	public static boolean isRegistered(Class<?> type) {
		return BY_TYPE.containsKey(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> PacketTypeCodec<T> get(Class<T> type) {
		return (PacketTypeCodec<T>) BY_TYPE.get(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> PacketTypeCodec<T> codecFor(Class<T> type) {
		PacketTypeCodec<T> explicit = (PacketTypeCodec<T>) BY_TYPE.get(type);
		if (explicit != null) return explicit;
		return PacketAutoCodec.of(type);
	}

	public static void clear() {
		BY_TYPE.clear();
	}
}