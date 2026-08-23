package v.akfz.aslib.event.api;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guide {@link Event}
 */
public final class EventBus {

    private final Map<Class<?>, EventHandler<?>[]> handlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, EventHandler<?>[]> hierarchyCache = new ConcurrentHashMap<>();

    public void register(Listener listener) {
        Class<?> currentClass = listener.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {

                if (!method.isAnnotationPresent(Subscribe.class)) continue;
                if (method.getParameterCount() != 1) continue;

                Class<?> eventType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(eventType)) continue;

                method.setAccessible(true);
                Subscribe subscribe = method.getAnnotation(Subscribe.class);

                try {
                    EventInvoker<?> invoker = createInvoker(listener, method, (Class<? extends Event>) eventType);

                    EventHandler<?> handler = new EventHandler<>(
                            invoker,
                            subscribe.priority(),
                            subscribe.ignoreCancelled()
                    );

                    handlers.compute(eventType, (k, oldArr) -> {
                        EventHandler<?>[] arr = oldArr == null ? new EventHandler<?>[0] : oldArr;
                        EventHandler<?>[] newArr = Arrays.copyOf(arr, arr.length + 1);
                        newArr[arr.length] = handler;

                        Arrays.sort(newArr,
                                Comparator.comparingInt((EventHandler<?> h) -> h.priority().ordinal()).reversed()
                        );

                        return newArr;
                    });

                } catch (Throwable e) {
                    throw new RuntimeException("Failed to register listener method: " + method, e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        hierarchyCache.clear();
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void post(E event) {
        Class<?> eventClass = event.getClass();
        EventHandler<?>[] list = hierarchyCache.computeIfAbsent(eventClass, this::resolveHandlers);

        for (EventHandler<?> handler : list) {
            ((EventHandler<E>) handler).handle(event);
        }
    }

    private EventHandler<?>[] resolveHandlers(Class<?> eventClass) {
        List<EventHandler<?>> result = new ArrayList<>();
        Class<?> current = eventClass;

        while (current != null && current != Object.class) {
            EventHandler<?>[] direct = handlers.get(current);
            if (direct != null) Collections.addAll(result, direct);

            for (Class<?> iface : current.getInterfaces()) {
                EventHandler<?>[] ifaceHandlers = handlers.get(iface);
                if (ifaceHandlers != null) Collections.addAll(result, ifaceHandlers);
            }

            current = current.getSuperclass();
        }

        return result.toArray(EventHandler[]::new);
    }

    @SuppressWarnings("unchecked")
    private <E extends Event> EventInvoker<E> createInvoker(
            Object listener,
            Method method,
            Class<E> eventClass
    ) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle boundHandle = lookup.unreflect(method).bindTo(listener);
        MethodHandle adaptedHandle = boundHandle.asType(MethodType.methodType(void.class, Event.class));

        return event -> {
            try {
                adaptedHandle.invokeExact(event);
            } catch (Throwable ex) {
                throw new RuntimeException("Error executing event listener: " + method, ex);
            }
        };
    }
}