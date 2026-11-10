/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.refinery.generator.InvalidProblemException;
import tools.refinery.generator.ProblemLoader;
import tools.refinery.generator.standalone.StandaloneRefinery;
import tools.refinery.store.util.CancellationToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

// A generation that never ends fails its own test instead of filling the heap of the test worker.
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class ModelEnumeratorTest {
	@TempDir
	private Path folder;

	private final ModelEnumerator enumerator = StandaloneRefinery.getInstance(ModelEnumerator.class);

	private static GenerationRequest request(String problem, Integer maxModels) {
		return request(problem, maxModels, null, List.of(), true);
	}

	private static GenerationRequest request(String problem, Integer maxModels, Double timeoutSeconds,
											 List<String> scopeOverrides, boolean includeSources) {
		return new GenerationRequest(problem, null, maxModels, List.of(), scopeOverrides,
				GenerationRequest.DEFAULT_RANDOM_SEED, timeoutSeconds, GenerationRequest.DEFAULT_LOAD_TIMEOUT_SECONDS,
				null, 1, includeSources);
	}

	private GenerationResult enumerate(GenerationRequest request) throws IOException {
		return enumerate(request, CancellationToken.NONE);
	}

	private GenerationResult enumerate(GenerationRequest request, CancellationToken cancellationToken)
			throws IOException {
		var out = new PrintStream(OutputStream.nullOutputStream());
		try (var status = new StatusReporter(out, 1, System::nanoTime, false)) {
			return enumerator.enumerate(request, folder, status, cancellationToken);
		}
	}

	@ParameterizedTest(name = "{0} target ships and {1} obstacles have {2} models")
	@CsvSource({
			"1, 0, 7",
			"1, 1, 7",
			"2, 0, 28",
	})
	void allModelsOfColregsScenarios(int targetShips, int obstacles, int expectedModels) throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(targetShips, obstacles), null));
		assertThat(result.stopReason(), is(StopReason.EXHAUSTED));
		assertThat(result.complete(), is(true));
		assertThat(result.modelCount(), is(expectedModels));
		for (var model : result.models()) {
			assertThat(Files.readString(folder.resolve(model.file()), StandardCharsets.UTF_8), is(model.source()));
		}
	}

	@Test
	void modelsAreNumberedInOrder() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(1, 0), null));
		for (int i = 0; i < result.modelCount(); i++) {
			var model = result.models().get(i);
			assertThat(model.index(), is(i));
			assertThat(model.file(), is("models/model_%05d.problem".formatted(i)));
		}
	}

	@Test
	void generatedModelsLoadBack() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(1, 0), null));
		var loader = StandaloneRefinery.getInstance(ProblemLoader.class);
		var sources = result.models().stream().map(GeneratedModel::source).toList();
		for (var source : sources) {
			loader.loadString(source);
		}
		assertThat(sources.stream().distinct().count(), is((long) sources.size()));
	}

	@Test
	void modelCap() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(2, 0), 5));
		assertThat(result.stopReason(), is(StopReason.CAP_REACHED));
		assertThat(result.complete(), is(false));
		assertThat(result.modelCount(), is(5));
		assertThat(result.maxModels(), is(5));
		try (var files = Files.list(folder.resolve(ModelEnumerator.MODELS_FOLDER))) {
			assertThat(files.count(), is(5L));
		}
	}

	@Test
	void capAboveModelCount() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(1, 0), 100));
		assertThat(result.stopReason(), is(StopReason.EXHAUSTED));
		assertThat(result.modelCount(), is(7));
	}

	@Test
	void scopeOverride() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(1, 0), null, null, List.of("TS=2"), true));
		assertThat(result.stopReason(), is(StopReason.EXHAUSTED));
		assertThat(result.modelCount(), is(28));
	}

	@Test
	void sourcesCanBeLeftOut() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(1, 0), null, null, List.of(), false));
		assertThat(result.modelCount(), is(7));
		for (var model : result.models()) {
			assertThat(model.source(), nullValue());
			assertThat(Files.exists(folder.resolve(model.file())), is(true));
		}
	}

	@Test
	void unsatisfiable() throws IOException {
		var result = enumerate(request("""
				class A.
				error noA() <-> !A(_).
				scope A = 0.
				""", null));
		assertThat(result.stopReason(), is(StopReason.UNSATISFIABLE));
		assertThat(result.complete(), is(true));
		assertThat(result.modelCount(), is(0));
	}

	@Test
	void timeout() throws IOException {
		var result = enumerate(request(ColregsProblems.partialModel(3, 0), null, 0.001, List.of(), true));
		assertThat(result.stopReason(), is(StopReason.TIMEOUT));
		assertThat(result.complete(), is(false));
		assertThat(result.modelCount(), lessThan(84));
	}

	@Test
	void stopKeepsModelsFound() throws IOException {
		var calls = new int[1];
		CancellationToken stopAfterAWhile = () -> {
			calls[0]++;
			if (calls[0] > 2000) {
				throw new StopRequestedException();
			}
		};
		var result = enumerate(request(ColregsProblems.partialModel(3, 0), null), stopAfterAWhile);
		assertThat(result.stopReason(), is(StopReason.STOPPED));
		assertThat(result.complete(), is(false));
		for (var model : result.models()) {
			assertThat(Files.exists(folder.resolve(model.file())), is(true));
		}
	}

	@Test
	void invalidProblem() {
		// Not a syntax error at the end of the file: Refinery's parser may loop on those, see LoadWatchdog.
		var request = request("""
				class A {
				    B[1] b
				}
				""", null);
		assertThrows(InvalidProblemException.class, () -> enumerate(request));
	}
}
