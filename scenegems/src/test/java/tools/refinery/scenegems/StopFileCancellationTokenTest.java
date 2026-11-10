/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StopFileCancellationTokenTest {
	@TempDir
	private Path folder;

	private long now;

	@Test
	void noStopWithoutFile() {
		var token = new StopFileCancellationToken(folder.resolve("stop"), () -> now);
		assertDoesNotThrow(token::checkCancelled);
		assertThat(token.isStopRequested(), is(false));
	}

	@Test
	void stopWhenFileAppears() throws IOException {
		var stopFile = folder.resolve("stop");
		var token = new StopFileCancellationToken(stopFile, () -> now);
		token.checkCancelled();
		Files.writeString(stopFile, "");
		// The file system is only looked at once per check interval.
		assertDoesNotThrow(token::checkCancelled);
		now += StopFileCancellationToken.CHECK_INTERVAL_NANOS;
		assertThrows(StopRequestedException.class, token::checkCancelled);
		assertThat(token.isStopRequested(), is(true));
		// A stop stays requested even if the file goes away.
		Files.delete(stopFile);
		assertThrows(StopRequestedException.class, token::checkCancelled);
	}
}
