/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import tools.refinery.scenegems.StatusReporter.Phase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StatusReporterTest {
	private static final long SECOND = 1_000_000_000L;

	private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
	private long now;

	private StatusReporter createReporter() {
		return new StatusReporter(out, 1.0, () -> now, false);
	}

	private List<JsonObject> statusLines() {
		var lines = bytes.toString(StandardCharsets.UTF_8).lines().toList();
		for (var line : lines) {
			assertThat(line, startsWith(StatusReporter.STATUS_LINE_PREFIX));
		}
		return lines.stream()
				.map(line -> JsonParser.parseString(line.substring(StatusReporter.STATUS_LINE_PREFIX.length()))
						.getAsJsonObject())
				.toList();
	}

	@Test
	void phaseLine() {
		try (var reporter = createReporter()) {
			reporter.phase(Phase.LOADING, "loading the problem");
		}
		var lines = statusLines();
		assertThat(lines, hasSize(1));
		var status = lines.getFirst();
		assertThat(status.get("message").getAsString(), is("loading the problem"));
		assertThat(status.get("progress").getAsDouble(), is(0.0));
		assertThat(status.get("phase").getAsString(), is("loading"));
		assertThat(status.get("models_found").getAsInt(), is(0));
		assertThat(status.get("max_models").isJsonNull(), is(true));
	}

	@Test
	void progressWithCap() {
		try (var reporter = createReporter()) {
			reporter.generating(4);
			now += 2 * SECOND;
			reporter.modelFound();
		}
		var status = statusLines().getLast();
		assertThat(status.get("progress").getAsDouble(), is(0.25));
		assertThat(status.get("models_found").getAsInt(), is(1));
		assertThat(status.get("max_models").getAsInt(), is(4));
		assertThat(status.get("message").getAsString(), is("generating models: 1 of at most 4 found after 2.0 s"));
	}

	@Test
	void noProgressWithoutCap() {
		try (var reporter = createReporter()) {
			reporter.generating(null);
			now += 12 * SECOND;
			reporter.modelFound();
		}
		var status = statusLines().getLast();
		assertThat(status.get("progress").isJsonNull(), is(true));
		assertThat(status.get("elapsed_s").getAsDouble(), is(12.0));
		assertThat(status.get("message").getAsString(), is("generating models: 1 found after 12 s"));
	}

	@Test
	void modelsFoundAreThrottled() {
		try (var reporter = createReporter()) {
			reporter.generating(null);
			now += SECOND / 10;
			reporter.modelFound();
			now += SECOND;
			reporter.modelFound();
		}
		var lines = statusLines();
		assertThat(lines, hasSize(2));
		assertThat(lines.getLast().get("models_found").getAsInt(), is(2));
	}

	@Test
	void heartbeatOnlyWhileGenerating() {
		try (var reporter = createReporter()) {
			reporter.phase(Phase.INITIALIZING, "initializing the generator");
			now += SECOND;
			reporter.tick();
			reporter.generating(null);
			now += SECOND;
			reporter.tick();
			now += SECOND / 10;
			reporter.tick();
		}
		var lines = statusLines();
		assertThat(lines.stream().map(line -> line.get("phase").getAsString()).toList(),
				is(List.of("initializing", "generating", "generating")));
	}

	@Test
	void finished() {
		var result = new GenerationResult(StopReason.CAP_REACHED, 2, 1, 3.25, null, List.of(
				new GeneratedModel(0, "models/model_00000.problem", null),
				new GeneratedModel(1, "models/model_00001.problem", null)));
		try (var reporter = createReporter()) {
			reporter.generating(2);
			reporter.finished(result);
		}
		var status = statusLines().getLast();
		assertThat(status.get("phase").getAsString(), is("finished"));
		assertThat(status.get("progress").getAsDouble(), is(1.0));
		assertThat(status.get("models_found").getAsInt(), is(2));
		assertThat(status.get("message").getAsString(), is("finished: 2 models, model cap reached, after 3.3 s"));
	}

	@Test
	void failed() {
		try (var reporter = createReporter()) {
			reporter.failed("invalid job: no problem");
		}
		var status = statusLines().getLast();
		assertThat(status.get("phase").getAsString(), is("failed"));
		assertThat(status.get("progress").isJsonNull(), is(true));
		assertThat(status.get("message").getAsString(), is("failed: invalid job: no problem"));
	}
}
