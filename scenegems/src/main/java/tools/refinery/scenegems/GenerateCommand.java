/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Generate the models of a problem into a folder")
public class GenerateCommand {
	private static final Logger LOG = LoggerFactory.getLogger(GenerateCommand.class);

	@Inject
	private GenerationRunner runner;

	@Parameter(description = "input path", required = true)
	private String inputPath;

	@Parameter(names = {"-output", "-o"}, description = "Output folder for models/ and result.json",
			required = true, placeholder = "PATH")
	private String outputPath;

	@Parameter(names = {"-max-models", "-n"}, description = "Model cap; omit to generate every model",
			placeholder = "COUNT")
	private Integer maxModels;

	@Parameter(names = {"-scope", "-s"}, description = "Extra scope constraints",
			placeholder = "Type1=range,Type2=range,...")
	private List<String> scopes = new ArrayList<>();

	@Parameter(names = {"-scope-override", "-S"}, description = "Override scope constraints",
			placeholder = "Type1=range,Type2=range,...")
	private List<String> scopeOverrides = new ArrayList<>();

	@Parameter(names = {"-random-seed", "-r"}, description = "Random seed", placeholder = "SEED")
	private long randomSeed = GenerationRequest.DEFAULT_RANDOM_SEED;

	@Parameter(names = {"-timeout", "-t"}, description = "Time limit in seconds", placeholder = "SECONDS")
	private Double timeoutSeconds;

	@Parameter(names = "-load-timeout", description = "Time limit of loading the problem in seconds",
			placeholder = "SECONDS")
	private double loadTimeoutSeconds = GenerationRequest.DEFAULT_LOAD_TIMEOUT_SECONDS;

	@Parameter(names = "-state-coder-depth", description = "Neighborhood depth for telling models apart",
			placeholder = "DEPTH")
	private Integer stateCoderDepth;

	@Parameter(names = "-status-interval", description = "Seconds between status lines", placeholder = "SECONDS")
	private double statusIntervalSeconds = GenerationRequest.DEFAULT_STATUS_INTERVAL_SECONDS;

	@Parameter(names = "-no-sources", description = "Leave the model sources out of result.json")
	private boolean noSources;

	public int run() {
		GenerationRequest request;
		try {
			request = new GenerationRequest(null, Path.of(inputPath), maxModels, scopes, scopeOverrides, randomSeed,
					timeoutSeconds, loadTimeoutSeconds, stateCoderDepth, statusIntervalSeconds, !noSources);
		} catch (InvalidRequestException e) {
			LOG.error("Invalid arguments: {}", e.getMessage());
			return ModelGenerationMain.EXIT_USAGE;
		}
		return runner.run(request, Path.of(outputPath), System.out);
	}
}
