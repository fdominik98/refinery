/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import tools.refinery.store.util.CancellationToken;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.LongSupplier;

/**
 * Requests a stop when a file appears, so a caller that can only copy files into the container (like the SceneGEMS
 * launcher with {@code docker cp}) can end the generation early and still get the models found so far.
 * <p>
 * The generator checks for cancellation very often, so the file system is looked at only every
 * {@link #CHECK_INTERVAL_NANOS}.
 */
public class StopFileCancellationToken implements CancellationToken {
	static final long CHECK_INTERVAL_NANOS = 250_000_000L;

	private final Path stopFile;
	private final LongSupplier nanoTime;
	private long nextCheckNanos;
	private volatile boolean stopRequested;

	public StopFileCancellationToken(Path stopFile) {
		this(stopFile, System::nanoTime);
	}

	StopFileCancellationToken(Path stopFile, LongSupplier nanoTime) {
		this.stopFile = stopFile;
		this.nanoTime = nanoTime;
		nextCheckNanos = nanoTime.getAsLong();
	}

	public boolean isStopRequested() {
		return stopRequested;
	}

	@Override
	public void checkCancelled() {
		if (!stopRequested) {
			long now = nanoTime.getAsLong();
			if (now - nextCheckNanos >= 0) {
				nextCheckNanos = now + CHECK_INTERVAL_NANOS;
				stopRequested = Files.exists(stopFile);
			}
		}
		if (stopRequested) {
			throw new StopRequestedException();
		}
	}
}
