package n.paradox.aslib.event.api;

@FunctionalInterface
public interface EventInvoker<E extends Event> {
    void invoke(E event);
}