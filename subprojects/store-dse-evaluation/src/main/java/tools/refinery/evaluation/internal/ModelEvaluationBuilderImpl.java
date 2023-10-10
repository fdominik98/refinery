/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import tools.refinery.store.adapter.AbstractModelAdapterBuilder;
import tools.refinery.store.adapter.ModelAdapterBuilder;
import tools.refinery.store.model.ModelStore;
import tools.refinery.evaluation.ModelEvaluationBuilder;
import tools.refinery.store.representation.Symbol;

public class ModelEvaluationBuilderImpl
		extends AbstractModelAdapterBuilder<ModelEvaluationStoreAdapterImpl>
		implements ModelEvaluationBuilder {

	private Symbol<Boolean> acceptanceSymbol;

	@Override
	protected ModelEvaluationStoreAdapterImpl doBuild(ModelStore store) {
		return new ModelEvaluationStoreAdapterImpl(store, acceptanceSymbol);
	}

	@Override
	public ModelAdapterBuilder acceptanceSymbol(Symbol<Boolean> acceptanceSymbol) {
		this.acceptanceSymbol = acceptanceSymbol;
		return this;
	}
}
