package v.akfz.aslib.util.math;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class CompositePair<T extends Number> {

    protected T start;
    protected final Map<String, T> addedParts = new HashMap<>();

    protected final Function<Double, T> caster;
    protected final Class<T> clazz;

    protected CompositePair(T start) {
        this.start = start;
        this.clazz = (Class<T>) start.getClass();
        this.caster = this.createCaster(this.clazz);
    }

    public void setStart(T val) {
        this.start = val;
    }

    public T getStart() {
        return this.start;
    }

    public void setPart(String key, T value) {
        addedParts.put(key.toLowerCase(), value);
    }

    public void removePart(String key) {
        addedParts.remove(key.toLowerCase());
    }

    public void clear() {
        addedParts.clear();
    }

    public Map<String, T> getParts() {
        return new HashMap<>(addedParts);
    }

    public T get() {
        if (addedParts.isEmpty()) return start;

        double sum = start.doubleValue();
        for (T val : addedParts.values()) {
            if (val != null) sum += val.doubleValue();
        }

        return caster.apply(sum);
    }

    private Function<Double, T> createCaster(Class<T> clazz) {
        if (clazz == Float.class)   return d -> (T) (Object) Float.valueOf(d.floatValue());
        if (clazz == Double.class)  return d -> (T) (Object) Double.valueOf(d);
        if (clazz == Integer.class) return d -> (T) (Object) Integer.valueOf(d.intValue());
        if (clazz == Long.class)    return d -> (T) (Object) Long.valueOf(d.longValue());
        if (clazz == Short.class)   return d -> (T) (Object) Short.valueOf(d.shortValue());
        if (clazz == Byte.class)    return d -> (T) (Object) Byte.valueOf(d.byteValue());

        return d -> (T) (Object) d;
    }

    public static <T extends Number> CompositePair<T> of(T start) {
        return new CompositePair<>(start);
    }

    public static <T extends Number> CompositePair<T> empty(Class<T> clazz) {
        T zero;
        if (clazz == Float.class)   zero = (T) Float.valueOf(0f);
        else if (clazz == Double.class) zero = (T) Double.valueOf(0d);
        else if (clazz == Integer.class) zero = (T) Integer.valueOf(0);
        else if (clazz == Long.class)    zero = (T) Long.valueOf(0L);
        else if (clazz == Short.class)   zero = (T) Short.valueOf((short) 0);
        else if (clazz == Byte.class)    zero = (T) Byte.valueOf((byte) 0);
        else throw new IllegalArgumentException("Unsupported type: " + clazz);

        return new CompositePair<>(zero);
    }

    public void add(String part, T amount) {
        if (amount == null || part == null) return;

        T current = this.getPart(part);
        double currentVal = (current != null) ? current.doubleValue() : 0.0;
        double newSum = currentVal + amount.doubleValue();

        this.setPart(part, this.caster.apply(newSum));
    }

    public void addStart(T amount) {
        if (amount == null) return;

        double newStart = this.start.doubleValue() + amount.doubleValue();
        this.setStart(this.caster.apply(newStart));
    }

    public void modifyAnyButNot(BiFunction<String, T, T> modifier, String... keys) {
        if (keys == null) {
            keys = new String[0];
        }

        Set<String> excludedKeys = new HashSet<>();
        for (String key : keys) {
            if (key != null) {
                excludedKeys.add(key.toLowerCase());
            }
        }

        for (Map.Entry<String, T> entry : this.addedParts.entrySet()) {
            if (excludedKeys.contains(entry.getKey())) {
                continue;
            }

            T val = entry.getValue();
            if (val != null) {
                T newValue = modifier.apply(entry.getKey(), val);
                this.addedParts.put(entry.getKey(), newValue);
            }
        }
    }

    public T getPart(String key) {
        return this.addedParts.get(key.toLowerCase());
    }

    public T getAnyButNot(String... keys) {
        if (keys == null || keys.length == 0) {
            return this.get();
        }

        Set<String> excludedKeys = new HashSet<>();
        for (String key : keys) {
            if (key != null) {
                excludedKeys.add(key.toLowerCase());
            }
        }

        double sum = this.start.doubleValue();

        for (Map.Entry<String, T> entry : this.addedParts.entrySet()) {
            if (excludedKeys.contains(entry.getKey())) {
                continue;
            }
            T val = entry.getValue();
            if (val != null) {
                sum += val.doubleValue();
            }
        }

        return this.caster.apply(sum);
    }
}
