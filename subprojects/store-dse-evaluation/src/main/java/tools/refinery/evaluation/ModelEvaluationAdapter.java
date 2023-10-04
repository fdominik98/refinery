/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.evaluation.internal.ModelEvaluationBuilderImpl;

public interface ModelEvaluationAdapter extends ModelAdapter {

	ModelEvaluationStoreAdapter getStoreAdapter();
	static ModelEvaluationBuilder builder() {
		return new ModelEvaluationBuilderImpl();
	}
}
