package v.akfz.aslib.util.scheduler;

import v.akfz.aslib.event.api.EventPriority;
import v.akfz.aslib.event.api.Listener;
import v.akfz.aslib.event.api.Subscribe;
import v.akfz.aslib.event.impl.TickUpdater;

/**
 * Drives both schedulers from the tick event. Registered once in AsLib.init().
 * LOWEST priority so other listeners run first — scheduler tasks see a
 * fully-updated world.
 */
public class SchedulerListener implements Listener {
	@Subscribe(priority = EventPriority.LOWEST)
	public void onTick(TickUpdater event) {
		Scheduler.dispatch(event);
	}
}