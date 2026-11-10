/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * What to generate: a metamodel with a partial model, and the limits of the generation.
 *
 * @param problem               The Refinery source of the metamodel and the partial model, or {@code null} if it is
 *                              read from {@code problemFile}.
 * @param problemFile           The file to read the problem from, or {@code null} if {@code problem} is given.
 * @param maxModels             The model cap, or {@code null} to generate every model.
 * @param scopes                Extra scope constraints, such as {@code TS=2..3}.
 * @param scopeOverrides        Scope constraints that replace the ones of the same type in the problem.
 * @param randomSeed            The random seed of the exploration.
 * @param timeoutSeconds        The time limit of the generation, or {@code null} for no limit.
 * @param loadTimeoutSeconds    The time limit of loading the problem, see {@link LoadWatchdog}.
 * @param stateCoderDepth       The neighborhood depth for telling models apart, or {@code null} for Refinery's
 *                              default.
 * @param statusIntervalSeconds How often a status line is written while generating.
 * @param includeSources        Whether the result document holds the source of each model next to its file name.
 */
public record GenerationRequest(
		@Nullable String problem,
		@Nullable Path problemFile,
		@Nullable Integer maxModels,
		List<String> scopes,
		List<String> scopeOverrides,
		long randomSeed,
		@Nullable Double timeoutSeconds,
		double loadTimeoutSeconds,
		@Nullable Integer stateCoderDepth,
		double statusIntervalSeconds,
		boolean includeSources) {
	public static final long DEFAULT_RANDOM_SEED = 1;
	// Loading takes a few seconds even on a cold JVM, so this only ends a parser that loops.
	public static final double DEFAULT_LOAD_TIMEOUT_SECONDS = 120;
	public static final double DEFAULT_STATUS_INTERVAL_SECONDS = 1;

	public static final String PROBLEM = "problem";
	public static final String MAX_MODELS = "max_models";
	public static final String SCOPES = "scopes";
	public static final String SCOPE_OVERRIDES = "scope_overrides";
	public static final String RANDOM_SEED = "random_seed";
	public static final String TIMEOUT_S = "timeout_s";
	public static final String LOAD_TIMEOUT_S = "load_timeout_s";
	public static final String STATE_CODER_DEPTH = "state_coder_depth";
	public static final String STATUS_INTERVAL_S = "status_interval_s";
	public static final String INCLUDE_SOURCES = "include_sources";

	private static final Set<String> PAYLOAD_FIELDS = Set.of(PROBLEM, MAX_MODELS, SCOPES, SCOPE_OVERRIDES,
			RANDOM_SEED, TIMEOUT_S, LOAD_TIMEOUT_S, STATE_CODER_DEPTH, STATUS_INTERVAL_S, INCLUDE_SOURCES);

	public GenerationRequest {
		if ((problem == null) == (problemFile == null)) {
			throw new InvalidRequestException("Exactly one of the problem source and the problem file must be given");
		}
		if (maxModels != null && maxModels < 1) {
			throw new InvalidRequestException("%s must be at least 1, or null for no cap, got %d"
					.formatted(MAX_MODELS, maxModels));
		}
		if (timeoutSeconds != null && !(timeoutSeconds > 0 && Double.isFinite(timeoutSeconds))) {
			throw new InvalidRequestException("%s must be positive, or null for no limit, got %s"
					.formatted(TIMEOUT_S, timeoutSeconds));
		}
		if (!(loadTimeoutSeconds > 0 && Double.isFinite(loadTimeoutSeconds))) {
			throw new InvalidRequestException("%s must be positive, got %s"
					.formatted(LOAD_TIMEOUT_S, loadTimeoutSeconds));
		}
		if (stateCoderDepth != null && stateCoderDepth < 1) {
			throw new InvalidRequestException("%s must be at least 1, got %d"
					.formatted(STATE_CODER_DEPTH, stateCoderDepth));
		}
		if (!(statusIntervalSeconds > 0 && Double.isFinite(statusIntervalSeconds))) {
			throw new InvalidRequestException("%s must be positive, got %s"
					.formatted(STATUS_INTERVAL_S, statusIntervalSeconds));
		}
		scopes = List.copyOf(scopes);
		scopeOverrides = List.copyOf(scopeOverrides);
	}

	/**
	 * Reads the payload of a SceneGEMS job. Unknown fields are rejected, so a misspelled limit can't be ignored.
	 */
	public static GenerationRequest fromPayload(JsonObject payload) {
		var unknownFields = new TreeSet<>(payload.keySet());
		unknownFields.removeAll(PAYLOAD_FIELDS);
		if (!unknownFields.isEmpty()) {
			throw new InvalidRequestException("Unknown payload fields: " + String.join(", ", unknownFields));
		}
		var problem = getString(payload, PROBLEM);
		if (problem == null) {
			throw new InvalidRequestException("The payload has no %s".formatted(PROBLEM));
		}
		var randomSeed = getInteger(payload, RANDOM_SEED);
		var loadTimeout = getNumber(payload, LOAD_TIMEOUT_S);
		var statusInterval = getNumber(payload, STATUS_INTERVAL_S);
		var includeSources = getBoolean(payload, INCLUDE_SOURCES);
		return new GenerationRequest(
				problem,
				null,
				toInt(MAX_MODELS, getInteger(payload, MAX_MODELS)),
				getStrings(payload, SCOPES),
				getStrings(payload, SCOPE_OVERRIDES),
				randomSeed == null ? DEFAULT_RANDOM_SEED : toLong(RANDOM_SEED, randomSeed),
				getNumber(payload, TIMEOUT_S),
				loadTimeout == null ? DEFAULT_LOAD_TIMEOUT_SECONDS : loadTimeout,
				toInt(STATE_CODER_DEPTH, getInteger(payload, STATE_CODER_DEPTH)),
				statusInterval == null ? DEFAULT_STATUS_INTERVAL_SECONDS : statusInterval,
				includeSources == null || includeSources);
	}

	private static @Nullable JsonElement get(JsonObject payload, String field) {
		var element = payload.get(field);
		return element == null || element.isJsonNull() ? null : element;
	}

	private static @Nullable String getString(JsonObject payload, String field) {
		var element = get(payload, field);
		if (element == null) {
			return null;
		}
		if (element instanceof JsonPrimitive primitive && primitive.isString()) {
			return primitive.getAsString();
		}
		throw new InvalidRequestException("%s must be a string".formatted(field));
	}

	private static @Nullable Boolean getBoolean(JsonObject payload, String field) {
		var element = get(payload, field);
		if (element == null) {
			return null;
		}
		if (element instanceof JsonPrimitive primitive && primitive.isBoolean()) {
			return primitive.getAsBoolean();
		}
		throw new InvalidRequestException("%s must be true or false".formatted(field));
	}

	private static @Nullable BigDecimal getDecimal(JsonObject payload, String field, String expected) {
		var element = get(payload, field);
		if (element == null) {
			return null;
		}
		if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
			return primitive.getAsBigDecimal();
		}
		throw new InvalidRequestException("%s must be %s".formatted(field, expected));
	}

	private static @Nullable Double getNumber(JsonObject payload, String field) {
		var value = getDecimal(payload, field, "a number");
		return value == null ? null : value.doubleValue();
	}

	private static @Nullable BigDecimal getInteger(JsonObject payload, String field) {
		var value = getDecimal(payload, field, "an integer");
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value.toBigIntegerExact());
		} catch (ArithmeticException e) {
			throw new InvalidRequestException("%s must be an integer, got %s".formatted(field, value), e);
		}
	}

	private static @Nullable Integer toInt(String field, @Nullable BigDecimal value) {
		if (value == null) {
			return null;
		}
		try {
			return value.intValueExact();
		} catch (ArithmeticException e) {
			throw new InvalidRequestException("%s is out of range: %s".formatted(field, value), e);
		}
	}

	private static long toLong(String field, BigDecimal value) {
		try {
			return value.longValueExact();
		} catch (ArithmeticException e) {
			throw new InvalidRequestException("%s is out of range: %s".formatted(field, value), e);
		}
	}

	private static List<String> getStrings(JsonObject payload, String field) {
		var element = get(payload, field);
		if (element == null) {
			return List.of();
		}
		if (!(element instanceof JsonArray array)) {
			throw new InvalidRequestException("%s must be a list of strings".formatted(field));
		}
		var strings = new ArrayList<String>(array.size());
		for (var item : array) {
			if (!(item instanceof JsonPrimitive primitive && primitive.isString())) {
				throw new InvalidRequestException("%s must be a list of strings".formatted(field));
			}
			strings.add(primitive.getAsString());
		}
		return strings;
	}
}
