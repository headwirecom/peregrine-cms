package com.peregrine.commons.concurrent;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

/**
 * Found at <a href=
 * "https://stackoverflow.com/questions/4742210/implementing-debounce-in-java">Stack
 * Overflow</a>
 */
public final class DeBouncer<Element> implements Callback<Element>, ElementAdjuster<Element> {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ConcurrentHashMap<Element, TimerTask> delayedTasks = new ConcurrentHashMap<>();
	private final Set<Element> emptySet = Collections.emptySet();
	private final Callback<Element> callback;
	private final ElementAdjuster<Element> adjuster;
	private final int interval;
	private final TimeUnit timeUnit;

	public DeBouncer(final Callback<Element> callback, final ElementAdjuster<Element> adjuster, final int interval, final TimeUnit timeUnit) {
		this.callback = callback;
		this.adjuster = nonNull(adjuster) ? adjuster : this;
		this.interval = Math.max(0, interval);
		this.timeUnit = timeUnit;
	}

	public DeBouncer(final Callback<Element> callback, final int interval, final TimeUnit timeUnit) {
		this(callback, null, interval, timeUnit);
	}

	public DeBouncer(final Callback<Element> callback, final ElementAdjuster<Element> adjuster, final int interval) {
		this(callback, adjuster, interval, TimeUnit.MILLISECONDS);
	}

	public DeBouncer(final Callback<Element> callback, final int interval) {
		this(callback, interval, TimeUnit.MILLISECONDS);
	}

	public void call(final Element key) {
		final ConcurrentHashMap.KeySetView<Element, TimerTask> oldElements = delayedTasks.keySet();
		adjuster.findSubElements(key, oldElements).stream()
				.map(delayedTasks::get)
				.filter(Objects::nonNull)
				.forEach(TimerTask::revoke);
		final Element adjustedKey = adjuster.findSuperElement(key, oldElements);
		final TimerTask task = new TimerTask(adjustedKey);
		TimerTask prev;
		do {
			prev = delayedTasks.putIfAbsent(adjustedKey, task);
			if (prev == null) {
				scheduler.schedule(task, interval, timeUnit);
			}
		} while (prev != null && !prev.extend());
	}

	public Set<Element> terminate() {
		scheduler.shutdownNow();
		return delayedTasks.keySet();
	}

	public void finishAndTerminate() {
		for (final Element arg : terminate()) {
			callback.call(arg);
		}
	}

	@Override
	public Set<Element> findSubElements(final Element newElement, final Set<Element> oldElements) {
		return emptySet;
	}

	@Override
	public Element findSuperElement(final Element newElement, final Set<Element> oldElements) {
		return newElement;
	}

	/** The task that wakes up when the wait time elapses */
	private final class TimerTask implements Runnable {

		private final Object lock = new Object();
		private final Element key;
		private long dueTime;
		private boolean revoke = false;

		public TimerTask(final Element key) {
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

		public void revoke() {
			synchronized (lock) {
				revoke = true;
				dueTime = -1;
				delayedTasks.remove(key);
			}
		}

		public void run() {
			synchronized (lock) {
				if (revoke) {
					return;
				}

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
