/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The outcome of a generation that ran: the models it found and why it stopped.
 *
 * @param stopReason     Why the generation stopped.
 * @param maxModels      The model cap of the request, or {@code null} if there was none.
 * @param randomSeed     The random seed of the exploration.
 * @param elapsedSeconds The time from loading the problem to the end of the generation.
 * @param message        A detail about the stop reason, such as why the problem is unsatisfiable, or {@code null}.
 * @param models         The models in the order they were found.
 */
public record GenerationResult(
		StopReason stopReason,
		@Nullable Integer maxModels,
		long randomSeed,
		double elapsedSeconds,
		@Nullable String message,
		List<GeneratedModel> models) {
	public GenerationResult {
		models = List.copyOf(models);
	}

	public int modelCount() {
		return models.size();
	}

	/**
	 * Whether the models are all the models of the problem, up to the isomorphism check of the generator.
	 */
	public boolean complete() {
		return stopReason.isComplete();
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("stop_reason", stopReason.getId());
		json.addProperty("complete", complete());
		json.addProperty("models_found", modelCount());
		// Gson stores a null value as JsonNull, which a writer with serializeNulls keeps.
		json.addProperty("max_models", maxModels);
		json.addProperty("random_seed", randomSeed);
		json.addProperty("elapsed_s", Math.round(elapsedSeconds * 1000) / 1000.0);
		json.addProperty("message", message);
		var modelsJson = new JsonArray(models.size());
		for (var model : models) {
			var modelJson = new JsonObject();
			modelJson.addProperty("index", model.index());
			modelJson.addProperty("file", model.file());
			if (model.source() != null) {
				modelJson.addProperty("source", model.source());
			}
			modelsJson.add(modelJson);
		}
		json.add("models", modelsJson);
		return json;
	}
}
