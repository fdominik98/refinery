/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.generator.ModelFacadeResult;
import tools.refinery.generator.ModelGenerator;
import tools.refinery.generator.ModelGeneratorFactory;
import tools.refinery.generator.ProblemLoader;
import tools.refinery.language.model.problem.Problem;
import tools.refinery.scenegems.StatusReporter.Phase;
import tools.refinery.store.dse.transition.VersionWithObjectiveValue;
import tools.refinery.store.dse.transition.statespace.SolutionStoreListener;
import tools.refinery.store.dse.transition.statespace.internal.SolutionStoreImpl;
import tools.refinery.store.util.CancellationToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Generates the models of a problem and writes each one to a file as soon as it is found.
 * <p>
 * Writing the models while the generator runs keeps them when the generation times out or is stopped: the generator
 * itself forgets its solutions in that case. Without a model cap the exploration runs until no unexplored state is
 * left, so the models are all the models of the problem up to isomorphism. Isomorphism is decided by a hash of the
 * neighborhoods of the nodes, see {@link ModelGeneratorFactory#stateCoderDepth(int)}.
 */
public class ModelEnumerator {
	public static final String MODELS_FOLDER = "models";

	private static final Logger LOG = LoggerFactory.getLogger(ModelEnumerator.class);

	@Inject
	private ProblemLoader problemLoader;

	@Inject
	private Provider<ModelGeneratorFactory> generatorFactoryProvider;

	/**
	 * Loads the problem of the request and adds the scopes of the request to it.
	 *
	 * @throws tools.refinery.generator.InvalidProblemException         If the problem doesn't parse or validate.
	 * @throws tools.refinery.generator.InvalidScopeConstraintException If a scope of the request doesn't parse.
	 */
	public Problem load(GenerationRequest request, StatusReporter status) throws IOException {
		status.phase(Phase.LOADING, "loading the problem");
		var problemFile = request.problemFile();
		var problem = problemFile == null ? problemLoader.loadString(request.problem()) :
				problemLoader.loadFile(problemFile.toFile());
		return problemLoader.loadScopeConstraints(problem, request.scopes(), request.scopeOverrides());
	}

	/**
	 * Loads the problem of the request and generates its models into
	 * {@code outputFolder}/{@value #MODELS_FOLDER}.
	 *
	 * @see #load(GenerationRequest, StatusReporter)
	 */
	public GenerationResult enumerate(GenerationRequest request, Path outputFolder, StatusReporter status,
									  CancellationToken cancellationToken) throws IOException {
		long startNanos = System.nanoTime();
		var problem = load(request, status);
		return enumerate(request, problem, outputFolder, status, cancellationToken, startNanos);
	}

	/**
	 * Generates the models of a loaded problem into {@code outputFolder}/{@value #MODELS_FOLDER}.
	 *
	 * @param startNanos When the work on the request started, by {@link System#nanoTime()}, for the elapsed time of
	 *                   the result.
	 */
	public GenerationResult enumerate(GenerationRequest request, Problem problem, Path outputFolder,
									  StatusReporter status, CancellationToken cancellationToken, long startNanos)
			throws IOException {
		var sink = new ModelSink(outputFolder, request.includeSources(), status);
		StopReason stopReason;
		String message = null;
		try {
			status.phase(Phase.INITIALIZING, "initializing the generator");
			var factory = generatorFactoryProvider.get()
					.cancellationToken(cancellationToken)
					// Tell models apart by what they show, not by the solver's internal state, like the CLI does
					// when it generates more than one model.
					.partialInterpretationBasedNeighborhoods(true);
			if (request.stateCoderDepth() != null) {
				factory.stateCoderDepth(request.stateCoderDepth());
			}
			try (var generator = factory.tryCreateGenerator(problem)) {
				if (generator.getInitializationResult() instanceof ModelFacadeResult.Rejected rejected) {
					stopReason = StopReason.UNSATISFIABLE;
					message = rejected.formatMessage();
				} else {
					stopReason = generate(request, generator, sink, status);
				}
			}
		} catch (StopRequestedException e) {
			stopReason = StopReason.STOPPED;
		}
		double elapsedSeconds = (System.nanoTime() - startNanos) / 1e9;
		return new GenerationResult(stopReason, request.maxModels(), request.randomSeed(), elapsedSeconds, message,
				sink.getModels());
	}

	private static StopReason generate(GenerationRequest request, ModelGenerator generator, ModelSink sink,
									   StatusReporter status) throws IOException {
		var maxModels = request.maxModels();
		generator.setRandomSeed(request.randomSeed());
		generator.setMaxNumberOfSolutions(maxModels == null ? SolutionStoreImpl.UNLIMITED : maxModels);
		sink.prepare(generator);
		generator.addListener(sink);
		status.generating(maxModels);
		var timeoutSeconds = request.timeoutSeconds();
		var result = timeoutSeconds == null ? generator.tryGenerate() :
				generator.tryGenerateWithTimeout(Math.round(timeoutSeconds * 1e9), TimeUnit.NANOSECONDS);
		return switch (result) {
			case REQUEST_FULFILLED -> StopReason.CAP_REACHED;
			case NO_MORE_SOLUTIONS -> StopReason.EXHAUSTED;
			case UNSATISFIABLE -> StopReason.UNSATISFIABLE;
			case TIMEOUT -> StopReason.TIMEOUT;
		};
	}

	private static final class ModelSink implements SolutionStoreListener {
		private final Path outputFolder;
		private final Path modelsFolder;
		private final boolean includeSources;
		private final StatusReporter status;
		private final Map<VersionWithObjectiveValue, GeneratedModel> models = new LinkedHashMap<>();
		private ModelGenerator generator;
		private int nextIndex;

		private ModelSink(Path outputFolder, boolean includeSources, StatusReporter status) {
			this.outputFolder = outputFolder;
			modelsFolder = outputFolder.resolve(MODELS_FOLDER);
			this.includeSources = includeSources;
			this.status = status;
		}

		public void prepare(ModelGenerator generator) throws IOException {
			this.generator = generator;
			Files.createDirectories(modelsFolder);
		}

		public List<GeneratedModel> getModels() {
			return List.copyOf(models.values());
		}

		@Override
		public void solutionAdded(VersionWithObjectiveValue version) {
			// The exploration runs on a model of its own, so restoring the generator's model doesn't disturb it.
			// The web API saves its models the same way.
			generator.getModel().restore(version.version());
			int index = nextIndex;
			nextIndex++;
			var fileName = "model_%05d.problem".formatted(index);
			String source;
			try {
				source = serialize(generator);
				Files.writeString(modelsFolder.resolve(fileName), source, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to save model " + index, e);
			}
			models.put(version, new GeneratedModel(index, MODELS_FOLDER + "/" + fileName,
					includeSources ? source : null));
			status.modelFound();
		}

		@Override
		public void solutionRemoved(VersionWithObjectiveValue version) {
			// The solution store only drops a solution for a better one when it is full, which shouldn't happen,
			// since the exploration stops when the store is full. Mirror the store anyway.
			var model = models.remove(version);
			if (model == null) {
				return;
			}
			LOG.warn("The generator dropped model {}", model.index());
			try {
				Files.deleteIfExists(outputFolder.resolve(model.file()));
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to delete model " + model.index(), e);
			}
			status.modelRemoved();
		}

		private static String serialize(ModelGenerator generator) throws IOException {
			var resource = generator.serialize().eResource();
			try (var outputStream = new ByteArrayOutputStream()) {
				resource.save(outputStream, Map.of());
				return outputStream.toString(StandardCharsets.UTF_8);
			}
		}
	}
}
