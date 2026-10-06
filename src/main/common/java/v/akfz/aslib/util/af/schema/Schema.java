package v.akfz.aslib.util.af.schema;

import v.akfz.aslib.util.af.BinaryException;
import v.akfz.aslib.util.af.codec.BinaryCodec;
import v.akfz.aslib.util.af.codec.StructuredBinaryCodec;
import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.registry.BinaryRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File schema: string pool + type table + root index. Also implements the
 * schema-driven payload codec (read/write of actual values).
 * <p>
 * Schema is always read first, top-to-bottom, before any value. Because field
 * names live in the schema (not in the payload), migration is a single pass:
 * read value by old schema, set it into the current class by name, drop the
 * rest.
 */
public final class Schema {

	public final String[] pool;
	public final TypeDef[] types;
	public final int rootIdx;

	public Schema(String[] pool, TypeDef[] types, int rootIdx) {
		this.pool = pool;
		this.types = types;
		this.rootIdx = rootIdx;
	}

	public TypeDef root() { return types[rootIdx]; }

	public Class<?> resolveClass(int idx) {
		TypeDef def = types[idx];
		try {
			return switch (def.kind) {
				case TypeDef.K_BOOLEAN -> boolean.class;
				case TypeDef.K_BYTE -> byte.class;
				case TypeDef.K_SHORT -> short.class;
				case TypeDef.K_INT -> int.class;
				case TypeDef.K_LONG -> long.class;
				case TypeDef.K_FLOAT -> float.class;
				case TypeDef.K_DOUBLE -> double.class;
				case TypeDef.K_CHAR -> char.class;
				case TypeDef.K_BOOLEAN_BOX -> Boolean.class;
				case TypeDef.K_BYTE_BOX -> Byte.class;
				case TypeDef.K_SHORT_BOX -> Short.class;
				case TypeDef.K_INT_BOX -> Integer.class;
				case TypeDef.K_LONG_BOX -> Long.class;
				case TypeDef.K_FLOAT_BOX -> Float.class;
				case TypeDef.K_DOUBLE_BOX -> Double.class;
				case TypeDef.K_CHAR_BOX -> Character.class;
				case TypeDef.K_STRING -> String.class;
				case TypeDef.K_UUID -> UUID.class;
				case TypeDef.K_BYTES -> byte[].class;
				case TypeDef.K_ENUM, TypeDef.K_OBJECT, TypeDef.K_COLLECTION,
				     TypeDef.K_MAP, TypeDef.K_CUSTOM -> Class.forName(def.className);
				case TypeDef.K_ARRAY -> Array.newInstance(resolveClass(def.elemIdx), 0).getClass();
				default -> Object.class;
			};
		} catch (ClassNotFoundException e) {
			throw new BinaryException("Class not found: " + def.className, e);
		}
	}

	public void write(BinaryWriter w) throws IOException {
		Map<String, Integer> idx = new HashMap<>();
		for (int i = 0; i < pool.length; i++) idx.put(pool[i], i);

		w.writeVarInt(pool.length);
		for (String s : pool) w.writeString(s);
		w.writeVarInt(types.length);
		for (TypeDef t : types) writeType(w, t, idx);
		w.writeVarInt(rootIdx);
	}

	private static void writeType(BinaryWriter w, TypeDef t, Map<String, Integer> idx) throws IOException {
		w.writeByte(t.kind);
		switch (t.kind) {
			case TypeDef.K_ENUM:
				w.writeVarInt(idx.get(t.className));
				w.writeVarInt(t.enumConstants.length);
				for (String c : t.enumConstants) w.writeVarInt(idx.get(c));
				break;
			case TypeDef.K_ARRAY:
				w.writeVarInt(t.elemIdx);
				break;
			case TypeDef.K_COLLECTION:
				w.writeVarInt(idx.get(t.className));
				w.writeVarInt(t.elemIdx);
				break;
			case TypeDef.K_MAP:
				w.writeVarInt(idx.get(t.className));
				w.writeVarInt(t.keyIdx);
				w.writeVarInt(t.valIdx);
				break;
			case TypeDef.K_OBJECT:
				w.writeVarInt(idx.get(t.className));
				w.writeVarInt(t.fieldNames.length);
				for (int i = 0; i < t.fieldNames.length; i++) {
					w.writeVarInt(idx.get(t.fieldNames[i]));
					w.writeVarInt(t.fieldTypeIdx[i]);
				}
				break;
			case TypeDef.K_CUSTOM:
				w.writeVarInt(idx.get(t.codecClass));
				w.writeVarInt(idx.get(t.className));
				w.writeVarInt(t.codecVersion);
				break;
			default:
		}
	}

	public static Schema read(BinaryReader r) throws IOException {
		int poolCount = r.readVarInt();
		String[] pool = new String[poolCount];
		for (int i = 0; i < poolCount; i++) pool[i] = r.readString();

		int typeCount = r.readVarInt();
		TypeDef[] types = new TypeDef[typeCount];
		for (int i = 0; i < typeCount; i++) types[i] = readType(r, pool);

		int rootIdx = r.readVarInt();
		return new Schema(pool, types, rootIdx);
	}

	private static TypeDef readType(BinaryReader r, String[] pool) throws IOException {
		byte kind = (byte) r.readByte();
		switch (kind) {
			case TypeDef.K_ENUM: {
				String cls = pool[r.readVarInt()];
				int n = r.readVarInt();
				String[] consts = new String[n];
				for (int i = 0; i < n; i++) consts[i] = pool[r.readVarInt()];
				return TypeDef.enumType(cls, consts);
			}
			case TypeDef.K_ARRAY:
				return TypeDef.array(r.readVarInt());
			case TypeDef.K_COLLECTION: {
				String cls = pool[r.readVarInt()];
				return TypeDef.collection(cls, r.readVarInt());
			}
			case TypeDef.K_MAP: {
				String cls = pool[r.readVarInt()];
				int k = r.readVarInt(), v = r.readVarInt();
				return TypeDef.map(cls, k, v);
			}
			case TypeDef.K_OBJECT: {
				String cls = pool[r.readVarInt()];
				int n = r.readVarInt();
				String[] names = new String[n];
				int[] idxs = new int[n];
				for (int i = 0; i < n; i++) {
					names[i] = pool[r.readVarInt()];
					idxs[i] = r.readVarInt();
				}
				return TypeDef.object(cls, names, idxs);
			}
			case TypeDef.K_CUSTOM: {
				String codec = pool[r.readVarInt()];
				String value = pool[r.readVarInt()];
				return TypeDef.custom(codec, value, r.readVarInt());
			}
			default:
				return TypeDef.primitive(kind);
		}
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public void writeValue(BinaryWriter w, Object value, int typeIdx, boolean topLevel) throws IOException {
		TypeDef def = types[typeIdx];

		switch (def.kind) {
			case TypeDef.K_BOOLEAN: w.writeBoolean((Boolean) value); return;
			case TypeDef.K_BYTE:    w.writeByte((Byte) value); return;
			case TypeDef.K_SHORT:   w.writeVarInt((Short) value); return;
			case TypeDef.K_INT:     w.writeInt((Integer) value); return;
			case TypeDef.K_LONG:    w.writeLong((Long) value); return;
			case TypeDef.K_FLOAT:   w.writeFloat((Float) value); return;
			case TypeDef.K_DOUBLE:  w.writeDouble((Double) value); return;
			case TypeDef.K_CHAR:    w.writeVarInt((Character) value); return;

			case TypeDef.K_BOOLEAN_BOX:
			case TypeDef.K_BYTE_BOX:
			case TypeDef.K_SHORT_BOX:
			case TypeDef.K_INT_BOX:
			case TypeDef.K_LONG_BOX:
			case TypeDef.K_FLOAT_BOX:
			case TypeDef.K_DOUBLE_BOX:
			case TypeDef.K_CHAR_BOX:
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				writeBoxed(w, value, def.kind);
				return;

			case TypeDef.K_STRING:
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				w.writeString((String) value);
				return;

			case TypeDef.K_UUID:
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				w.writeUUID((UUID) value);
				return;

			case TypeDef.K_BYTES: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				byte[] b = (byte[]) value;
				w.writeVarInt(b.length);
				w.writeRaw(b);
				return;
			}

			case TypeDef.K_ENUM: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				w.writeVarInt(((Enum<?>) value).ordinal());
				return;
			}

			case TypeDef.K_ARRAY: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				int len = Array.getLength(value);
				w.writeVarInt(len);
				for (int i = 0; i < len; i++) {
					writeValue(w, Array.get(value, i), def.elemIdx, false);
				}
				return;
			}

			case TypeDef.K_COLLECTION: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				Collection<?> c = (Collection<?>) value;
				w.writeVarInt(c.size());
				for (Object o : c) writeValue(w, o, def.elemIdx, false);
				return;
			}

			case TypeDef.K_MAP: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				Map<?, ?> m = (Map<?, ?>) value;
				w.writeVarInt(m.size());
				for (Map.Entry<?, ?> e : m.entrySet()) {
					writeValue(w, e.getKey(), def.keyIdx, false);
					writeValue(w, e.getValue(), def.valIdx, false);
				}
				return;
			}

			case TypeDef.K_OBJECT: {
				if (value == null) { w.writeBoolean(false); return; }
				w.writeBoolean(true);
				Class<?> schemaCls = classForName(def.className);
				BinaryCodec<?> registered = BinaryRegistry.get(schemaCls);
				if (registered instanceof StructuredBinaryCodec<?> sbc) {
					((StructuredBinaryCodec) sbc).writeFields(w, value, this, typeIdx);
				} else {
					boolean rec = schemaCls.isRecord();
					for (int i = 0; i < def.fieldNames.length; i++) {
						Object v = readFieldValue(value, schemaCls, def.fieldNames[i], rec);
						writeValue(w, v, def.fieldTypeIdx[i], false);
					}
				}
				return;
			}

			case TypeDef.K_CUSTOM: {
				BinaryCodec<Object> codec = codecByName(def.codecClass);
				if (topLevel) {
					codec.write(w, value);
				} else {
					if (value == null) { w.writeBoolean(false); return; }
					w.writeBoolean(true);
					java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
					BinaryWriter bw = new BinaryWriter(buf);
					codec.write(bw, value);
					bw.flush();
					byte[] bytes = buf.toByteArray();
					w.writeVarInt(bytes.length);
					w.writeRaw(bytes);
				}
				return;
			}
		}
		throw new BinaryException("Unknown type kind: " + def.kind);
	}

	private static void writeBoxed(BinaryWriter w, Object v, byte kind) throws IOException {
		switch (kind) {
			case TypeDef.K_BOOLEAN_BOX: w.writeBoolean((Boolean) v); break;
			case TypeDef.K_BYTE_BOX:    w.writeByte((Byte) v); break;
			case TypeDef.K_SHORT_BOX:   w.writeVarInt((Short) v); break;
			case TypeDef.K_INT_BOX:     w.writeInt((Integer) v); break;
			case TypeDef.K_LONG_BOX:    w.writeLong((Long) v); break;
			case TypeDef.K_FLOAT_BOX:   w.writeFloat((Float) v); break;
			case TypeDef.K_DOUBLE_BOX:  w.writeDouble((Double) v); break;
			case TypeDef.K_CHAR_BOX:    w.writeVarInt((Character) v); break;
		}
	}

	public Object readValue(BinaryReader r, int typeIdx, Type target, boolean topLevel)
			throws IOException {
		TypeDef def = types[typeIdx];
		Class<?> targetClass = target != null ? rawClass(target) : null;

		switch (def.kind) {
			case TypeDef.K_BOOLEAN: return r.readBoolean();
			case TypeDef.K_BYTE:    return (byte) r.readByte();
			case TypeDef.K_SHORT:   return (short) r.readVarInt();
			case TypeDef.K_INT:     return r.readInt();
			case TypeDef.K_LONG:    return r.readLong();
			case TypeDef.K_FLOAT:   return r.readFloat();
			case TypeDef.K_DOUBLE:  return r.readDouble();
			case TypeDef.K_CHAR:    return (char) r.readVarInt();

			case TypeDef.K_BOOLEAN_BOX:
			case TypeDef.K_BYTE_BOX:
			case TypeDef.K_SHORT_BOX:
			case TypeDef.K_INT_BOX:
			case TypeDef.K_LONG_BOX:
			case TypeDef.K_FLOAT_BOX:
			case TypeDef.K_DOUBLE_BOX:
			case TypeDef.K_CHAR_BOX:
				if (!r.readBoolean()) return null;
				return readBoxed(r, def.kind);

			case TypeDef.K_STRING:
				if (!r.readBoolean()) return null;
				return r.readString();

			case TypeDef.K_UUID:
				if (!r.readBoolean()) return null;
				return r.readUUID();

			case TypeDef.K_BYTES: {
				if (!r.readBoolean()) return null;
				int len = r.readVarInt();
				byte[] b = new byte[len];
				r.readFully(b);
				return b;
			}

			case TypeDef.K_ENUM: {
				if (!r.readBoolean()) return null;
				int ord = r.readVarInt();
				if (ord < 0 || ord >= def.enumConstants.length)
					throw new BinaryException("Bad enum ordinal " + ord
							+ " for " + def.className);
				String name = def.enumConstants[ord];
				Class<?> enumCls = targetClass != null ? targetClass : classForName(def.className);
				try {
					@SuppressWarnings({"unchecked", "rawtypes"})
					Object v = Enum.valueOf((Class<? extends Enum>) enumCls, name);
					return v;
				} catch (IllegalArgumentException e) {
					throw new BinaryException("Enum " + enumCls.getName()
							+ " has no constant named " + name, e);
				}
			}

			case TypeDef.K_ARRAY: {
				if (!r.readBoolean()) return null;
				int len = r.readVarInt();
				Class<?> comp = targetClass != null && targetClass.isArray()
						? targetClass.getComponentType()
						: resolveClass(def.elemIdx);
				Object arr = Array.newInstance(comp, len);
				for (int i = 0; i < len; i++) {
					Array.set(arr, i, readValue(r, def.elemIdx, comp, false));
				}
				return arr;
			}

			case TypeDef.K_COLLECTION: {
				if (!r.readBoolean()) return null;
				int size = r.readVarInt();
				Class<?> impl = pickImpl(targetClass, def.className, false);
				Collection<Object> col = instantiateCollection(impl, size);
				Type elem = target != null ? typeArg(target, 0) : null;
				for (int i = 0; i < size; i++) {
					col.add(readValue(r, def.elemIdx, elem, false));
				}
				return col;
			}

			case TypeDef.K_MAP: {
				if (!r.readBoolean()) return null;
				int size = r.readVarInt();
				Class<?> impl = pickImpl(targetClass, def.className, true);
				Map<Object, Object> map = instantiateMap(impl, size);
				Type kt = target != null ? typeArg(target, 0) : null;
				Type vt = target != null ? typeArg(target, 1) : null;
				for (int i = 0; i < size; i++) {
					Object k = readValue(r, def.keyIdx, kt, false);
					Object v = readValue(r, def.valIdx, vt, false);
					map.put(k, v);
				}
				return map;
			}

			case TypeDef.K_OBJECT: {
				if (!r.readBoolean()) return null;
				Class<?> schemaCls = classForName(def.className);
				BinaryCodec<?> registered = BinaryRegistry.get(schemaCls);
				if (registered instanceof StructuredBinaryCodec<?> sbc) {
					return sbc.readFields(r, this, typeIdx, schemaCls);
				}
				Class<?> instanceCls = targetClass != null ? targetClass : schemaCls;
				return readObject(r, def, instanceCls);
			}

			case TypeDef.K_CUSTOM: {
				BinaryCodec<Object> codec = codecByName(def.codecClass);
				if (topLevel) {
					return codec.read(r);
				}
				if (!r.readBoolean()) return null;
				int len = r.readVarInt();
				byte[] buf = new byte[len];
				r.readFully(buf);
				return codec.read(new BinaryReader(new ByteArrayInputStream(buf)));
			}
		}
		throw new BinaryException("Unknown type kind: " + def.kind);
	}

	private static Object readBoxed(BinaryReader r, byte kind) throws IOException {
		return switch (kind) {
			case TypeDef.K_BOOLEAN_BOX -> r.readBoolean();
			case TypeDef.K_BYTE_BOX    -> (byte) r.readByte();
			case TypeDef.K_SHORT_BOX   -> (short) r.readVarInt();
			case TypeDef.K_INT_BOX     -> r.readInt();
			case TypeDef.K_LONG_BOX    -> r.readLong();
			case TypeDef.K_FLOAT_BOX   -> r.readFloat();
			case TypeDef.K_DOUBLE_BOX  -> r.readDouble();
			case TypeDef.K_CHAR_BOX    -> (char) r.readVarInt();
			default -> throw new BinaryException("Not boxed: " + kind);
		};
	}

	private Object readObject(BinaryReader r, TypeDef def, Class<?> cls) throws IOException {
		if (cls.isRecord()) {
			RecordComponent[] comps = cls.getRecordComponents();
			Class<?>[] paramTypes = new Class<?>[comps.length];
			for (int i = 0; i < comps.length; i++) paramTypes[i] = comps[i].getType();

			Map<String, Object> byName = new HashMap<>();
			for (int i = 0; i < def.fieldNames.length; i++) {
				Type ft = findFieldType(cls, def.fieldNames[i]);
				byName.put(def.fieldNames[i],
						readValue(r, def.fieldTypeIdx[i], ft, false));
			}

			Object[] args = new Object[comps.length];
			for (int i = 0; i < comps.length; i++) {
				RecordComponent rc = comps[i];
				Class<?> pt = paramTypes[i];
				Object v = byName.get(rc.getName());
				args[i] = (v != null && isAssignableBoxed(pt, v.getClass()))
						? v
						: defaultValue(pt);
			}
			try {
				Constructor<?> ctor = cls.getDeclaredConstructor(paramTypes);
				ctor.setAccessible(true);
				return ctor.newInstance(args);
			} catch (ReflectiveOperationException | IllegalArgumentException e) {
				throw new BinaryException("Cannot construct record " + cls.getName(), e);
			}
		}

		Object instance;
		try {
			Constructor<?> ctor = cls.getDeclaredConstructor();
			ctor.setAccessible(true);
			instance = ctor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new BinaryException("Class " + cls.getName()
					+ " has no no-arg constructor; cannot instantiate", e);
		}

		for (int i = 0; i < def.fieldNames.length; i++) {
			Type ft = findFieldType(cls, def.fieldNames[i]);
			Object v = readValue(r, def.fieldTypeIdx[i], ft, false);
			Field f = findField(cls, def.fieldNames[i]);
			if (f == null) continue;
			try {
				f.setAccessible(true);
				f.set(instance, v);
			} catch (IllegalAccessException | IllegalArgumentException ignored) {
			}
		}
		return instance;
	}

	private static Object readFieldValue(Object instance, Class<?> cls, String name, boolean isRecord) {
		if (isRecord) {
			for (RecordComponent rc : cls.getRecordComponents()) {
				if (rc.getName().equals(name)) {
					try { return rc.getAccessor().invoke(instance); }
					catch (ReflectiveOperationException e) {
						throw new BinaryException("Cannot read record field " + name, e);
					}
				}
			}
		}
		Field f = findField(cls, name);
		if (f == null) throw new BinaryException("Field not found: " + cls.getName() + "." + name);
		try {
			f.setAccessible(true);
			return f.get(instance);
		} catch (ReflectiveOperationException e) {
			throw new BinaryException("Cannot read field " + name, e);
		}
	}

	private static Field findField(Class<?> cls, String name) {
		Class<?> c = cls;
		while (c != null && c != Object.class) {
			try { return c.getDeclaredField(name); }
			catch (NoSuchFieldException ignored) {}
			c = c.getSuperclass();
		}
		return null;
	}

	private static Type findFieldType(Class<?> cls, String name) {
		if (cls.isRecord()) {
			for (RecordComponent rc : cls.getRecordComponents()) {
				if (rc.getName().equals(name)) return rc.getGenericType();
			}
		}
		Field f = findField(cls, name);
		return f != null ? f.getGenericType() : null;
	}

	private static Class<?> rawClass(Type t) {
		if (t instanceof Class<?> c) return c;
		if (t instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
		if (t instanceof GenericArrayType gat)
			return Array.newInstance(rawClass(gat.getGenericComponentType()), 0).getClass();
		return Object.class;
	}

	private static Type typeArg(Type t, int i) {
		if (t instanceof ParameterizedType pt) {
			Type[] args = pt.getActualTypeArguments();
			if (i < args.length) return args[i];
		}
		return null;
	}

	private static Class<?> pickImpl(Class<?> target, String fromSchema, boolean isMap) {
		if (target != null && !target.isInterface() && !Modifier.isAbstract(target.getModifiers()))
			return target;
		Class<?> impl = fromSchema != null ? classForName(fromSchema) : null;
		if (impl != null && !impl.isInterface() && !Modifier.isAbstract(impl.getModifiers()))
			return impl;
		if (isMap) return LinkedHashMap.class;
		return ArrayList.class;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> instantiateCollection(Class<?> impl, int size) {
		try { return (Collection<Object>) impl.getDeclaredConstructor().newInstance(); }
		catch (ReflectiveOperationException e) {
			throw new BinaryException("Cannot instantiate collection " + impl.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Object, Object> instantiateMap(Class<?> impl, int size) {
		try { return (Map<Object, Object>) impl.getDeclaredConstructor().newInstance(); }
		catch (ReflectiveOperationException e) {
			throw new BinaryException("Cannot instantiate map " + impl.getName(), e);
		}
	}

	private static Class<?> classForName(String n) {
		try { return Class.forName(n); }
		catch (ClassNotFoundException e) { throw new BinaryException("Class not found: " + n, e); }
	}

	private static Object defaultValue(Class<?> cls) {
		if (cls == boolean.class) return false;
		if (cls == byte.class) return (byte) 0;
		if (cls == short.class) return (short) 0;
		if (cls == int.class) return 0;
		if (cls == long.class) return 0L;
		if (cls == float.class) return 0f;
		if (cls == double.class) return 0d;
		if (cls == char.class) return '\0';
		return null;
	}

	private static boolean isAssignableBoxed(Class<?> target, Class<?> actual) {
		if (target.isPrimitive()) return wrap(target).isAssignableFrom(actual);
		return target.isAssignableFrom(actual);
	}

	private static Class<?> wrap(Class<?> p) {
		if (p == boolean.class) return Boolean.class;
		if (p == byte.class)    return Byte.class;
		if (p == short.class)   return Short.class;
		if (p == int.class)     return Integer.class;
		if (p == long.class)    return Long.class;
		if (p == float.class)   return Float.class;
		if (p == double.class)  return Double.class;
		if (p == char.class)    return Character.class;
		return p;
	}

	private static final Map<String, BinaryCodec<?>> CODEC_CACHE = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	private static BinaryCodec<Object> codecByName(String name) {
		return (BinaryCodec<Object>) CODEC_CACHE.computeIfAbsent(name, n -> {
			try {
				Class<?> cls = Class.forName(n);
				Constructor<?> ctor = cls.getDeclaredConstructor();
				ctor.setAccessible(true);
				return (BinaryCodec<?>) ctor.newInstance();
			} catch (ReflectiveOperationException e) {
				throw new BinaryException("Cannot instantiate codec: " + n, e);
			}
		});
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Schema s)) return false;
		return rootIdx == s.rootIdx
				&& Arrays.equals(pool, s.pool)
				&& Arrays.equals(types, s.types);
	}

	@Override
	public int hashCode() {
		int h = Arrays.hashCode(pool);
		h = 31 * h + Arrays.hashCode(types);
		h = 31 * h + rootIdx;
		return h;
	}
}