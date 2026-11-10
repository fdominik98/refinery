/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

/**
 * Why model generation stopped. Only {@link #EXHAUSTED} and {@link #UNSATISFIABLE} mean that the generated models
 * are all the models of the problem.
 */
public enum StopReason {
	EXHAUSTED("exhausted", "all models found", true),
	CAP_REACHED("cap_reached", "model cap reached", false),
	TIMEOUT("timeout", "timed out", false),
	STOPPED("stopped", "stop requested", false),
	UNSATISFIABLE("unsatisfiable", "the problem has no models", true);

	private final String id;
	private final String description;
	private final boolean complete;

	StopReason(String id, String description, boolean complete) {
		this.id = id;
		this.description = description;
		this.complete = complete;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isComplete() {
		return complete;
	}
}
