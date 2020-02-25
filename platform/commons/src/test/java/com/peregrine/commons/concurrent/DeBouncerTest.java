package com.peregrine.commons.concurrent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class DeBouncerTest implements Callback<Integer> {

	private static final int INTERVAL = 150;
	private static final int BUFFER = 50;
	private static final int BUFFERED_INTERVAL = INTERVAL + BUFFER;
	private static final int HALF_INTERVAL = INTERVAL / 2;

	private final DeBouncer<Integer> model = new DeBouncer<Integer>(this, INTERVAL);

	private int value;

	@Override
	public void call(final Integer value) {
		this.value = value;
	}

	@Test
	public synchronized void testAll() throws InterruptedException {
		final int desiredValue = 1;
		final int otherValue = 2;

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

}
