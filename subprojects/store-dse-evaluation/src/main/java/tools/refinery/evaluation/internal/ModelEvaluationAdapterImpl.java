/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.evaluation.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.refinery.evaluation.ModelEvaluationStoreAdapter;
import tools.refinery.evaluation.statespace.EvaluationStore;
import tools.refinery.store.map.Version;
import tools.refinery.store.model.Model;
import tools.refinery.evaluation.ModelEvaluationAdapter;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;

public class ModelEvaluationAdapterImpl implements ModelEvaluationAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(ModelEvaluationAdapterImpl.class);

	private final Model model;
	private final ModelEvaluationStoreAdapterImpl storeAdapter;
	private final ModelQueryAdapter queryEngine;

	public ModelEvaluationAdapterImpl(Model model, ModelEvaluationStoreAdapterImpl storeAdapter) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.queryEngine = model.getAdapter(ModelQueryAdapter.class);
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
	public double evaluateAccuracy(EvaluationStore evaluationStore, Symbol<Boolean> acceptanceSymbol) {
		double acceptedTrajectories = 0;
		var currentVersion = model.getState();
		for(var trajectory : evaluationStore.getTrajectories()) {
			for(Version version : trajectory) {
				model.restore(version);
				var acceptanceInterpretation = model.getInterpretation(acceptanceSymbol);
				if(acceptanceInterpretation.get(Tuple.of())) {
					acceptedTrajectories++;
					break;
				}
			}
		}
		model.restore(currentVersion);
		return acceptedTrajectories / evaluationStore.getTrajectories().size();
	}

}
