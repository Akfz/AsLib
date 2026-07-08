package v.akfz.aslib.event.api;

public final class EventHandler<E extends Event> {

    private final EventInvoker<E> invoker;
    private final EventPriority priority;
    private final boolean ignoreCancelled;

    public EventHandler(EventInvoker<E> invoker, EventPriority priority, boolean ignoreCancelled) {
        this.invoker = invoker;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
    }

    public EventPriority priority() {
        return priority;
    }

    public void handle(E event) {
        if (ignoreCancelled && event instanceof Cancellable c && c.isCancelled()) return;
        invoker.invoke(event);
    }
}