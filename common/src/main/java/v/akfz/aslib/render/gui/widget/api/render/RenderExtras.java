package v.akfz.aslib.render.gui.widget.api.render;

import java.util.HashMap;
import java.util.Map;

public class RenderExtras {
    private final Map<String, RenderObject> data = new HashMap<>();

    public record RenderObject(Class<?> objectClass, Object object) {}

    public <T> RenderExtras with(String name, Class<T> type, T value) {
        data.put(name, new RenderObject(type, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, Class<T> type) {
        RenderObject wrapper = data.get(name);
        if (wrapper == null) {
            return null;
        }

        if (type.isAssignableFrom(wrapper.objectClass()) || type.isInstance(wrapper.object())) {
            return (T) wrapper.object();
        }

        throw new ClassCastException(String.format(
                "Не удалось привести параметр '%s' из типа %s к запрашиваемому типу %s",
                name, wrapper.objectClass().getName(), type.getName()
        ));
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String name, Class<T> type, T defaultValue) {
        RenderObject wrapper = data.get(name);
        if (wrapper == null || !type.isInstance(wrapper.object())) {
            return defaultValue;
        }
        return (T) wrapper.object();
    }
}