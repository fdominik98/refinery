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
import tools.refinery.store.tuple.Tuple;

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
	private final Symbol<Boolean> inAcceptSymbol;
	private EvaluationState evaluationState;
	private long startTime;
	private long pauseTime;
	private long pauseTimeSpan;
	private long timeSpan;


	public ModelEvaluationAdapterImpl(Model model, ModelEvaluationStoreAdapterImpl storeAdapter, Symbol<Boolean> inAcceptSymbol) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.inAcceptSymbol = inAcceptSymbol;
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
	public EvaluationResult getEvaluationResult(EvaluationStore evaluationStore,
												int solutionNumber, List<Symbol<?>> symbols) {
		evaluationStore.convertToTrajectoryPaths();
		return new EvaluationResult(
				evaluationStore.getTrajectories().size(),
				solutionNumber,
				evaluationStore.getTimeSpan(),
				evaluateAccuracy(evaluationStore),
				evaluateDiversity(evaluationStore, symbols));
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public ModelEvaluationStoreAdapter getStoreAdapter() {
		return storeAdapter;
	}

	private double evaluateAccuracy(EvaluationStore evaluationStore) {
		double acceptedTrajectories = 0;
		for(var trajectory : evaluationStore.getTrajectories()) {
			if(isAccepted(trajectory)) {
				acceptedTrajectories++;
			}
		}
		return acceptedTrajectories / evaluationStore.getTrajectories().size();
	}

	private boolean isAccepted(List<Version> trajectory) {
		var currentVersion = model.getState();
		for(Version version : trajectory) {
			model.restore(version);
			var inAcceptInterpretation = model.getInterpretation(inAcceptSymbol);
			if(inAcceptInterpretation.get(Tuple.of())) {
				model.restore(currentVersion);
				return true;
			}
		}
		model.restore(currentVersion);
		return false;
	}

	private double evaluateDiversity(EvaluationStore evaluationStore, List<Symbol<?>> symbols) {
		Map<Version, ObjectCodeImpl> versionCodeMap = constructVersionCodeMap(evaluationStore, symbols);
		int numberOfTrajectories = evaluationStore.getTrajectories().size();
		if (numberOfTrajectories <= 1) {
			throw new IllegalArgumentException("There must be at least two trajectories to calculate pairwise " +
					"distance");
		}

		double totalDistance = 0;
		int count = 0;

		for (int i = 0; i < numberOfTrajectories; i++) {
			for (int j = i + 1; j < numberOfTrajectories; j++) {
				var t1 = evaluationStore.getTrajectories().get(i);
				var t2 = evaluationStore.getTrajectories().get(j);
				if(isAccepted(t1) && isAccepted(t2)){
					totalDistance += distance(versionCodeMap,t1, t2);
					count++;
				}
			}
		}

		return totalDistance / count;
	}
	@Override
	public double distance(ObjectCodeImpl objectCode1, ObjectCodeImpl objectCode2) {
		Map<Long, Integer> frequencyMap1 = new HashMap<>();
		Map<Long, Integer> frequencyMap2 = new HashMap<>();

		for (int i = 0; i < objectCode1.getSize(); i++) {
			frequencyMap1.put(objectCode1.get(i), frequencyMap1.getOrDefault(objectCode1.get(i), 0) + 1);
		}

		for (int i = 0; i < objectCode2.getSize(); i++) {
			frequencyMap2.put(objectCode2.get(i), frequencyMap1.getOrDefault(objectCode2.get(i), 0) + 1);
		}

		int unpairedCount = 0;

		// Iterate over the values in vector1 and check if they have a pair in vector2
		for (int i = 0; i < objectCode1.getSize(); i++) {
			int frequency1 = frequencyMap1.get(objectCode1.get(i));
			int frequency2 = frequencyMap2.getOrDefault(objectCode1.get(i), 0);

			if (frequency1 > frequency2) {
				unpairedCount += frequency1 - frequency2; // Count the unpaired values
				frequencyMap2.remove(objectCode1.get(i)); // Remove the paired values from vector2
			} else if (frequency1 < frequency2) {
				unpairedCount += frequency2 - frequency1; // Count the unpaired values
				frequencyMap1.remove(objectCode1.get(i)); // Remove the paired values from vector1
			} else {
				// Both vectors have the same frequency for this value, remove from both
				frequencyMap1.remove(objectCode1.get(i));
				frequencyMap2.remove(objectCode1.get(i));
			}
		}

		// Any remaining values in vector2 are unpaired
		for (int frequency : frequencyMap2.values()) {
			unpairedCount += frequency;
		}

		// Any remaining values in vector1 are unpaired
		for (int frequency : frequencyMap1.values()) {
			unpairedCount += frequency;
		}

		return unpairedCount;
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

	private Map<Version,ObjectCodeImpl> constructVersionCodeMap(EvaluationStore evaluationStore, List<Symbol<?>> symbols) {
		var interpretations = symbols.stream().map(model::getInterpretation).toList();
		ThreeDNeighbourhoodCalculator neighbourhoodCalculator = new ThreeDNeighbourhoodCalculator(model,
				interpretations, IntSets.mutable.empty());

		Map<Version, ObjectCodeImpl> versionCodeMap = new HashMap<>();

		var currentVersion = model.getState();
		for(var trajectory : evaluationStore.getTrajectories()) {
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

		double jaccardSimilarity = (double) intersection.size() / union.size();

		// Return Jaccard Distance
		return 1.0 - jaccardSimilarity;
	}
}
