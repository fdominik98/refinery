/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.evaluation.ModelEvaluationStoreAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.evaluation.ModelEvaluationAdapter;

public class ModelEvaluationAdapterImpl implements ModelEvaluationAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(ModelEvaluationAdapterImpl.class);

	private final Model model;
	private final ModelEvaluationStoreAdapterImpl storeAdapter;

	public ModelEvaluationAdapterImpl(Model model, ModelEvaluationStoreAdapterImpl storeAdapter) {
		this.model = model;
		this.storeAdapter = storeAdapter;
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public ModelEvaluationStoreAdapter getStoreAdapter() {
		return storeAdapter;
	}
}
