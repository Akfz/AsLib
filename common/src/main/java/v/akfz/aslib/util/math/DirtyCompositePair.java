package v.akfz.aslib.util.math;

import java.util.function.BiFunction;

public class DirtyCompositePair<T extends Number> extends CompositePair<T> {
    private T cachedValue;
    private boolean dirty = true;

    public DirtyCompositePair(T start) {
        super(start);
        updateCache();
    }

    private void updateCache() {
        this.cachedValue = super.get();
    }

    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setStart(T val) {
        super.setStart(val);
        updateCache();
        this.dirty = true;
    }

    @Override
    public void setPart(String key, T value) {
        super.setPart(key, value);
        updateCache();
        this.dirty = true;
    }

    @Override
    public void removePart(String key) {
        super.removePart(key);
        updateCache();
        this.dirty = true;
    }

    @Override
    public void clear() {
        super.clear();
        updateCache();
        this.dirty = true;
    }

    @Override
    public T get() {
        if (this.dirty) {
            updateCache();
        }
        return this.cachedValue;
    }

    public void markClean() {
        updateCache();
        this.dirty = false;
    }

    @Override
    public void modifyAnyButNot(BiFunction<String, T, T> modifier, String... keys) {
        super.modifyAnyButNot(modifier, keys);
        updateCache();
        this.dirty = true;
    }

    public static <T extends Number> DirtyCompositePair<T> of(T start) {
        return new DirtyCompositePair<>(start);
    }

    public static <T extends Number> DirtyCompositePair<T> empty(Class<T> clazz) {
        return new DirtyCompositePair<>(CompositePair.empty(clazz).getStart());
    }
}