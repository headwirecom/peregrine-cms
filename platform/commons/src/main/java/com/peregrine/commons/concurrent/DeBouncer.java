package com.peregrine.commons.concurrent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Found at <a href=
 * "https://stackoverflow.com/questions/4742210/implementing-debounce-in-java">Stack
 * Overflow</a>
 */
public final class DeBouncer<Argument> implements Callback<Argument> {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ConcurrentHashMap<Argument, TimerTask> delayedTasks = new ConcurrentHashMap<Argument, TimerTask>();
	private final Callback<Argument> callback;
	private final int interval;
	private final TimeUnit timeUnit;

	public DeBouncer(final Callback<Argument> callback, final int interval, final TimeUnit timeUnit) {
		this.callback = callback;
		this.interval = interval;
		this.timeUnit = timeUnit;
	}

	public DeBouncer(final Callback<Argument> callback, final int interval) {
		this(callback, interval, TimeUnit.MILLISECONDS);
	}

	public void call(final Argument key) {
		final TimerTask task = new TimerTask(key);

		TimerTask prev;
		do {
			prev = delayedTasks.putIfAbsent(key, task);
			if (prev == null) {
				scheduler.schedule(task, interval, timeUnit);
			}
		} while (prev != null && !prev.extend());
	}

	public Set<Argument> terminate() {
		scheduler.shutdownNow();
		return delayedTasks.keySet();
	}

	/** The task that wakes up when the wait time elapses */
	private final class TimerTask implements Runnable {

		private final Object lock = new Object();
		private final Argument key;
		private long dueTime;

		public TimerTask(final Argument key) {
			this.key = key;
			extend();
		}

		public boolean extend() {
			synchronized (lock) {
				if (dueTime < 0) { // Task has been shutdown
					return false;
				}

				dueTime = System.currentTimeMillis() + interval;

				return true;
			}
		}

		public void run() {
			synchronized (lock) {
				final long remaining = dueTime - System.currentTimeMillis();
				if (remaining > 0) { // Re-schedule task
					scheduler.schedule(this, remaining, timeUnit);
				} else { // Mark as terminated and invoke callback
					dueTime = -1;
					try {
						callback.call(key);
					} finally {
						delayedTasks.remove(key);
					}
				}
			}
		}

	}

}
