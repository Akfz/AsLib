package v.akfz.aslib.event.api;

/**
 * Listener marker. Optional — {@link EventBus#register} accepts any object
 * with {@code @Subscribe} methods. Implementing {@code Listener} just gives
 * you the lifecycle hooks.
 */
public interface Listener {

	/** Called right after this listener's handlers are wired into the bus. */
	default void onRegistered(EventBus bus) {}

	/** Called after all of this listener's handlers are removed. */
	default void onUnregistered(EventBus bus) {}
}