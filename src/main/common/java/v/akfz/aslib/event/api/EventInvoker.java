package v.akfz.aslib.event.api;

/**
 * Guide {@link Event}
 * What are you doing here 😏
 */
@FunctionalInterface
public interface EventInvoker<E extends Event> {
    void invoke(E event);
}