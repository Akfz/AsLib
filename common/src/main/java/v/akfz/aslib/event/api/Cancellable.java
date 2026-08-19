package v.akfz.aslib.event.api;

/**
 * Add if you need to cancel, but you need to implement it yourself
 * More in {@link Event}
 */
public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}