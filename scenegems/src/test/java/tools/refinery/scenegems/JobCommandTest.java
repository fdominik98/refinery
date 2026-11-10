/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Timeout(value = 2, unit = TimeUnit.MINUTES)
class JobCommandTest {
	@TempDir
	private Path folder;

	private void writeJob(JsonObject payload) throws IOException {
		var job = new JsonObject();
		job.addProperty("job_id", "job-1");
		job.addProperty("job_type", "model_generation");
		job.add("payload", payload);
		job.addProperty("attempt", 1);
		job.addProperty("idempotency_key", "job-1/1");
		var document = new JsonObject();
		document.add("job", job);
		Files.writeString(folder.resolve(JobFolder.JOB_DOCUMENT), document.toString(), StandardCharsets.UTF_8);
	}

	private int runJob() {
		return ModelGenerationMain.run(new String[]{"job", "-folder", folder.toString()});
	}

	private JsonObject readOutcome() throws IOException {
		var text = Files.readString(folder.resolve(JobFolder.RESULT_DOCUMENT), StandardCharsets.UTF_8);
		return JsonParser.parseString(text).getAsJsonObject();
	}

	@Test
	void jobWritesResult() throws IOException {
		var payload = new JsonObject();
		payload.addProperty("problem", ColregsProblems.partialModel(1, 0));
		writeJob(payload);

		assertThat(runJob(), is(ModelGenerationMain.EXIT_SUCCESS));

		var result = readOutcome().getAsJsonObject("result");
		assertThat(result.get("stop_reason").getAsString(), is("exhausted"));
		assertThat(result.get("complete").getAsBoolean(), is(true));
		assertThat(result.get("models_found").getAsInt(), is(7));
		assertThat(result.get("max_models").isJsonNull(), is(true));
		assertThat(result.get("message").isJsonNull(), is(true));
		var models = result.getAsJsonArray("models");
		assertThat(models.size(), is(7));
		var first = models.get(0).getAsJsonObject();
		assertThat(first.get("index").getAsInt(), is(0));
		assertThat(first.get("file").getAsString(), is("models/model_00000.problem"));
		assertThat(Files.readString(folder.resolve(first.get("file").getAsString()), StandardCharsets.UTF_8),
				is(first.get("source").getAsString()));
	}

	@Test
	void jobWithCap() throws IOException {
		var payload = new JsonObject();
		payload.addProperty("problem", ColregsProblems.partialModel(2, 0));
		payload.addProperty("max_models", 3);
		payload.addProperty("include_sources", false);
		writeJob(payload);

		assertThat(runJob(), is(ModelGenerationMain.EXIT_SUCCESS));

		var result = readOutcome().getAsJsonObject("result");
		assertThat(result.get("stop_reason").getAsString(), is("cap_reached"));
		assertThat(result.get("complete").getAsBoolean(), is(false));
		assertThat(result.get("models_found").getAsInt(), is(3));
		assertThat(result.getAsJsonArray("models").get(0).getAsJsonObject().has("source"), is(false));
	}

	@Test
	void invalidPayloadWritesError() throws IOException {
		var payload = new JsonObject();
		payload.addProperty("max_models", 3);
		writeJob(payload);

		assertThat(runJob(), is(ModelGenerationMain.EXIT_FAILURE));

		var outcome = readOutcome();
		assertThat(outcome.has("result"), is(false));
		assertThat(outcome.get("error").getAsString(), startsWith("invalid job: "));
		assertThat(outcome.get("retryable").getAsBoolean(), is(false));
	}

	@Test
	void invalidProblemWritesError() throws IOException {
		var payload = new JsonObject();
		payload.addProperty("problem", "class A {\n    B[1] b\n}\n");
		writeJob(payload);

		assertThat(runJob(), is(ModelGenerationMain.EXIT_FAILURE));

		var outcome = readOutcome();
		assertThat(outcome.get("error").getAsString(), not(emptyString()));
		assertThat(outcome.get("retryable").getAsBoolean(), is(false));
	}

	@Test
	void missingJobWritesError() throws IOException {
		assertThat(runJob(), is(ModelGenerationMain.EXIT_FAILURE));
		assertThat(readOutcome().get("error").getAsString(), startsWith("invalid job: "));
	}

	@Test
	void stopFileBeforeStart() throws IOException {
		var payload = new JsonObject();
		payload.addProperty("problem", ColregsProblems.partialModel(1, 0));
		writeJob(payload);
		Files.writeString(folder.resolve(JobFolder.STOP_DOCUMENT), "");

		assertThat(runJob(), is(ModelGenerationMain.EXIT_SUCCESS));

		var result = readOutcome().getAsJsonObject("result");
		assertThat(result.get("stop_reason").getAsString(), is("stopped"));
		assertThat(result.get("models_found").getAsInt(), is(0));
	}
}
