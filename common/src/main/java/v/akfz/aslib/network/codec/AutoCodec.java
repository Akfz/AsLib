package v.akfz.aslib.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutoCodec {

    private static final Map<Class<?>, TypeCodec<?>> TYPE_CODECS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    static {
        registerType(int.class, Integer.class, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
        registerType(float.class, Float.class, FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
        registerType(double.class, Double.class, FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble);
        registerType(boolean.class, Boolean.class, FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
        registerType(long.class, Long.class, FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong);

        registerType(String.class, new TypeCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, String value) {
                buf.writeUtf(value);
            }

            @Override
            public String decode(FriendlyByteBuf buf) {
                return buf.readUtf();
            }
        });

        registerType(ResourceLocation.class, new TypeCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, ResourceLocation value) {
                buf.writeResourceLocation(value);
            }

            @Override
            public ResourceLocation decode(FriendlyByteBuf buf) {
                return buf.readResourceLocation();
            }
        });

        registerType(UUID.class, new TypeCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, UUID value) {
                buf.writeUUID(value);
            }

            @Override
            public UUID decode(FriendlyByteBuf buf) {
                return buf.readUUID();
            }
        });

        registerType(Vec3.class, new TypeCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, Vec3 value) {
                buf.writeDouble(value.x);
                buf.writeDouble(value.y);
                buf.writeDouble(value.z);
            }

            @Override
            public Vec3 decode(FriendlyByteBuf buf) {
                return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            }
        });
    }

    public static <T> void registerType(Class<T> clazz, TypeCodec<T> codec) {
        TYPE_CODECS.put(clazz, codec);
    }

    public static <T> void registerType(Class<T> primClass, Class<T> wrapClass, TypeEncoder<T> encoder, TypeDecoder<T> decoder) {
        TypeCodec<T> codec = new TypeCodec<>() {
            @Override public void encode(FriendlyByteBuf buf, T value) { encoder.encode(buf, value); }
            @Override public T decode(FriendlyByteBuf buf) { return decoder.decode(buf); }
        };
        if (primClass != null) TYPE_CODECS.put(primClass, codec);
        if (wrapClass != null) TYPE_CODECS.put(wrapClass, codec);
    }

    @SuppressWarnings("unchecked")
    public static void encode(FriendlyByteBuf buf, Object obj) {
        if (obj == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);

        Class<?> clazz = obj.getClass();
        TypeCodec<Object> codec = (TypeCodec<Object>) TYPE_CODECS.get(clazz);

        if (codec != null) {
            codec.encode(buf, obj);
            return;
        }

        try {
            if (clazz.isRecord()) {
                for (RecordComponent component : clazz.getRecordComponents()) {
                    Object val = component.getAccessor().invoke(obj);
                    encode(buf, val);
                }
            } else {
                List<Field> fields = getFields(clazz);
                for (Field field : fields) {
                    encode(buf, field.get(obj));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode class: " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T decode(FriendlyByteBuf buf, Class<T> clazz) {
        if (!buf.readBoolean()) {
            return null;
        }

        TypeCodec<T> codec = (TypeCodec<T>) TYPE_CODECS.get(clazz);

        if (codec != null) {
            return codec.decode(buf);
        }

        try {
            if (clazz.isRecord()) {
                RecordComponent[] components = clazz.getRecordComponents();
                Class<?>[] paramTypes = new Class<?>[components.length];
                Object[] args = new Object[components.length];

                for (int i = 0; i < components.length; i++) {
                    paramTypes[i] = components[i].getType();
                    args[i] = decode(buf, paramTypes[i]);
                }

                Constructor<T> canonicalConstructor = clazz.getDeclaredConstructor(paramTypes);
                canonicalConstructor.setAccessible(true);
                return canonicalConstructor.newInstance(args);
            } else {
                T instance = clazz.getDeclaredConstructor().newInstance();
                List<Field> fields = getFields(clazz);

                for (Field field : fields) {
                    Object fieldValue = decode(buf, field.getType());
                    field.set(instance, fieldValue);
                }
                return instance;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode class: " + clazz.getName(), e);
        }
    }

    private static List<Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> list = new ArrayList<>();
            Class<?> current = c;
            while (current != null && current != Object.class) {
                for (Field f : current.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                        f.setAccessible(true);
                        list.add(f);
                    }
                }
                current = current.getSuperclass();
            }
            return list;
        });
    }

    @FunctionalInterface public interface TypeEncoder<T> { void encode(FriendlyByteBuf buf, T val); }
    @FunctionalInterface public interface TypeDecoder<T> { T decode(FriendlyByteBuf buf); }
}