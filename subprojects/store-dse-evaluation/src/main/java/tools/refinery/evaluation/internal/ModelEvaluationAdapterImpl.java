/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import org.eclipse.collections.api.factory.primitive.IntSets;
import tools.refinery.evaluation.ModelEvaluationStoreAdapter;
import tools.refinery.evaluation.statespace.EvaluationStore;
import tools.refinery.store.map.Version;
import tools.refinery.store.model.Model;
import tools.refinery.evaluation.ModelEvaluationAdapter;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.statecoding.neighbourhood.ObjectCodeImpl;

import java.util.*;

public class ModelEvaluationAdapterImpl implements ModelEvaluationAdapter {
	public enum EvaluationState {
		NOT_STARTED,
		STARTED,
		PAUSED,
		COMPLETED
	}

	private final Model model;
	private final ModelEvaluationStoreAdapterImpl storeAdapter;
	private EvaluationState evaluationState;
	private long startTime;
	private long pauseTime;
	private long pauseTimeSpan;
	private long timeSpan;


	public ModelEvaluationAdapterImpl(Model model, ModelEvaluationStoreAdapterImpl storeAdapter) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.evaluationState = EvaluationState.NOT_STARTED;
		this.pauseTime = 0;
		this.startTime = 0;
		this.pauseTimeSpan = 0;
		this.timeSpan = 0;
	}

	@Override
	public void start() {
		if(evaluationState != EvaluationState.NOT_STARTED) {
			throw new IllegalArgumentException("Evaluation has been already started before");
		}
		evaluationState = EvaluationState.STARTED;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void stop(EvaluationStore evaluationStore) {
		long now = System.currentTimeMillis();
		switch(evaluationState) {
			case PAUSED -> 	timeSpan = (now - startTime) - pauseTimeSpan - (now - pauseTime);
			case STARTED -> timeSpan = (now - startTime) - pauseTimeSpan;
			case COMPLETED -> throw new IllegalArgumentException("Evaluation has been already completed");
			case NOT_STARTED -> throw new IllegalArgumentException("Evaluation has not been started");
		}
		evaluationState = EvaluationState.COMPLETED;
		evaluationStore.setTimeSpan(timeSpan);
	}

	@Override
	public void pause() {
		long now = System.currentTimeMillis();
		switch(evaluationState) {
			case PAUSED -> 	throw new IllegalArgumentException("Evaluation is already paused");
			case STARTED -> pauseTime = now;
			case COMPLETED -> throw new IllegalArgumentException("Evaluation has been already completed");
			case NOT_STARTED -> throw new IllegalArgumentException("Evaluation has not been started");
		}
		evaluationState = EvaluationState.PAUSED;
	}

	@Override
	public void resume() {
		long now = System.currentTimeMillis();
		switch(evaluationState) {
			case PAUSED -> 	pauseTimeSpan += (now - pauseTime);
			case STARTED -> throw new IllegalArgumentException("Evaluation has not been paused");
			case COMPLETED -> throw new IllegalArgumentException("Evaluation has been already completed");
			case NOT_STARTED -> throw new IllegalArgumentException("Evaluation has not been started");
		}
		evaluationState = EvaluationState.STARTED;
	}

	@Override
	public EvaluationResult getEvaluationResult(EvaluationStore evaluationStore, List<Symbol<?>> symbols) {
		var trajectories = evaluationStore.convertToTrajectoryPaths();
		double sum = 0;
		for(var t : trajectories) {
			sum += t.size();
		}

		return new EvaluationResult(
				evaluationStore.getAllVersionSize(),
				sum / trajectories.size(),
				evaluationStore.getTimeSpan(),
				evaluateDistance(trajectories, symbols));
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public ModelEvaluationStoreAdapter getStoreAdapter() {
		return storeAdapter;
	}

	private double evaluateDistance(List<Stack<Version>> trajectories, List<Symbol<?>> symbols) {
		Map<Version, ObjectCodeImpl> versionCodeMap = constructVersionCodeMap(trajectories, symbols);
		int numberOfTrajectories = trajectories.size();
		if (numberOfTrajectories < 2) {
			return 0;
		}

		double totalDistance = 0;
		int count = 0;

		for (int i = 0; i < numberOfTrajectories; i++) {
			for (int j = i + 1; j < numberOfTrajectories; j++) {
				var t1 = trajectories.get(i);
				var t2 = trajectories.get(j);
				totalDistance += distance(versionCodeMap,t1, t2);
				count++;
			}
		}

		return totalDistance / count;
	}

	private double distance(Map<Version,ObjectCodeImpl> versionCodeMap, List<Version> trajectory1,
							List<Version> trajectory2) {

		if(trajectory1.size() != trajectory2.size()) {
			throw  new IllegalArgumentException("Trajectory sizes do not match");
		}

		double sum = 0.0;
		for (int i = 0; i < trajectory1.size(); i++) {
			var code1 = versionCodeMap.get(trajectory1.get(i));
			var code2 = versionCodeMap.get(trajectory2.get(i));
			sum += jaccardDistance(code1, code2);
		}
		return sum / trajectory1.size();
	}

	private Map<Version,ObjectCodeImpl> constructVersionCodeMap(List<Stack<Version>> trajectories,
																List<Symbol<?>> symbols) {
		var interpretations = symbols.stream().map(model::getInterpretation).toList();
		ThreeDNeighbourhoodCalculator neighbourhoodCalculator = new ThreeDNeighbourhoodCalculator(model,
				interpretations, IntSets.mutable.empty());

		Map<Version, ObjectCodeImpl> versionCodeMap = new HashMap<>();

		var currentVersion = model.getState();
		for(var trajectory : trajectories) {
			for(Version version : trajectory) {
				if(!versionCodeMap.containsKey(version)) {
					model.restore(version);
					ObjectCodeImpl oldObjectCode = (ObjectCodeImpl)neighbourhoodCalculator.calculateCodes().objectCode();
					versionCodeMap.put(version, new ObjectCodeImpl(oldObjectCode));
				}
			}
		}
		model.restore(currentVersion);
		return versionCodeMap;
	}

	private static double jaccardDistance(ObjectCodeImpl objectCode1, ObjectCodeImpl objectCode2) {
		Set<Long> set1 = new HashSet<>();
		for (int i = 0; i < objectCode1.getSize(); i++) {
			set1.add(objectCode1.get(i));
		}
		Set<Long> set2 = new HashSet<>();
		for (int i = 0; i < objectCode2.getSize(); i++) {
			set2.add(objectCode2.get(i));
		}

		// Calculate intersection size
		Set<Long> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);

		// Calculate union size
		Set<Long> union = new HashSet<>(set1);
		union.addAll(set2);

		return (double) intersection.size() / union.size();
	}
}
