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

import java.io.IOException;
import java.nio.file.Path;

@Parameters(commandDescription = "Run a SceneGEMS model generation job from its job folder")
public class JobCommand {
	private static final Logger LOG = LoggerFactory.getLogger(JobCommand.class);

	@Inject
	private GenerationRunner runner;

	@Parameter(names = {"-folder", "-f"}, description = "Job folder with job.json", placeholder = "PATH")
	private String folder = JobFolder.DEFAULT_JOB_FOLDER;

	public int run() {
		var jobFolder = Path.of(folder);
		GenerationRequest request;
		try {
			request = JobFolder.readRequest(jobFolder);
		} catch (IOException | InvalidRequestException e) {
			LOG.error("Invalid job", e);
			try (var status = new StatusReporter(System.out, GenerationRequest.DEFAULT_STATUS_INTERVAL_SECONDS,
					System::nanoTime, false)) {
				return GenerationRunner.fail(jobFolder, status, "invalid job: " + e.getMessage());
			}
		}
		return runner.run(request, jobFolder, System.out);
	}
}
