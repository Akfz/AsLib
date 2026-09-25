package v.akfz.aslib.event.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple event bus. Thread-safe for registration and dispatch.
 * <p>
 * Register any object with {@link Subscribe} methods — no marker interface required.
 * If the object also implements {@link Listener}, its lifecycle hooks get called.
 * <p>
 * Dispatch walks the event's class hierarchy and interfaces, so a handler for
 * {@code MyBaseEvent} also receives {@code MyChildEvent} instances.
 * Higher priority runs first.
 */
public final class EventBus {

    private final Map<Class<?>, EventHandler<?>[]> handlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, EventHandler<?>[]> hierarchyCache = new ConcurrentHashMap<>();
    private final Map<Object, List<EventHandler<?>>> byListener = new ConcurrentHashMap<>();

    private static final ExecutorService ASYNC_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "aslib-event-async");
        t.setDaemon(true);
        return t;
    });

    public <E extends Event> E postAsync(E event) {
        ASYNC_POOL.execute(() -> post(event));
        return event;
    }

    @SuppressWarnings("unchecked")
    public Registration register(Object listener) {
        Objects.requireNonNull(listener, "listener");

        Class<?> currentClass = listener.getClass();
        List<EventHandler<?>> collected = new ArrayList<>();

        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Subscribe.class)) continue;
                if (method.getParameterCount() != 1) continue;

                Class<?> eventType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(eventType)) continue;

                method.setAccessible(true);
                Subscribe sub = method.getAnnotation(Subscribe.class);

                try {
                    EventInvoker<?> invoker = createInvoker(listener, method, (Class<? extends Event>) eventType);
                    EventHandler<?> handler = new EventHandler<>(
                            invoker, sub.priority(), sub.ignoreCancelled());
                    collected.add(handler);
                    addHandler(eventType, handler);
                } catch (Throwable t) {
                    throw new RuntimeException("Failed to register: " + method, t);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (collected.isEmpty()) {
            return () -> {};
        }

        byListener.put(listener, collected);
        hierarchyCache.clear();

        if (listener instanceof Listener l) {
            l.onRegistered(this);
        }

        return () -> unregister(listener);
    }

    public void registerAndForget(Object listener) {
        register(listener);
    }

    public void unregister(Object listener) {
        List<EventHandler<?>> owned = byListener.remove(listener);
        if (owned == null) return;

        for (EventHandler<?> h : owned) {
            removeHandler(h);
        }
        hierarchyCache.clear();

        if (listener instanceof Listener l) {
            l.onUnregistered(this);
        }
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> E post(E event) {
        EventHandler<?>[] list = hierarchyCache.computeIfAbsent(event.getClass(), this::resolveHandlers);
        for (EventHandler<?> handler : list) {
            ((EventHandler<E>) handler).handle(event);
        }
        return event;
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

    private void addHandler(Class<?> eventType, EventHandler<?> handler) {
        handlers.compute(eventType, (k, oldArr) -> {
            EventHandler<?>[] arr = oldArr == null ? new EventHandler<?>[0] : oldArr;
            EventHandler<?>[] newArr = Arrays.copyOf(arr, arr.length + 1);
            newArr[arr.length] = handler;

            Arrays.sort(newArr,
                    Comparator.comparingInt((EventHandler<?> h) -> h.priority().ordinal()).reversed()
            );
            return newArr;
        });
    }

    private void removeHandler(EventHandler<?> handler) {
        for (Map.Entry<Class<?>, EventHandler<?>[]> entry : handlers.entrySet()) {
            EventHandler<?>[] arr = entry.getValue();
            int idx = -1;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == handler) { idx = i; break; }
            }
            if (idx < 0) continue;

            if (arr.length == 1) {
                handlers.remove(entry.getKey());
            } else {
                EventHandler<?>[] newArr = new EventHandler<?>[arr.length - 1];
                System.arraycopy(arr, 0, newArr, 0, idx);
                System.arraycopy(arr, idx + 1, newArr, idx, arr.length - idx - 1);
                entry.setValue(newArr);
            }
            return;
        }
    }

    private <E extends Event> EventInvoker<E> createInvoker(
            Object listener, Method method, Class<E> eventClass) throws Throwable {

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle bound = lookup.unreflect(method).bindTo(listener);
        MethodHandle adapted = bound.asType(MethodType.methodType(void.class, Event.class));

        return event -> {
            try {
                adapted.invokeExact(event);
            } catch (Throwable t) {
                throw new RuntimeException("Error in listener: " + method, t);
            }
        };
    }

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        void unregister();
        @Override default void close() { unregister(); }
    }
}