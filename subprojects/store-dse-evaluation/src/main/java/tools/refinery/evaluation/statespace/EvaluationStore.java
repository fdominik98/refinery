/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.statespace;

import tools.refinery.store.map.Version;

import java.util.List;
import java.util.Map;

public interface EvaluationStore {
	void addModelVersion(Version oldVersion, Version newVersion);
	List<List<Version>> getTrajectories();
}
