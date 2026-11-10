/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import java.time.Duration;

/**
 * Runs an action if loading the problem takes longer than a time limit.
 * <p>
 * Refinery's parser can loop forever on some syntax errors, for example on a class body cut off by the end of the
 * file. Such a loop doesn't check for cancellation or interrupts, so the only way out is to end the process: the
 * action writes an error result first, so the caller still learns that the problem is invalid instead of waiting for
 * its own timeout.
 */
public class LoadWatchdog implements AutoCloseable {
	private final Object lock = new Object();
	private final Thread thread;
	private boolean done;

	public LoadWatchdog(double timeoutSeconds, Runnable onTimeout) {
		var timeout = Duration.ofNanos(Math.round(timeoutSeconds * 1e9));
		thread = Thread.ofPlatform().daemon().name("scenegems-load-watchdog").start(() -> {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			synchronized (lock) {
				if (done) {
					return;
				}
				done = true;
			}
			onTimeout.run();
		});
	}

	@Override
	public void close() {
		synchronized (lock) {
			done = true;
		}
		thread.interrupt();
	}
}
