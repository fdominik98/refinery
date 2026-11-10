/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.generator.InvalidProblemException;
import tools.refinery.generator.InvalidScopeConstraintException;
import tools.refinery.language.model.problem.Problem;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Runs a generation request into an output folder and writes its outcome there, the same way for the
 * {@code job} and the {@code generate} commands.
 */
public class GenerationRunner {
	private static final Logger LOG = LoggerFactory.getLogger(GenerationRunner.class);

	@Inject
	private ModelEnumerator enumerator;

	public int run(GenerationRequest request, Path outputFolder, PrintStream statusOutput) {
		try (var status = StatusReporter.start(statusOutput, request.statusIntervalSeconds())) {
			long startNanos = System.nanoTime();
			GenerationResult result;
			try {
				Problem problem;
				try (var ignored = new LoadWatchdog(request.loadTimeoutSeconds(),
						() -> loadTimedOut(outputFolder, status, request.loadTimeoutSeconds()))) {
					problem = enumerator.load(request, status);
				}
				var stopToken = new StopFileCancellationToken(outputFolder.resolve(JobFolder.STOP_DOCUMENT));
				result = enumerator.enumerate(request, problem, outputFolder, status, stopToken, startNanos);
			} catch (InvalidProblemException | InvalidScopeConstraintException | InvalidRequestException e) {
				// Another attempt with the same input fails the same way.
				return fail(outputFolder, status, e.getMessage());
			} catch (IOException | RuntimeException | OutOfMemoryError e) {
				LOG.error("Model generation failed", e);
				return fail(outputFolder, status, "%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
			}
			try {
				JobFolder.writeResult(outputFolder, result);
			} catch (IOException e) {
				LOG.error("Failed to write the result", e);
				status.failed("could not write %s: %s".formatted(JobFolder.RESULT_DOCUMENT, e.getMessage()));
				return ModelGenerationMain.EXIT_FAILURE;
			}
			status.finished(result);
			return ModelGenerationMain.EXIT_SUCCESS;
		}
	}

	private static void loadTimedOut(Path outputFolder, StatusReporter status, double loadTimeoutSeconds) {
		fail(outputFolder, status, ("loading the problem did not finish in %s s; Refinery's parser can loop on a " +
				"block that the end of the file cuts off, such as a class body without its closing brace")
				.formatted(loadTimeoutSeconds));
		// The parser doesn't react to interrupts, and shutdown hooks could wait for it, so stop the JVM at once.
		Runtime.getRuntime().halt(ModelGenerationMain.EXIT_FAILURE);
	}

	public static int fail(Path outputFolder, StatusReporter status, String error) {
		try {
			JobFolder.writeError(outputFolder, error, false);
		} catch (IOException e) {
			LOG.error("Failed to write the error", e);
		}
		status.failed(error);
		return ModelGenerationMain.EXIT_FAILURE;
	}
}
