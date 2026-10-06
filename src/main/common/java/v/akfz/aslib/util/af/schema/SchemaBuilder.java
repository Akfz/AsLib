package v.akfz.aslib.util.af.schema;

import v.akfz.aslib.util.af.codec.BinaryAutoCodec;
import v.akfz.aslib.util.af.codec.BinaryCodec;
import v.akfz.aslib.util.af.registry.BinaryRegistry;
import v.akfz.aslib.util.af.registry.FieldCodecRegistry;

import java.lang.reflect.*;
import java.util.*;

/**
 * Builds a {@link Schema} for a given type by reflection, with the same field
 * selection rules the runtime uses (see {@link BinaryAutoCodec}).
 * <p>
 * Types are deduplicated by structural key. Recursive types are supported by
 * reserving a slot before recursing into fields.
 * <p>
 * Registered custom codecs ({@link BinaryRegistry}) become {@code CUSTOM} nodes;
 * per-field codecs ({@link FieldCodecRegistry}) do too. Everything else is
 * broken down into primitives, enums, arrays, collections, maps, and objects.
 */
public final class SchemaBuilder {

	private final Map<String, Integer> poolIdx = new HashMap<>();
	private final List<String> pool = new ArrayList<>();
	private final Map<Object, Integer> typeIdx = new HashMap<>();
	private final List<TypeDef> types = new ArrayList<>();

	private SchemaBuilder() {}

	public static Schema build(Class<?> rootType) {
		SchemaBuilder b = new SchemaBuilder();
		int rootIdx = b.resolve(rootType);
		return new Schema(b.pool.toArray(new String[0]), b.types.toArray(new TypeDef[0]), rootIdx);
	}

	public static Schema customOnly(Class<?> codecClass, Class<?> valueClass, int version) {
		String c = codecClass.getName();
		String v = valueClass.getName();
		return new Schema(new String[]{c, v},
				new TypeDef[]{TypeDef.custom(c, v, version)}, 0);
	}

	private int intern(String s) {
		return poolIdx.computeIfAbsent(s, k -> { pool.add(k); return pool.size() - 1; });
	}

	private int resolve(Type t) {
		Object key = normalizeKey(t);
		Integer existing = typeIdx.get(key);
		if (existing != null) return existing;

		int idx = types.size();
		types.add(null);
		typeIdx.put(key, idx);
		types.set(idx, computeDef(t));
		return idx;
	}

	private Object normalizeKey(Type t) {
		if (t instanceof Class<?> c) return c;
		if (t instanceof ParameterizedType pt) {
			List<Object> args = new ArrayList<>();
			for (Type a : pt.getActualTypeArguments()) args.add(normalizeKey(a));
			return new ParamKey((Class<?>) pt.getRawType(), args);
		}
		if (t instanceof GenericArrayType gat) return new ArrayKey(normalizeKey(gat.getGenericComponentType()));
		if (t instanceof TypeVariable<?> || t instanceof WildcardType) return Object.class;
		return t;
	}

	private TypeDef computeDef(Type t) {
		Class<?> raw = rawClass(t);

		if (BinaryRegistry.isRegistered(raw)) {
			BinaryCodec<?> codec = BinaryRegistry.get(raw);
			return TypeDef.custom(codec.getClass().getName(), raw.getName(), 0);
		}

		if (raw == boolean.class)  return TypeDef.primitive(TypeDef.K_BOOLEAN);
		if (raw == byte.class)     return TypeDef.primitive(TypeDef.K_BYTE);
		if (raw == short.class)    return TypeDef.primitive(TypeDef.K_SHORT);
		if (raw == int.class)      return TypeDef.primitive(TypeDef.K_INT);
		if (raw == long.class)     return TypeDef.primitive(TypeDef.K_LONG);
		if (raw == float.class)    return TypeDef.primitive(TypeDef.K_FLOAT);
		if (raw == double.class)   return TypeDef.primitive(TypeDef.K_DOUBLE);
		if (raw == char.class)     return TypeDef.primitive(TypeDef.K_CHAR);

		if (raw == Boolean.class)  return TypeDef.primitive(TypeDef.K_BOOLEAN_BOX);
		if (raw == Byte.class)     return TypeDef.primitive(TypeDef.K_BYTE_BOX);
		if (raw == Short.class)    return TypeDef.primitive(TypeDef.K_SHORT_BOX);
		if (raw == Integer.class)  return TypeDef.primitive(TypeDef.K_INT_BOX);
		if (raw == Long.class)     return TypeDef.primitive(TypeDef.K_LONG_BOX);
		if (raw == Float.class)    return TypeDef.primitive(TypeDef.K_FLOAT_BOX);
		if (raw == Double.class)   return TypeDef.primitive(TypeDef.K_DOUBLE_BOX);
		if (raw == Character.class) return TypeDef.primitive(TypeDef.K_CHAR_BOX);

		if (raw == String.class)   return TypeDef.primitive(TypeDef.K_STRING);
		if (raw == UUID.class)     return TypeDef.primitive(TypeDef.K_UUID);
		if (raw == byte[].class)   return TypeDef.primitive(TypeDef.K_BYTES);

		if (raw.isEnum()) {
			Object[] cs = raw.getEnumConstants();
			String[] names = new String[cs.length];
			for (int i = 0; i < cs.length; i++) names[i] = ((Enum<?>) cs[i]).name();
			return TypeDef.enumType(raw.getName(), names);
		}

		if (raw.isArray()) {
			Type comp = raw.getComponentType();
			if (t instanceof GenericArrayType gat) comp = gat.getGenericComponentType();
			return TypeDef.array(resolve(comp));
		}

		if (Collection.class.isAssignableFrom(raw)) {
			Type elem = typeArg(t, 0);
			Class<?> impl = raw;
			if (impl.isInterface() || Modifier.isAbstract(impl.getModifiers()))
				impl = Set.class.isAssignableFrom(raw) ? HashSet.class : ArrayList.class;
			return TypeDef.collection(impl.getName(), resolve(elem));
		}

		if (Map.class.isAssignableFrom(raw)) {
			Type k = typeArg(t, 0), v = typeArg(t, 1);
			Class<?> impl = raw;
			if (impl.isInterface() || Modifier.isAbstract(impl.getModifiers()))
				impl = SortedMap.class.isAssignableFrom(raw) ? TreeMap.class : LinkedHashMap.class;
			return TypeDef.map(impl.getName(), resolve(k), resolve(v));
		}

		return buildObject(raw);
	}

	private TypeDef buildObject(Class<?> cls) {
		List<Field> fields = BinaryAutoCodec.collectSerializableFields(cls);
		String[] names = new String[fields.size()];
		int[] ids = new int[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			Field f = fields.get(i);
			names[i] = f.getName();
			BinaryCodec<?> fc = FieldCodecRegistry.get(f);
			if (fc != null) ids[i] = addCustom(fc.getClass(), f.getType());
			else            ids[i] = resolve(f.getGenericType());
		}
		return TypeDef.object(cls.getName(), names, ids);
	}

	private int addCustom(Class<?> codecCls, Class<?> valueCls) {
		Object key = new CustomKey(codecCls, valueCls);
		Integer existing = typeIdx.get(key);
		if (existing != null) return existing;
		int idx = types.size();
		types.add(TypeDef.custom(codecCls.getName(), valueCls.getName(), 0));
		typeIdx.put(key, idx);
		return idx;
	}

	static Class<?> rawClass(Type t) {
		if (t instanceof Class<?> c) return c;
		if (t instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
		if (t instanceof GenericArrayType gat)
			return Array.newInstance(rawClass(gat.getGenericComponentType()), 0).getClass();
		return Object.class;
	}

	static Type typeArg(Type t, int i) {
		if (t instanceof ParameterizedType pt) {
			Type[] args = pt.getActualTypeArguments();
			if (i < args.length) return args[i];
		}
		return Object.class;
	}

	private record ParamKey(Class<?> raw, List<Object> args) {}
	private record ArrayKey(Object comp) {}
	private record CustomKey(Class<?> codec, Class<?> value) {}
}