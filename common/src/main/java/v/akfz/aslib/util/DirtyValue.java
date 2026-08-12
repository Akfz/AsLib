package v.akfz.aslib.util;

import java.util.Objects;

//в основном для синхрона
public class DirtyValue<T> {
    private T value;
    private boolean dirty;

    public DirtyValue(T initialValue) {
        this.value = initialValue;
        this.dirty = false;
    }

    public static <T> DirtyValue<T> of(T val) {
        return new DirtyValue<>(val);
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public T get() {
        return this.value;
    }

    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            this.dirty = true;
        }
    }

    public void markClean() {
        this.dirty = false;
    }

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirtyValue<?> that = (DirtyValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}