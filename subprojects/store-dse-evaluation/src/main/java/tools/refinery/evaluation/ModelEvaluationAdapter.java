/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation;

import tools.refinery.evaluation.statespace.EvaluationStore;
import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.evaluation.internal.ModelEvaluationBuilderImpl;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.statecoding.neighbourhood.ObjectCodeImpl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface ModelEvaluationAdapter extends ModelAdapter {
	record EvaluationResult (long timeSpan,double accuracy,double diversity){}

	class EvaluationResultContainer extends ArrayList<EvaluationResult> {
		private EvaluationResult calculateMedian(Comparator comp){
			sort(comp);
			var res1 = get(size() / 2);
			if(size() % 2 == 0) {
				var res2 = get(size() / 2 - 1);
				return new EvaluationResult(
						(res1.timeSpan + res2.timeSpan) / 2,
						(res1.accuracy + res2.accuracy) / 2,
						(res1.diversity + res2.diversity) / 2
				);
			}
			else {
				return new EvaluationResult(res1.timeSpan,
						res1.accuracy,
						res1.diversity);
			}
		}

		public EvaluationResult medianByTimeSpan() {
			return calculateMedian(Comparator.comparingLong(EvaluationResult::timeSpan));
		}

		public EvaluationResult medianByAccuracy() {
			return calculateMedian(Comparator.comparingDouble(EvaluationResult::accuracy));
		}

		public EvaluationResult medianByDiversity() {
			return calculateMedian(Comparator.comparingDouble(EvaluationResult::diversity));
		}
	}

	ModelEvaluationStoreAdapter getStoreAdapter();

	static ModelEvaluationBuilder builder() {
		return new ModelEvaluationBuilderImpl();
	}

	double evaluateAccuracy(EvaluationStore evaluationStore);

	double evaluateDiversity(EvaluationStore evaluationStore,
											   List<Symbol<?>> symbols);

	double distance(ObjectCodeImpl objectCode1, ObjectCodeImpl objectCode2);


	void start();
	void stop(EvaluationStore evaluationStore);
	void pause();
	void resume();

	EvaluationResult getEvaluationResult(EvaluationStore evaluationStore, List<Symbol<?>> symbols);
}
