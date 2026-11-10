/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.generator.standalone.StandaloneRefinery;

/**
 * Entry point of the SceneGEMS model generation interface.
 * <ul>
 *     <li>{@code job [--folder /job]} runs a SceneGEMS per-job container: it reads {@code job.json} from the job
 *     folder, writes status lines to the standard output and the models and {@code result.json} to the job
 *     folder.</li>
 *     <li>{@code generate <input> --output <folder> ...} runs the same generation from the command line.</li>
 * </ul>
 */
public final class ModelGenerationMain {
	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_FAILURE = 1;
	public static final int EXIT_USAGE = 2;

	public static final String PROGRAM_NAME = "generate-models";

	private static final Logger LOG = LoggerFactory.getLogger(ModelGenerationMain.class);

	private ModelGenerationMain() {
		throw new IllegalStateException("This is a static utility class and should not be instantiated directly");
	}

	public static int run(String[] args) {
		var injector = StandaloneRefinery.getInjector();
		var jobCommand = injector.getInstance(JobCommand.class);
		var generateCommand = injector.getInstance(GenerateCommand.class);
		var jCommander = JCommander.newBuilder()
				.programName(PROGRAM_NAME)
				.addCommand("job", jobCommand)
				.addCommand("generate", generateCommand)
				.build();
		try {
			jCommander.parse(args);
		} catch (ParameterException e) {
			LOG.error("Invalid arguments: {}", e.getMessage());
			jCommander.usage();
			return EXIT_USAGE;
		}
		var parsedCommand = jCommander.getParsedCommand();
		if (parsedCommand == null) {
			jCommander.usage();
			return EXIT_USAGE;
		}
		return switch (parsedCommand) {
			case "job" -> jobCommand.run();
			case "generate" -> generateCommand.run();
			default -> throw new IllegalStateException("Unknown command: " + parsedCommand);
		};
	}

	public static void main(String[] args) {
		int exitValue = EXIT_FAILURE;
		try {
			exitValue = run(args);
		} catch (RuntimeException e) {
			LOG.error("Initialization error", e);
		}
		System.exit(exitValue);
	}
}
