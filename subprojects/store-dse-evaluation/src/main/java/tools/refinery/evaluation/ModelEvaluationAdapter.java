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

public interface ModelEvaluationAdapter extends ModelAdapter {

	ModelEvaluationStoreAdapter getStoreAdapter();

	static ModelEvaluationBuilder builder() {
		return new ModelEvaluationBuilderImpl();
	}

	double evaluateAccuracy(EvaluationStore evaluationStore, Symbol<Boolean> acceptanceSymbol);
}
