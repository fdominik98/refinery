/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.statespace.internal;

import tools.refinery.store.map.Version;
import tools.refinery.evaluation.statespace.EvaluationStore;
import java.util.*;

public class EvaluationStoreImpl extends HashMap<Version, Version> implements EvaluationStore{

	private List<Stack<Version>> trajectories;

	private Optional<Long> timeSpan = Optional.empty();

	@Override
	public List<Stack<Version>> convertToTrajectoryPaths() {
		if (isEmpty()) {
			throw new IllegalArgumentException("No trajectories were found");
		}

		Stack<Version> longestTrajectory = new Stack<>();

		List<Stack<Version>> trajectories = new ArrayList<>();
		for(var version : entrySet()) {
			Stack<Version> trajectory = new Stack<>();
			Version lastVersion = version.getKey();
			trajectory.push(lastVersion);
			do {
				lastVersion = get(lastVersion);
				trajectory.push(lastVersion);
			}while(containsKey(lastVersion));
			trajectories.add(trajectory);
			if(trajectory.size() > longestTrajectory.size()) {
				longestTrajectory = trajectory;
			}
		}

		for(var trajectory : trajectories) {
			while(trajectory.size() != longestTrajectory.size()){
				trajectory.add(trajectory.lastElement());
			}
		}

		this.trajectories = trajectories;
		return trajectories;
	}

	@Override
	public long getTimeSpan() {
		if (timeSpan.isEmpty()) {
			throw new IllegalArgumentException("Evaluation is not ready");
		}
		return timeSpan.get();
	}

	@Override
	public void setTimeSpan(long timeSpan) {
		this.timeSpan = Optional.of(timeSpan);
	}

	@Override
	public List<Stack<Version>> getTrajectories() {
		return trajectories;
	}

	@Override
	public void addModelVersion(Version oldVersion, Version newVersion) {
		put(oldVersion, newVersion);
	}
}
