package v.akfz.aslib.event.api;

/**
 * Base class for all events.
 * <p>
 * Usage example:
 * <ol>
 *   <li>Create an event class extending {@link Event}:
 *     <pre>{@code
 *     public class FirstTickEvent extends Event {}
 *     }</pre>
 *   </li>
 *   <li>Create a listener implementing {@link Listener} with {@link Subscribe} annotation:
 *     <pre>{@code
 *     public class FirstTickListener implements Listener {
 *         @Subscribe(priority = EventPriority.HIGHEST)
 *         public void execute(FirstTickEvent event) {
 *             // Your event logic here
 *         }
 *     }
 *     }</pre>
 *   </li>
 *   <li>Register and post events (<b>MUST</b> be done via {@link v.akfz.aslib.AsLib#EVENT_BUS}):
 *     <pre>{@code
 *     // Registering a listener:
 *     v.akfz.aslib.AsLib.EVENT_BUS.register(new FirstTickListener());
 *
 *     // Posting an event:
 *     v.akfz.aslib.AsLib.EVENT_BUS.post(new FirstTickEvent());
 *     }</pre>
 *   </li>
 * </ol>
 */
public abstract class Event {
}