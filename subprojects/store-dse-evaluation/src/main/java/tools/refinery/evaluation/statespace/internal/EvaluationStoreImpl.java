/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.statespace.internal;

import tools.refinery.store.map.Version;
import tools.refinery.evaluation.statespace.EvaluationStore;
import java.util.ArrayList;
import java.util.List;

public class EvaluationStoreImpl implements EvaluationStore {

	private final List<List<Version>> trajectories = new ArrayList<>();


	@Override
	public synchronized void addModelVersion(Version oldVersion, Version newVersion) {
		if (trajectories.isEmpty()) {
			trajectories.add(new ArrayList<>(List.of(oldVersion)));
		}

		List<Version> newTrajectory = null;

		for (List<Version> trajectory : trajectories) {
			int index = trajectory.indexOf(oldVersion);

			if (index == trajectory.size() - 1) {
				trajectory.add(newVersion);
				return;
			} else if (index != -1) {
				newTrajectory = new ArrayList<>(trajectory.subList(0, index + 1));
				newTrajectory.add(newVersion);
				break;
			}
		}

		if (newTrajectory == null) {
			throw new IllegalArgumentException("No old version found: " + oldVersion);
		}

		trajectories.add(newTrajectory);
	}

	@Override
	public List<List<Version>> getTrajectories() {
		return trajectories;
	}
}
