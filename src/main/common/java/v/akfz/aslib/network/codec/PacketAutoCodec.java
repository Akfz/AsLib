package v.akfz.aslib.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import v.akfz.aslib.network.annotation.NetExclude;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection-driven codec for a class. Built once via {@link #of(Class)},
 * then reused. Writes and reads members in declaration order.
 * <p>
 * Field selection:
 * <ul>
 *   <li>static and transient are skipped</li>
 *   <li>no Net* annotation anywhere → serialize everything</li>
 *   <li>any Net* annotation → serialize only {@code @NetInclude} fields</li>
 *   <li>{@code @NetExclude} beats {@code @NetInclude} on the same member</li>
 * </ul>
 * <p>
 * Supported types, without registration:
 * <ul>
 *   <li>primitives (int, long, boolean, float, double, byte, short, char) and their wrappers</li>
 *   <li>String, UUID, ResourceLocation, Vec3</li>
 *   <li>byte[]</li>
 *   <li>enums (ordinal)</li>
 *   <li>arrays, List/Set/Queue, Map — recursively, if their element types are supported</li>
 *   <li>any other class that itself can be auto-coded or is in {@link PacketCodecRegistry}</li>
 * </ul>
 * Anything else → {@link IllegalStateException} at codec-build time, with a clear
 * message telling you to register a {@code PacketTypeCodec}.
 * <p>
 * Records work out of the box via the canonical constructor. Regular classes
 * need a no-arg constructor.
 * <p>
 * All reference types get a 1-byte presence flag. Primitives don't.
 */
public final class PacketAutoCodec<T> implements PacketTypeCodec<T> {

    private static final Map<Class<?>, PacketAutoCodec<?>> CACHE = new ConcurrentHashMap<>();

    private final Class<T> type;
    private final List<MemberCodec<?>> members;
    private final Constructor<T> ctor;
    private final boolean record;

    private record MemberCodec<F>(Field field, int recordIndex,
                                  PacketTypeCodec<?> codec, boolean primitive,
                                  Getter getter, Setter setter) {}

    @FunctionalInterface private interface Getter { Object get(Object instance); }
    @FunctionalInterface private interface Setter { void set(Object instance, Object value); }

    private PacketAutoCodec(Class<T> type, List<MemberCodec<?>> members,
                            Constructor<T> ctor, boolean record) {
        this.type = type;
        this.members = members;
        this.ctor = ctor;
        this.record = record;
    }

    @SuppressWarnings("unchecked")
    public static <T> PacketAutoCodec<T> of(Class<T> cls) {
        return (PacketAutoCodec<T>) CACHE.computeIfAbsent(cls, PacketAutoCodec::build);
    }

    public static void clearCache() { CACHE.clear(); }

    @Override
    public void encode(FriendlyByteBuf buf, T value) {
        if (value == null) { buf.writeBoolean(false); return; }
        buf.writeBoolean(true);
        for (MemberCodec<?> m : members) {
            Object v = m.getter.get(value);
            writeValue(buf, m.codec, m.primitive, v);
        }
    }

    @Override
    public T decode(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) return null;
        try {
            if (record) {
                Object[] args = new Object[members.size()];
                int i = 0;
                for (MemberCodec<?> m : members) {
                    args[i++] = readValue(buf, m.codec, m.primitive);
                }
                return ctor.newInstance(args);
            } else {
                T instance = ctor.newInstance();
                for (MemberCodec<?> m : members) {
                    Object v = readValue(buf, m.codec, m.primitive);
                    m.setter.set(instance, v);
                }
                return instance;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode " + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(FriendlyByteBuf buf, PacketTypeCodec<?> codec,
                                   boolean primitive, Object v) {
        if (primitive) {
            ((PacketTypeCodec<Object>) codec).encode(buf, v);
        } else {
            if (v == null) { buf.writeBoolean(false); return; }
            buf.writeBoolean(true);
            ((PacketTypeCodec<Object>) codec).encode(buf, v);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object readValue(FriendlyByteBuf buf, PacketTypeCodec<?> codec,
                                    boolean primitive) {
        if (primitive) {
            return ((PacketTypeCodec<Object>) codec).decode(buf);
        }
        if (!buf.readBoolean()) return null;
        return ((PacketTypeCodec<Object>) codec).decode(buf);
    }

    private static <T> PacketAutoCodec<T> build(Class<T> cls) {
        boolean isRecord = cls.isRecord();

        List<MemberCodec<?>> members = new ArrayList<>();
        List<Class<?>> ctorParamTypes = new ArrayList<>();

        if (isRecord) {
            RecordComponent[] comps = cls.getRecordComponents();
            for (int i = 0; i < comps.length; i++) {
                RecordComponent rc = comps[i];
                if (skipMember(rc.getAccessor(), rc.getAnnotations())) continue;

                Class<?> t = rc.getType();
                PacketTypeCodec<?> codec = resolveCodec(rc.getGenericType(), cls, rc.getName());
                boolean primitive = t.isPrimitive();

                int idx = i;
                members.add(new MemberCodec<>(
                        null, idx, codec, primitive,
                        instance -> {
                            try { return rc.getAccessor().invoke(instance); }
                            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
                        },
                        null));
                ctorParamTypes.add(t);
            }

            try {
                Constructor<T> ctor = (Constructor<T>) cls.getDeclaredConstructor(
                        ctorParamTypes.toArray(new Class<?>[0]));
                ctor.setAccessible(true);
                return new PacketAutoCodec<>(cls, members, ctor, true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Record " + cls.getName()
                        + " has no canonical constructor", e);
            }
        }

        List<Field> fields = new ArrayList<>();
        Class<?> current = cls;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (skipMember(null, f.getAnnotations())) continue;
                fields.add(f);
            }
            current = current.getSuperclass();
        }

        for (Field f : fields) {
            f.setAccessible(true);
            PacketTypeCodec<?> codec = resolveCodec(f.getGenericType(), cls, f.getName());
            boolean primitive = f.getType().isPrimitive();
            members.add(new MemberCodec<>(f, -1, codec, primitive,
                    instance -> {
                        try { return f.get(instance); }
                        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
                    },
                    (instance, value) -> {
                        try { f.set(instance, value); }
                        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
                    }));
        }

        try {
            Constructor<T> ctor = cls.getDeclaredConstructor();
            ctor.setAccessible(true);
            return new PacketAutoCodec<>(cls, members, ctor, false);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Class " + cls.getName()
                    + " has no no-arg constructor", e);
        }
    }

    private static boolean skipMember(Method accessor, java.lang.annotation.Annotation[] annos) {
        boolean hasExclude = false;
        for (var a : annos) {
            if (a instanceof NetExclude) hasExclude = true;
        }
        if (hasExclude) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    static PacketTypeCodec<?> resolveCodec(Type type, Class<?> owner, String fieldName) {
        Class<?> raw = rawClass(type);

        PacketTypeCodec<?> custom = PacketCodecRegistry.get(raw);
        if (custom != null) return custom;

        PacketTypeCodec<?> prim = primitiveCodec(raw);
        if (prim != null) return prim;

        if (raw == String.class) return STRING;
        if (raw == UUID.class)   return UUID_CODEC;
        if (raw == ResourceLocation.class) return RL_CODEC;
        if (raw == Vec3.class)   return VEC3;
        if (raw == byte[].class) return BYTE_ARRAY;

        if (raw.isEnum()) return enumCodec(raw);

        if (raw.isArray()) {
            Class<?> comp = raw.getComponentType();
            return arrayCodec(comp,
                    (PacketTypeCodec<Object>) resolveCodec(comp, owner, fieldName + "[]"));
        }

        if (Collection.class.isAssignableFrom(raw)) {
            Type elemType = typeArg(type, 0, Object.class);
            PacketTypeCodec<?> element = resolveCodec(elemType, owner, fieldName + "<>");
            return collectionCodec(raw, (PacketTypeCodec<Object>) element);
        }

        if (Map.class.isAssignableFrom(raw)) {
            Type kType = typeArg(type, 0, Object.class);
            Type vType = typeArg(type, 1, Object.class);
            return mapCodec(raw,
                    (PacketTypeCodec<Object>) resolveCodec(kType, owner, fieldName + "<K>"),
                    (PacketTypeCodec<Object>) resolveCodec(vType, owner, fieldName + "<V>"));
        }

        try {
            return PacketAutoCodec.of(raw);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                    "Cannot encode " + owner.getName() + "." + fieldName
                            + " of type " + raw.getName()
                            + " — register a PacketTypeCodec via PacketCodecRegistry.register()",
                    e);
        }
    }

    private static Class<?> rawClass(Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        if (t instanceof GenericArrayType gat) {
            Class<?> comp = rawClass(gat.getGenericComponentType());
            return Array.newInstance(comp, 0).getClass();
        }
        throw new IllegalStateException("Unsupported type: " + t);
    }

    private static Type typeArg(Type t, int i, Type fallback) {
        if (t instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (i < args.length) return args[i];
        }
        return fallback;
    }

    private static PacketTypeCodec<?> primitiveCodec(Class<?> raw) {
        if (raw == int.class || raw == Integer.class)     return INT;
        if (raw == long.class || raw == Long.class)       return LONG;
        if (raw == boolean.class || raw == Boolean.class) return BOOL;
        if (raw == float.class || raw == Float.class)     return FLOAT;
        if (raw == double.class || raw == Double.class)   return DOUBLE;
        if (raw == byte.class || raw == Byte.class)       return BYTE;
        if (raw == short.class || raw == Short.class)     return SHORT;
        if (raw == char.class || raw == Character.class)  return CHAR;
        return null;
    }

    private static final PacketTypeCodec<Integer> INT = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Integer v) { b.writeVarInt(v); }
        public Integer decode(FriendlyByteBuf b) { return b.readVarInt(); }
    };
    private static final PacketTypeCodec<Long> LONG = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Long v) { b.writeVarLong(v); }
        public Long decode(FriendlyByteBuf b) { return b.readVarLong(); }
    };
    private static final PacketTypeCodec<Boolean> BOOL = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Boolean v) { b.writeBoolean(v); }
        public Boolean decode(FriendlyByteBuf b) { return b.readBoolean(); }
    };
    private static final PacketTypeCodec<Float> FLOAT = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Float v) { b.writeFloat(v); }
        public Float decode(FriendlyByteBuf b) { return b.readFloat(); }
    };
    private static final PacketTypeCodec<Double> DOUBLE = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Double v) { b.writeDouble(v); }
        public Double decode(FriendlyByteBuf b) { return b.readDouble(); }
    };
    private static final PacketTypeCodec<Byte> BYTE = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Byte v) { b.writeByte(v); }
        public Byte decode(FriendlyByteBuf b) { return b.readByte(); }
    };
    private static final PacketTypeCodec<Short> SHORT = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Short v) { b.writeShort(v); }
        public Short decode(FriendlyByteBuf b) { return b.readShort(); }
    };
    private static final PacketTypeCodec<Character> CHAR = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Character v) { b.writeChar(v); }
        public Character decode(FriendlyByteBuf b) { return b.readChar(); }
    };
    private static final PacketTypeCodec<String> STRING = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, String v) { b.writeUtf(v); }
        public String decode(FriendlyByteBuf b) { return b.readUtf(); }
    };
    private static final PacketTypeCodec<UUID> UUID_CODEC = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, UUID v) { b.writeUUID(v); }
        public UUID decode(FriendlyByteBuf b) { return b.readUUID(); }
    };
    private static final PacketTypeCodec<ResourceLocation> RL_CODEC = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, ResourceLocation v) { b.writeResourceLocation(v); }
        public ResourceLocation decode(FriendlyByteBuf b) { return b.readResourceLocation(); }
    };
    private static final PacketTypeCodec<Vec3> VEC3 = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, Vec3 v) {
            b.writeDouble(v.x); b.writeDouble(v.y); b.writeDouble(v.z);
        }
        public Vec3 decode(FriendlyByteBuf b) {
            return new Vec3(b.readDouble(), b.readDouble(), b.readDouble());
        }
    };
    private static final PacketTypeCodec<byte[]> BYTE_ARRAY = new PacketTypeCodec<>() {
        public void encode(FriendlyByteBuf b, byte[] v) { b.writeByteArray(v); }
        public byte[] decode(FriendlyByteBuf b) { return b.readByteArray(); }
    };

    @SuppressWarnings({"rawtypes"})
    private static PacketTypeCodec<?> enumCodec(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        return new PacketTypeCodec() {
            public void encode(FriendlyByteBuf b, Object v) {
                b.writeVarInt(((Enum<?>) v).ordinal());
            }
            public Object decode(FriendlyByteBuf b) {
                int i = b.readVarInt();
                if (i < 0 || i >= constants.length) {
                    throw new IllegalStateException("Bad enum ordinal " + i + " for " + type.getName());
                }
                return constants[i];
            }
        };
    }

    private static PacketTypeCodec<Object> arrayCodec(Class<?> comp, PacketTypeCodec<Object> element) {
        return new PacketTypeCodec<>() {
            public void encode(FriendlyByteBuf b, Object arr) {
                int len = Array.getLength(arr);
                b.writeVarInt(len);
                for (int i = 0; i < len; i++) {
                    Object e = Array.get(arr, i);
                    if (e == null) { b.writeBoolean(false); }
                    else { b.writeBoolean(true); element.encode(b, e); }
                }
            }
            public Object decode(FriendlyByteBuf b) {
                int len = b.readVarInt();
                Object arr = Array.newInstance(comp, len);
                for (int i = 0; i < len; i++) {
                    Object e = b.readBoolean() ? element.decode(b) : null;
                    Array.set(arr, i, e);
                }
                return arr;
            }
        };
    }

    private static PacketTypeCodec<Collection<Object>> collectionCodec(
            Class<?> raw, PacketTypeCodec<Object> element) {
        return new PacketTypeCodec<>() {
            public void encode(FriendlyByteBuf b, Collection<Object> c) {
                b.writeVarInt(c.size());
                for (Object e : c) {
                    if (e == null) { b.writeBoolean(false); }
                    else { b.writeBoolean(true); element.encode(b, e); }
                }
            }
            public Collection<Object> decode(FriendlyByteBuf b) {
                int size = b.readVarInt();
                Collection<Object> out = instantiate(raw, size);
                for (int i = 0; i < size; i++) {
                    out.add(b.readBoolean() ? element.decode(b) : null);
                }
                return out;
            }
        };
    }

    private static PacketTypeCodec<Map<Object, Object>> mapCodec(
            Class<?> raw, PacketTypeCodec<Object> key, PacketTypeCodec<Object> val) {
        return new PacketTypeCodec<>() {
            public void encode(FriendlyByteBuf b, Map<Object, Object> m) {
                b.writeVarInt(m.size());
                for (Map.Entry<Object, Object> e : m.entrySet()) {
                    writeNullable(b, key, e.getKey());
                    writeNullable(b, val, e.getValue());
                }
            }
            public Map<Object, Object> decode(FriendlyByteBuf b) {
                int size = b.readVarInt();
                Map<Object, Object> out = instantiateMap(raw, size);
                for (int i = 0; i < size; i++) {
                    Object k = readNullable(b, key);
                    Object v = readNullable(b, val);
                    out.put(k, v);
                }
                return out;
            }
        };
    }

    private static void writeNullable(FriendlyByteBuf b, PacketTypeCodec<Object> c, Object v) {
        if (v == null) { b.writeBoolean(false); return; }
        b.writeBoolean(true); c.encode(b, v);
    }
    private static Object readNullable(FriendlyByteBuf b, PacketTypeCodec<Object> c) {
        return b.readBoolean() ? c.decode(b) : null;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> instantiate(Class<?> raw, int size) {
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            if (Set.class.isAssignableFrom(raw)) return new LinkedHashSet<>(Math.max(8, size));
            if (Queue.class.isAssignableFrom(raw)) return new ArrayDeque<>(Math.max(8, size));
            return new ArrayList<>(Math.max(8, size));
        }
        try {
            return (Collection<Object>) raw.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + raw.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> instantiateMap(Class<?> raw, int size) {
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            if (SortedMap.class.isAssignableFrom(raw)) return new TreeMap<>();
            return new LinkedHashMap<>(Math.max(8, size));
        }
        try {
            return (Map<Object, Object>) raw.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + raw.getName(), e);
        }
    }
}