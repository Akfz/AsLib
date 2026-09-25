package v.akfz.aslib.util.scheduler;

import java.util.concurrent.Future;

/**
 * Handle to a scheduled task. Cancel any time — cancellation is idempotent.
 * Safe to call {@link #cancel()} from another thread.
 */
public final class Task {

	final Runnable action;

	long nextTick;
	int interval;
	long untilMs;
	long intervalMs;

	volatile boolean cancelled;
	volatile Future<?> future;

	Task(Runnable action) {
		this.action = action;
	}

	public void cancel() {
		cancelled = true;
		Future<?> f = future;
		if (f != null) f.cancel(false);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isAsync() {
		return future != null;
	}
}