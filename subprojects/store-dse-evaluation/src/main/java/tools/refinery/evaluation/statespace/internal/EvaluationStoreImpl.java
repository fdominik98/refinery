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

	private final Set<Version> acceptedVersions = new HashSet<>();

	private Optional<Long> timeSpan = Optional.empty();

	@Override
	public List<Stack<Version>> convertToTrajectoryPaths() {
		if (isEmpty()) {
			throw new IllegalArgumentException("No trajectories were found");
		}

		Stack<Version> longestTrajectory = new Stack<>();

		trajectories = new ArrayList<>();
		for(var version : entrySet()) {
			Version newVersion = version.getKey();
			if (!acceptedVersions.contains(newVersion)){
				continue;
			}
			Stack<Version> trajectory = new Stack<>();
			while(containsKey(newVersion)) {
				trajectory.push(newVersion);
				newVersion = get(newVersion);
			}
			trajectory.push(newVersion);
			if(trajectory.size() > longestTrajectory.size()) {
				longestTrajectory = trajectory;
			}
			trajectories.add(trajectory);
		}

		for(var trajectory : trajectories) {
			while(trajectory.size() != longestTrajectory.size()){
				trajectory.add(trajectory.lastElement());
			}
		}

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
	public void addModelVersion(Version newVersion, Version oldVersion) {
		put(newVersion, oldVersion);
	}

	@Override
	public void addSolution(Version solution) {
		acceptedVersions.add(solution);
	}

	@Override
	public int getAllVersionSize() {
		return size();
	}
}
