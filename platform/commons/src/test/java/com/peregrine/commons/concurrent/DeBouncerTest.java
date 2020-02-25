package com.peregrine.commons.concurrent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public final class DeBouncerTest implements Callback<Integer>, ElementAdjuster<Integer> {

	private static final int INTERVAL = 150;
	private static final int BUFFER = 50;
	private static final int BUFFERED_INTERVAL = INTERVAL + BUFFER;
	private static final int HALF_INTERVAL = INTERVAL / 2;

	private final int desiredValue = 1;
	private final int otherValue = 2;

	private int value;

	@Override
	public void call(final Integer value) {
		this.value = value;
	}

	@Test
	public synchronized void testBasic() throws InterruptedException {
		final DeBouncer<Integer> model = new DeBouncer<>(this, INTERVAL);

		model.call(otherValue);
		wait(INTERVAL);
		model.call(otherValue);

		wait(HALF_INTERVAL);
		model.call(desiredValue);

		wait(HALF_INTERVAL);
		model.call(desiredValue);

		wait(BUFFERED_INTERVAL);
		model.call(otherValue);
		model.terminate();

		Assert.assertEquals(desiredValue, value);
	}

	@Test
	public synchronized void testAdjuster() throws InterruptedException {
		final DeBouncer<Integer> model = new DeBouncer<>(this, this, INTERVAL);

		model.call(otherValue);
		wait(INTERVAL);
		model.call(otherValue);

		wait(HALF_INTERVAL);
		model.call(desiredValue);

		wait(HALF_INTERVAL);
		model.call(desiredValue);

		wait(BUFFERED_INTERVAL);
		model.call(otherValue);
		model.call(desiredValue);
		model.finishAndTerminate();

		Assert.assertEquals(desiredValue, value);
	}

	@Override
	public Integer findSuperElement(final Integer newElement, final Set<Integer> oldElements) {
		return newElement;
	}

	@Override
	public Set<Integer> findSubElements(final Integer newElement, final Set<Integer> oldElements) {
		final Set<Integer> result = new HashSet<>();
		result.add(otherValue);
		return result;
	}
}
