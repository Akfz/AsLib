package v.akfz.aslib.util.af.codec;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.BinaryException;
import v.akfz.aslib.util.af.annotation.AfExclude;
import v.akfz.aslib.util.af.annotation.AfInclude;
import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.registry.BinaryRegistry;
import v.akfz.aslib.util.af.registry.FieldCodecRegistry;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection-based {@link BinaryCodec}. Serializes declared fields by position,
 * recursively, driven by field types.
 * <p>
 * Field selection:
 * <ul>
 *   <li>skips {@code static}, {@code transient}, {@code final}, {@code synthetic}</li>
 *   <li>no AF annotations anywhere in the class → legacy mode: every remaining
 *       field is serialized</li>
 *   <li>any AF annotation present ({@link AfInclude} or {@link AfExclude}) →
 *       explicit mode: only {@code @AfInclude} fields are serialized; everything
 *       else, annotated or not, is skipped</li>
 *   <li>{@code @AfExclude} wins over {@code @AfInclude} on the same field</li>
 * </ul>
 * <p>
 * In explicit mode {@code @AfExclude} is documentary: its only job is to make
 * "I know about this field and skip it" visible in the source. It does not
 * provide blacklist semantics — for that, use {@code transient} and keep the
 * class annotation-free.
 * <p>
 * Per-field overrides registered via {@link FieldCodecRegistry} take precedence
 * over the built-in type dispatch. This is how you serialize {@code final} fields
 * or types with compact custom encodings.
 * <p>
 * Object (non-primitive) fields get a 1-byte null prefix. Containers use
 * element types from the generic signature. Nested objects use their registered
 * codec, or fall back to auto-codec.
 * <p>
 * No class name is written for nested objects — the declared field type is
 * authoritative. If you need polymorphism (declared {@code Shape}, runtime
 * {@code Circle}), write a custom codec that switches on the runtime type.
 * <p>
 * <b>Warning:</b> reordering fields, renaming types, or changing generic
 * signatures breaks read compatibility for existing files. This is not a
 * versioned format.
 */
public class BinaryAutoCodec<T> implements BinaryCodec<T> {

	private static final Map<Class<?>, BinaryAutoCodec<?>> CACHE = new ConcurrentHashMap<>();

	private final Class<T> type;
	private final List<FieldEntry> fields;

	private record FieldEntry(Field field, @Nullable BinaryCodec<?> customCodec) {}

	public BinaryAutoCodec(Class<T> type) {
		this.type = Objects.requireNonNull(type, "type");
		this.fields = collectFields(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> BinaryAutoCodec<T> get(Class<T> cls) {
		return (BinaryAutoCodec<T>) CACHE.computeIfAbsent(cls, BinaryAutoCodec::new);
	}

	public static void clearCache() {
		CACHE.clear();
	}

	public List<Field> getFields() {
		List<Field> out = new ArrayList<>(fields.size());
		for (FieldEntry e : fields) out.add(e.field);
		return Collections.unmodifiableList(out);
	}

	private static List<FieldEntry> collectFields(Class<?> type) {
		List<Field> all = new ArrayList<>();
		Class<?> current = type;

		while (current != null && current != Object.class) {
			for (Field f : current.getDeclaredFields()) {
				int mods = f.getModifiers();
				if (Modifier.isStatic(mods)) continue;
				if (Modifier.isTransient(mods)) continue;
				if (Modifier.isFinal(mods)) continue;
				if (f.isSynthetic()) continue;
				f.setAccessible(true);
				all.add(f);
			}
			current = current.getSuperclass();
		}

		boolean anyAnnotation = all.stream().anyMatch(f ->
				f.isAnnotationPresent(AfInclude.class)
						|| f.isAnnotationPresent(AfExclude.class));

		if (anyAnnotation) {
			all.removeIf(f ->
					!f.isAnnotationPresent(AfInclude.class)
							|| f.isAnnotationPresent(AfExclude.class));
		}

		List<FieldEntry> result = new ArrayList<>(all.size());
		for (Field f : all) {
			result.add(new FieldEntry(f, FieldCodecRegistry.get(f)));
		}
		return result;
	}

	@Override
	public void write(BinaryWriter w, T value) throws IOException {
		if (value == null) throw new BinaryException("BinaryAutoCodec.write(null) for " + type.getName());

		for (FieldEntry entry : fields) {
			try {
				Object v = entry.field.get(value);
				if (entry.customCodec != null) {
					writeWithCodec(w, entry.customCodec, v);
				} else {
					writeValue(w, v, entry.field.getGenericType());
				}
			} catch (IllegalAccessException e) {
				throw new BinaryException("Failed to read field " + entry.field.getName(), e);
			}
		}
	}

	@Override
	public T read(BinaryReader r) throws IOException {
		T instance = createInstance(type);
		for (FieldEntry entry : fields) {
			Object v;
			if (entry.customCodec != null) {
				v = entry.customCodec.read(r);
			} else {
				v = readValue(r, entry.field.getGenericType());
			}
			try {
				entry.field.set(instance, v);
			} catch (IllegalAccessException e) {
				throw new BinaryException("Failed to set field " + entry.field.getName(), e);
			}
		}
		return instance;
	}

	private T createInstance(Class<T> cls) {
		try {
			Constructor<T> ctor = cls.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (NoSuchMethodException e) {
			throw new BinaryException("Class " + cls.getName() +
					" has no no-arg constructor; BinaryAutoCodec requires one");
		} catch (ReflectiveOperationException e) {
			throw new BinaryException("Failed to instantiate " + cls.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void writeWithCodec(BinaryWriter w, BinaryCodec<?> codec, @Nullable Object value)
			throws IOException {
		((BinaryCodec<Object>) codec).write(w, value);
	}

	@SuppressWarnings("unchecked")
	private static void writeValue(BinaryWriter w, @Nullable Object v, Type type) throws IOException {
		Class<?> cls = rawClass(type);

		boolean nullable = !cls.isPrimitive();
		if (nullable) {
			if (v == null) { w.writeBoolean(false); return; }
			w.writeBoolean(true);
		}

		if (cls == boolean.class || cls == Boolean.class)  { w.writeBoolean((Boolean) v); return; }
		if (cls == byte.class    || cls == Byte.class)     { w.writeByte((Byte) v); return; }
		if (cls == short.class   || cls == Short.class)    { w.writeVarInt((Short) v); return; }
		if (cls == int.class     || cls == Integer.class)  { w.writeInt((Integer) v); return; }
		if (cls == long.class    || cls == Long.class)     { w.writeLong((Long) v); return; }
		if (cls == float.class   || cls == Float.class)    { w.writeFloat((Float) v); return; }
		if (cls == double.class  || cls == Double.class)   { w.writeDouble((Double) v); return; }
		if (cls == char.class    || cls == Character.class){ w.writeVarInt((Character) v); return; }
		if (cls == String.class)                           { w.writeString((String) v); return; }
		if (cls == UUID.class)                             { w.writeUUID((UUID) v); return; }

		if (cls == byte[].class) {
			byte[] arr = (byte[]) v;
			w.writeVarInt(arr.length);
			w.writeRaw(arr);
			return;
		}

		if (cls.isEnum()) {
			w.writeVarInt(((Enum<?>) v).ordinal());
			return;
		}

		if (cls.isArray()) {
			Class<?> comp = cls.getComponentType();
			int len = Array.getLength(v);
			w.writeVarInt(len);
			for (int i = 0; i < len; i++) {
				writeValue(w, Array.get(v, i), comp);
			}
			return;
		}

		if (Collection.class.isAssignableFrom(cls)) {
			Collection<?> col = (Collection<?>) v;
			w.writeVarInt(col.size());
			Type elemType = typeArg(type, 0, Object.class);
			for (Object o : col) {
				writeValue(w, o, elemType);
			}
			return;
		}

		if (Map.class.isAssignableFrom(cls)) {
			Map<?, ?> map = (Map<?, ?>) v;
			w.writeVarInt(map.size());
			Type keyType = typeArg(type, 0, Object.class);
			Type valType = typeArg(type, 1, Object.class);
			for (Map.Entry<?, ?> e : map.entrySet()) {
				writeValue(w, e.getKey(), keyType);
				writeValue(w, e.getValue(), valType);
			}
			return;
		}

		if (!cls.isAssignableFrom(v.getClass())) {
			throw new BinaryException("Polymorphism not supported: declared " +
					cls.getName() + ", runtime " + v.getClass().getName() +
					". Use writeObject/readObject or a custom codec.");
		}

		BinaryCodec<Object> codec = BinaryRegistry.codecFor((Class<Object>) cls);
		codec.write(w, v);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static Object readValue(BinaryReader r, Type type) throws IOException {
		Class<?> cls = rawClass(type);

		boolean nullable = !cls.isPrimitive();
		if (nullable && !r.readBoolean()) return null;

		if (cls == boolean.class || cls == Boolean.class)   return r.readBoolean();
		if (cls == byte.class    || cls == Byte.class)      return (byte) r.readByte();
		if (cls == short.class   || cls == Short.class)     return (short) r.readVarInt();
		if (cls == int.class     || cls == Integer.class)   return r.readInt();
		if (cls == long.class    || cls == Long.class)      return r.readLong();
		if (cls == float.class   || cls == Float.class)     return r.readFloat();
		if (cls == double.class  || cls == Double.class)    return r.readDouble();
		if (cls == char.class    || cls == Character.class) return (char) r.readVarInt();
		if (cls == String.class)                            return r.readString();
		if (cls == UUID.class)                              return r.readUUID();

		if (cls == byte[].class) {
			int len = r.readVarInt();
			byte[] arr = new byte[len];
			r.readFully(arr);
			return arr;
		}

		if (cls.isEnum()) {
			int idx = r.readVarInt();
			Object[] constants = cls.getEnumConstants();
			if (idx < 0 || idx >= constants.length) {
				throw new BinaryException("Bad enum ordinal " + idx + " for " + cls.getName());
			}
			return constants[idx];
		}

		if (cls.isArray()) {
			Class<?> comp = cls.getComponentType();
			int len = r.readVarInt();
			Object arr = Array.newInstance(comp, len);
			for (int i = 0; i < len; i++) {
				Array.set(arr, i, readValue(r, comp));
			}
			return arr;
		}

		if (Collection.class.isAssignableFrom(cls)) {
			int size = r.readVarInt();
			Type elemType = typeArg(type, 0, Object.class);
			Collection<Object> col = instantiateCollection(cls, size);
			for (int i = 0; i < size; i++) {
				col.add(readValue(r, elemType));
			}
			return col;
		}

		if (Map.class.isAssignableFrom(cls)) {
			int size = r.readVarInt();
			Type keyType = typeArg(type, 0, Object.class);
			Type valType = typeArg(type, 1, Object.class);
			Map<Object, Object> map = instantiateMap(cls, size);
			for (int i = 0; i < size; i++) {
				Object k = readValue(r, keyType);
				Object v = readValue(r, valType);
				map.put(k, v);
			}
			return map;
		}

		BinaryCodec<Object> codec = BinaryRegistry.codecFor((Class<Object>) cls);
		return codec.read(r);
	}

	private static Class<?> rawClass(Type type) {
		if (type instanceof Class<?> c) return c;
		if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
		if (type instanceof GenericArrayType gat) {
			Class<?> comp = rawClass(gat.getGenericComponentType());
			return Array.newInstance(comp, 0).getClass();
		}
		throw new BinaryException("Unsupported Type: " + type);
	}

	private static Type typeArg(Type type, int index, Type fallback) {
		if (type instanceof ParameterizedType pt) {
			Type[] args = pt.getActualTypeArguments();
			if (index < args.length) return args[index];
		}
		return fallback;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> instantiateCollection(Class<?> cls, int hint) {
		if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
			if (Set.class.isAssignableFrom(cls)) return new HashSet<>(Math.max(8, hint));
			return new ArrayList<>(Math.max(8, hint));
		}
		try {
			return (Collection<Object>) cls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new BinaryException("Cannot instantiate collection " + cls.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Object, Object> instantiateMap(Class<?> cls, int hint) {
		if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
			if (SortedMap.class.isAssignableFrom(cls)) return new TreeMap<>();
			return new LinkedHashMap<>(Math.max(8, hint));
		}
		try {
			return (Map<Object, Object>) cls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new BinaryException("Cannot instantiate map " + cls.getName(), e);
		}
	}
}