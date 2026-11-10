/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LoadWatchdogTest {
	@Test
	void firesAfterTimeout() throws InterruptedException {
		var fired = new CountDownLatch(1);
		try (var ignored = new LoadWatchdog(0.01, fired::countDown)) {
			assertThat(fired.await(10, TimeUnit.SECONDS), is(true));
		}
	}

	@Test
	void closingCancels() throws InterruptedException {
		var fired = new CountDownLatch(1);
		var watchdog = new LoadWatchdog(0.2, fired::countDown);
		watchdog.close();
		assertThat(fired.await(1, TimeUnit.SECONDS), is(false));
	}
}
