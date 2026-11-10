/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerationRequestTest {
	private static GenerationRequest parse(String json) {
		return GenerationRequest.fromPayload(JsonParser.parseString(json).getAsJsonObject());
	}

	@Test
	void fullPayload() {
		var request = parse("""
				{
				  "problem": "class A.",
				  "max_models": 100,
				  "scopes": ["A=2..3"],
				  "scope_overrides": ["A=4"],
				  "random_seed": 42,
				  "timeout_s": 2.5,
				  "load_timeout_s": 30,
				  "state_coder_depth": 9,
				  "status_interval_s": 0.5,
				  "include_sources": false
				}
				""");
		assertThat(request.problem(), is("class A."));
		assertThat(request.problemFile(), nullValue());
		assertThat(request.maxModels(), is(100));
		assertThat(request.scopes(), is(List.of("A=2..3")));
		assertThat(request.scopeOverrides(), is(List.of("A=4")));
		assertThat(request.randomSeed(), is(42L));
		assertThat(request.timeoutSeconds(), is(2.5));
		assertThat(request.loadTimeoutSeconds(), is(30.0));
		assertThat(request.stateCoderDepth(), is(9));
		assertThat(request.statusIntervalSeconds(), is(0.5));
		assertThat(request.includeSources(), is(false));
	}

	@Test
	void defaults() {
		var request = parse("""
				{"problem": "class A."}
				""");
		assertThat(request.maxModels(), nullValue());
		assertThat(request.scopes(), empty());
		assertThat(request.scopeOverrides(), empty());
		assertThat(request.randomSeed(), is(GenerationRequest.DEFAULT_RANDOM_SEED));
		assertThat(request.timeoutSeconds(), nullValue());
		assertThat(request.loadTimeoutSeconds(), is(GenerationRequest.DEFAULT_LOAD_TIMEOUT_SECONDS));
		assertThat(request.stateCoderDepth(), nullValue());
		assertThat(request.statusIntervalSeconds(), is(GenerationRequest.DEFAULT_STATUS_INTERVAL_SECONDS));
		assertThat(request.includeSources(), is(true));
	}

	@Test
	void nullMaxModelsMeansNoCap() {
		var request = parse("""
				{"problem": "class A.", "max_models": null, "timeout_s": null}
				""");
		assertThat(request.maxModels(), nullValue());
		assertThat(request.timeoutSeconds(), nullValue());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{}",
			"{\"problem\": 1}",
			"{\"problem\": \"class A.\", \"max_models\": 0}",
			"{\"problem\": \"class A.\", \"max_models\": -1}",
			"{\"problem\": \"class A.\", \"max_models\": 1.5}",
			"{\"problem\": \"class A.\", \"max_models\": \"10\"}",
			"{\"problem\": \"class A.\", \"max_models\": 1e20}",
			"{\"problem\": \"class A.\", \"scopes\": \"A=2\"}",
			"{\"problem\": \"class A.\", \"scopes\": [2]}",
			"{\"problem\": \"class A.\", \"random_seed\": 1e30}",
			"{\"problem\": \"class A.\", \"timeout_s\": 0}",
			"{\"problem\": \"class A.\", \"load_timeout_s\": 0}",
			"{\"problem\": \"class A.\", \"state_coder_depth\": 0}",
			"{\"problem\": \"class A.\", \"status_interval_s\": 0}",
			"{\"problem\": \"class A.\", \"include_sources\": \"yes\"}",
			"{\"problem\": \"class A.\", \"max_model\": 10}",
	})
	void invalidPayload(String json) {
		assertThrows(InvalidRequestException.class, () -> parse(json));
	}
}
