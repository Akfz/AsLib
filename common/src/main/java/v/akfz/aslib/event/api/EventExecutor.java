package v.akfz.aslib.event.api;

import java.lang.invoke.MethodHandle;

public final class EventExecutor<E> {

    private final Object listener;
    private final MethodHandle handle;

    public EventExecutor(Object listener, MethodHandle handle) {
        this.listener = listener;
        this.handle = handle;
    }

    public void execute(E event) {
        try {
            handle.invoke(listener, event);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}