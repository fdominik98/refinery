/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.statespace;

import tools.refinery.store.map.Version;
import java.util.List;
import java.util.Stack;

public interface EvaluationStore {
	List<Stack<Version>> convertToTrajectoryPaths();
	long getTimeSpan();
	void setTimeSpan(long timeSpan);
	void addModelVersion(Version newVersion, Version oldVersion);
	void addSolution(Version solution);
	int getAllVersionSize();
}
