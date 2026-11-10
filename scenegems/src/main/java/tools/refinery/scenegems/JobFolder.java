/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The documents of the SceneGEMS per-job protocol in a job folder.
 * <ul>
 *     <li>{@value #JOB_DOCUMENT}: {@code {"job": JobSpec}}, copied in by the launcher before the start. The
 *     generation request is the job's {@code payload}.</li>
 *     <li>{@value #RESULT_DOCUMENT}: {@code {"result": {...}}} or {@code {"error": "...", "retryable": false}},
 *     copied out by the launcher after the stop.</li>
 *     <li>{@value #STOP_DOCUMENT}: copied in to stop the generation early; its content is ignored.</li>
 *     <li>{@value ModelEnumerator#MODELS_FOLDER}/: one {@code .problem} file per generated model.</li>
 * </ul>
 */
public final class JobFolder {
	public static final String DEFAULT_JOB_FOLDER = "/job";
	public static final String JOB_DOCUMENT = "job.json";
	public static final String RESULT_DOCUMENT = "result.json";
	public static final String STOP_DOCUMENT = "stop";

	private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

	private JobFolder() {
		throw new IllegalStateException("This is a static utility class and should not be instantiated directly");
	}

	/**
	 * Reads the generation request from the payload of the job document.
	 */
	public static GenerationRequest readRequest(Path folder) throws IOException {
		var path = folder.resolve(JOB_DOCUMENT);
		JsonObject document;
		try {
			document = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (JsonParseException | IllegalStateException e) {
			throw new InvalidRequestException("%s is not a JSON object: %s".formatted(JOB_DOCUMENT, e.getMessage()),
					e);
		}
		if (!(document.get("job") instanceof JsonObject job)) {
			throw new InvalidRequestException("%s has no job object".formatted(JOB_DOCUMENT));
		}
		if (!(job.get("payload") instanceof JsonObject payload)) {
			throw new InvalidRequestException("The job in %s has no payload object".formatted(JOB_DOCUMENT));
		}
		return GenerationRequest.fromPayload(payload);
	}

	public static void writeResult(Path folder, GenerationResult result) throws IOException {
		var outcome = new JsonObject();
		outcome.add("result", result.toJson());
		writeOutcome(folder, outcome);
	}

	public static void writeError(Path folder, String error, boolean retryable) throws IOException {
		var outcome = new JsonObject();
		outcome.addProperty("error", error);
		outcome.addProperty("retryable", retryable);
		writeOutcome(folder, outcome);
	}

	private static void writeOutcome(Path folder, JsonObject outcome) throws IOException {
		Files.createDirectories(folder);
		// Move a complete document into place, so a reader never sees half of it.
		var temporary = folder.resolve(RESULT_DOCUMENT + ".tmp");
		Files.writeString(temporary, GSON.toJson(outcome), StandardCharsets.UTF_8);
		Files.move(temporary, folder.resolve(RESULT_DOCUMENT), StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
	}
}
