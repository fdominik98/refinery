/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Writes the status lines of the SceneGEMS per-job protocol to the standard output.
 * <p>
 * A status line is {@link #STATUS_LINE_PREFIX} followed by a JSON object on one line. SceneGEMS reads its
 * {@code message} and {@code progress} (between 0 and 1, or {@code null} if unknown) and treats every other line as a
 * log line. The other fields are for consumers that want the counts without parsing the message. Refinery logs to the
 * standard error, so log lines never interleave with status lines.
 * <p>
 * While generating, a heartbeat writes a status line at the requested interval even if no model was found, so a
 * long exploration still shows its elapsed time.
 */
public class StatusReporter implements AutoCloseable {
	public static final String STATUS_LINE_PREFIX = "scenegems-status ";

	public enum Phase {
		LOADING("loading"),
		INITIALIZING("initializing"),
		GENERATING("generating"),
		FINISHED("finished"),
		FAILED("failed");

		private final String id;

		Phase(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	private static final long NANOS_PER_SECOND = 1_000_000_000L;

	private final PrintStream out;
	private final LongSupplier nanoTime;
	private final long intervalNanos;
	private final long startNanos;
	private final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
	private final @Nullable ScheduledExecutorService heartbeat;
	private Phase phase = Phase.LOADING;
	private String message = "starting";
	private int modelsFound;
	private @Nullable Integer maxModels;
	private long lastEmitNanos;

	StatusReporter(PrintStream out, double intervalSeconds, LongSupplier nanoTime, boolean withHeartbeat) {
		this.out = out;
		this.nanoTime = nanoTime;
		intervalNanos = Math.max(1, Math.round(intervalSeconds * NANOS_PER_SECOND));
		startNanos = nanoTime.getAsLong();
		lastEmitNanos = startNanos;
		if (withHeartbeat) {
			heartbeat = Executors.newSingleThreadScheduledExecutor(runnable -> {
				var thread = new Thread(runnable, "scenegems-status-heartbeat");
				thread.setDaemon(true);
				return thread;
			});
			heartbeat.scheduleAtFixedRate(this::tick, intervalNanos, intervalNanos, TimeUnit.NANOSECONDS);
		} else {
			heartbeat = null;
		}
	}

	public static StatusReporter start(PrintStream out, double intervalSeconds) {
		return new StatusReporter(out, intervalSeconds, System::nanoTime, true);
	}

	public synchronized void phase(Phase phase, String message) {
		this.phase = phase;
		this.message = message;
		emit();
	}

	public synchronized void generating(@Nullable Integer maxModels) {
		this.maxModels = maxModels;
		phase = Phase.GENERATING;
		emit();
	}

	public synchronized void modelFound() {
		modelsFound++;
		if (nanoTime.getAsLong() - lastEmitNanos >= intervalNanos) {
			emit();
		}
	}

	public synchronized void modelRemoved() {
		modelsFound--;
	}

	public synchronized void finished(GenerationResult result) {
		modelsFound = result.modelCount();
		maxModels = result.maxModels();
		phase = Phase.FINISHED;
		message = "finished: %d %s, %s, after %s".formatted(modelsFound, modelsFound == 1 ? "model" : "models",
				result.stopReason().getDescription(), formatSeconds(result.elapsedSeconds()));
		emit();
	}

	public synchronized void failed(String error) {
		phase = Phase.FAILED;
		message = "failed: " + error;
		emit();
	}

	synchronized void tick() {
		// Half an interval of slack, so a model found just before a tick doesn't make the heartbeat skip a beat.
		if (phase == Phase.GENERATING && nanoTime.getAsLong() - lastEmitNanos >= intervalNanos / 2) {
			emit();
		}
	}

	private void emit() {
		long now = nanoTime.getAsLong();
		lastEmitNanos = now;
		double elapsedSeconds = (double) (now - startNanos) / NANOS_PER_SECOND;
		var status = new JsonObject();
		status.addProperty("message", phase == Phase.GENERATING ? generatingMessage(elapsedSeconds) : message);
		status.addProperty("progress", getProgress());
		status.addProperty("phase", phase.getId());
		status.addProperty("models_found", modelsFound);
		status.addProperty("max_models", maxModels);
		status.addProperty("elapsed_s", Math.round(elapsedSeconds * 1000) / 1000.0);
		out.println(STATUS_LINE_PREFIX + gson.toJson(status));
		out.flush();
	}

	private String generatingMessage(double elapsedSeconds) {
		if (maxModels == null) {
			return "generating models: %d found after %s".formatted(modelsFound, formatSeconds(elapsedSeconds));
		}
		return "generating models: %d of at most %d found after %s".formatted(modelsFound, maxModels,
				formatSeconds(elapsedSeconds));
	}

	private @Nullable Double getProgress() {
		return switch (phase) {
			case LOADING, INITIALIZING -> 0.0;
			case GENERATING -> maxModels == null ? null : Math.min(1.0, (double) modelsFound / maxModels);
			case FINISHED -> 1.0;
			case FAILED -> null;
		};
	}

	private static String formatSeconds(double seconds) {
		// The root locale keeps the decimal point whatever the locale of the container is.
		return String.format(Locale.ROOT, seconds < 10 ? "%.1f s" : "%.0f s", seconds);
	}

	@Override
	public void close() {
		if (heartbeat != null) {
			heartbeat.shutdownNow();
		}
	}
}
