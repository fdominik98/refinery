/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.evaluation.ModelEvaluationStoreAdapter;
import tools.refinery.evaluation.statespace.EvaluationStore;
import tools.refinery.store.map.Version;
import tools.refinery.store.model.Model;
import tools.refinery.evaluation.ModelEvaluationAdapter;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.statecoding.neighbourhood.ObjectCodeImpl;
import tools.refinery.store.tuple.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelEvaluationAdapterImpl implements ModelEvaluationAdapter {
	public enum EvaluationState {
		NOT_STARTED,
		STARTED,
		PAUSED,
		COMPLETED
	}

	private static final Logger LOG = LoggerFactory.getLogger(ModelEvaluationAdapterImpl.class);

	private final Model model;
	private final ModelEvaluationStoreAdapterImpl storeAdapter;
	private final Symbol<Boolean> acceptanceSymbol;

	private EvaluationState evaluationState;

	private long startTime;
	private long pauseTime;
	private long pauseTimeSpan;
	private long timeSpan;


	public ModelEvaluationAdapterImpl(Model model, ModelEvaluationStoreAdapterImpl storeAdapter, Symbol<Boolean> acceptanceSymbol) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.acceptanceSymbol = acceptanceSymbol;

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

	@Override
	public double evaluateAccuracy(EvaluationStore evaluationStore) {
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
			var acceptanceInterpretation = model.getInterpretation(acceptanceSymbol);
			if(acceptanceInterpretation.get(Tuple.of())) {
				model.restore(currentVersion);
				return true;
			}
		}
		model.restore(currentVersion);
		return false;
	}

	@Override
	public double evaluateDiversity(EvaluationStore evaluationStore, List<Symbol<?>> symbols) {
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
		var o1 = objectCode1.getSize() > objectCode2.getSize() ? objectCode1 : objectCode2;
		var o2 = objectCode1 == o1 ? objectCode2 : objectCode1;

		int sum = 0;
		for(int i = 0; i < o1.getSize(); i++) {
			long v1 = o1.get(i);
			long v2 = o2.getSize() > i ? o2.get(i) : 0;
			sum += Math.pow(v1 - v2, 2);
		}
		return Math.sqrt(sum);
	}

	private double distance(Map<Version,ObjectCodeImpl> versionCodeMap, List<Version> trajectory1,
							List<Version> trajectory2) {
		var t1 = trajectory1.size() > trajectory2.size() ? trajectory1 : trajectory2;
		var t2 = trajectory1 == t1 ? trajectory2 : trajectory1;

		double sum = 0.0;
		for (int i = 0; i < t1.size(); i++) {
			var code1 = versionCodeMap.get(t1.get(i));
			var code2 = t2.size() > i ? versionCodeMap.get(t2.get(i)) : new ObjectCodeImpl();
			sum += distance(code1, code2);
		}
		return sum / t1.size();
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
}
