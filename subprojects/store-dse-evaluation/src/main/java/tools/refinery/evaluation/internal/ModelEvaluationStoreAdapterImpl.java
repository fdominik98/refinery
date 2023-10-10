/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.evaluation.ModelEvaluationStoreAdapter;
import tools.refinery.store.representation.Symbol;

public class ModelEvaluationStoreAdapterImpl implements ModelEvaluationStoreAdapter {
	private final ModelStore store;

	private final Symbol<Boolean> acceptanceSymbol;

	public ModelEvaluationStoreAdapterImpl(ModelStore store, Symbol<Boolean> acceptanceSymbol) {
		this.store = store;
		this.acceptanceSymbol = acceptanceSymbol;
	}

	@Override
	public ModelStore getStore() {
		return store;
	}

	@Override
	public ModelAdapter createModelAdapter(Model model) {
		return new ModelEvaluationAdapterImpl(model, this, acceptanceSymbol);
	}

}
