/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

/**
 * Thrown from the generator's cancellation checks when a stop was requested, to end the exploration early.
 */
public class StopRequestedException extends RuntimeException {
	public StopRequestedException() {
		super("Stop requested");
	}
}
