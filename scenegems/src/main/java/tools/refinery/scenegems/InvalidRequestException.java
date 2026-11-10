/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

/**
 * A generation request that can't be run as given, for example because a payload field has the wrong type.
 */
public class InvalidRequestException extends IllegalArgumentException {
	public InvalidRequestException(String message) {
		super(message);
	}

	public InvalidRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
