package v.akfz.aslib.util.scheduler;

import v.akfz.aslib.event.impl.TickUpdater;
import v.akfz.aslib.initializer.SideEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tick- and time-based task scheduler. Two flavors:
 * <ul>
 *   <li><b>main-thread</b> — runs on the client or server tick (whichever side
 *       you picked). Safe to touch game state inside.</li>
 *   <li><b>async</b> — runs on a small daemon worker pool. Do I/O, file parsing,
 *       heavy math. To touch game state afterward, hop back with
 *       {@link #async(Runnable, Runnable)}.</li>
 * </ul>
 * <p>
 * Pick a side:
 * <ul>
 *   <li>{@link #client()} — ticks with the client</li>
 *   <li>{@link #server()} — ticks with the server</li>
 *   <li>{@link #current()} — picks by {@link SideEnvironment}</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>{@code
 * Scheduler.server().after(100, () -> player.sendSystemMessage(...));  // 5s later
 * Scheduler.client().every(20, () -> updateHud());                     // once per sec
 * Scheduler.server().async(() -> loadBigFile())
 *     .thenOnServer(() -> applyLoadedData());
 * }</pre>
 */
public final class Scheduler {

	private static final ScheduledExecutorService POOL =
			Executors.newScheduledThreadPool(2, r -> {
				Thread t = new Thread(r, "aslib-scheduler");
				t.setDaemon(true);
				return t;
			});

	private static final Scheduler CLIENT = new Scheduler();
	private static final Scheduler SERVER = new Scheduler();

	private final List<Task> tasks = new ArrayList<>();
	private long tickCount = 0;

	private Scheduler() {}

	public static Scheduler client() { return CLIENT; }
	public static Scheduler server() { return SERVER; }

	public static Scheduler current() {
		return SideEnvironment.getCurrentSide() == SideEnvironment.Side.Client
				? CLIENT : SERVER;
	}

	public Task next(Runnable action) {
		return after(1, action);
	}

	public Task after(int ticks, Runnable action) {
		Task t = new Task(action);
		synchronized (tasks) {
			t.nextTick = tickCount + Math.max(1, ticks);
			tasks.add(t);
		}
		return t;
	}

	public Task every(int ticks, Runnable action) {
		return every(ticks, ticks, action);
	}

	public Task every(int ticks, int initialDelay, Runnable action) {
		if (ticks <= 0) throw new IllegalArgumentException("interval must be > 0");
		Task t = new Task(action);
		synchronized (tasks) {
			t.nextTick = tickCount + Math.max(1, initialDelay);
			t.interval = ticks;
			tasks.add(t);
		}
		return t;
	}

	public Task afterMs(long ms, Runnable action) {
		Task t = new Task(action);
		synchronized (tasks) {
			t.untilMs = System.currentTimeMillis() + Math.max(1, ms);
			tasks.add(t);
		}
		return t;
	}

	public Task everyMs(long ms, Runnable action) {
		if (ms <= 0) throw new IllegalArgumentException("interval must be > 0");
		Task t = new Task(action);
		synchronized (tasks) {
			t.untilMs = System.currentTimeMillis() + ms;
			t.intervalMs = ms;
			tasks.add(t);
		}
		return t;
	}

	public Task async(Runnable action) {
		Task t = new Task(action);
		t.future = POOL.submit(() -> safeRun(t));
		return t;
	}

	public Task async(Runnable action, Runnable thenOnMain) {
		Task t = new Task(action);
		t.future = POOL.submit(() -> {
			if (t.isCancelled()) return;
			try {
				action.run();
			} catch (Throwable ex) {
				logTaskError(ex);
				return;
			}
			if (thenOnMain != null && !t.isCancelled()) {
				next(thenOnMain);
			}
		});
		return t;
	}

	public Task asyncAfterMs(long ms, Runnable action) {
		Task t = new Task(action);
		t.future = POOL.schedule(() -> safeRun(t), Math.max(1, ms), TimeUnit.MILLISECONDS);
		return t;
	}

	public void cancelAll() {
		synchronized (tasks) {
			for (Task t : tasks) t.cancelled = true;
			tasks.clear();
		}
	}

	public int size() {
		synchronized (tasks) { return tasks.size(); }
	}

	private void tick() {
		tickCount++;
		long now = System.currentTimeMillis();

		List<Task> toRun = null;

		synchronized (tasks) {
			Iterator<Task> it = tasks.iterator();
			while (it.hasNext()) {
				Task t = it.next();
				if (t.cancelled) { it.remove(); continue; }

				boolean fire = false;
				boolean keep = false;

				if (t.untilMs > 0) {
					if (now >= t.untilMs) {
						fire = true;
						if (t.intervalMs > 0) {
							t.untilMs = now + t.intervalMs;
							keep = true;
						}
					} else {
						keep = true;
					}
				} else if (t.interval > 0) {
					if (tickCount >= t.nextTick) {
						fire = true;
						t.nextTick = tickCount + t.interval;
						keep = true;
					} else {
						keep = true;
					}
				} else {
					if (tickCount >= t.nextTick) {
						fire = true;
					}
				}

				if (fire) {
					if (toRun == null) toRun = new ArrayList<>(4);
					toRun.add(t);
				}
				if (!keep) it.remove();
			}
		}

		if (toRun != null) {
			for (Task t : toRun) {
				if (!t.cancelled) safeRun(t);
			}
		}
	}

	private void safeRun(Task t) {
		if (t.cancelled) return;
		try {
			t.action.run();
		} catch (Throwable ex) {
			logTaskError(ex);
		}
	}

	private static void logTaskError(Throwable ex) {
		System.err.println("[ASLib Scheduler] task threw:");
		ex.printStackTrace();
	}

	public static void dispatch(TickUpdater event) {
		if (event.client) CLIENT.tick();
		else SERVER.tick();
	}
}