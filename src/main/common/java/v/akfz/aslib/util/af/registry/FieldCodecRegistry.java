package v.akfz.aslib.util.af.registry;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.codec.BinaryAutoCodec;
import v.akfz.aslib.util.af.codec.BinaryCodec;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for per-field codecs. Overrides the default behaviour of
 * {@link BinaryAutoCodec} for specific fields.
 * <p>
 * Key is {@code (declaringClass, fieldName)} — not the raw {@link Field},
 * because two different {@code getDeclaredField} calls on the same field
 * return non-equal objects.
 * <p>
 * Registering the same field twice overwrites the previous codec — the
 * last write wins. This is intentional: it makes module overrides easy.
 * <p>
 * Registering (or clearing) invalidates {@link BinaryAutoCodec}'s cache so the
 * new codec is picked up by existing and future {@code BinaryAutoCodec} instances.
 * <p>
 * <b>Use cases:</b> store a {@code Duration} as {@code long},
 * serialize a class with {@code final} fields, custom compact encodings.
 */
public final class FieldCodecRegistry {

	private static final Map<FieldKey, BinaryCodec<?>> BY_FIELD = new ConcurrentHashMap<>();

	private FieldCodecRegistry() {}

	public record FieldKey(Class<?> owner, String fieldName) {}

	public static <T> void register(Class<?> owner, String fieldName, BinaryCodec<T> codec) {
		BY_FIELD.put(new FieldKey(owner, fieldName), codec);
		BinaryAutoCodec.clearCache();
	}

	public static void unregister(Class<?> owner, String fieldName) {
		BY_FIELD.remove(new FieldKey(owner, fieldName));
		BinaryAutoCodec.clearCache();
	}

	public static boolean isRegistered(Class<?> owner, String fieldName) {
		return BY_FIELD.containsKey(new FieldKey(owner, fieldName));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> BinaryCodec<T> get(Class<?> owner, String fieldName) {
		return (BinaryCodec<T>) BY_FIELD.get(new FieldKey(owner, fieldName));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static BinaryCodec<Object> get(Field field) {
		return (BinaryCodec<Object>) BY_FIELD.get(
				new FieldKey(field.getDeclaringClass(), field.getName()));
	}

	public static void clear() {
		BY_FIELD.clear();
		BinaryAutoCodec.clearCache();
	}
}