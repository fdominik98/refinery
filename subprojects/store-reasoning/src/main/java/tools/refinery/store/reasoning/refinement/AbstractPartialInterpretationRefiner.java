/*
 * SPDX-FileCopyrightText: 2023-2024 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.reasoning.refinement;

import tools.refinery.logic.AbstractValue;
import tools.refinery.store.dse.propagation.PropagationAdapter;
import tools.refinery.store.reasoning.ReasoningAdapter;
import tools.refinery.store.reasoning.representation.PartialSymbol;

public abstract class AbstractPartialInterpretationRefiner<A extends AbstractValue<A, C>, C>
		implements PartialInterpretationRefiner<A, C> {
	private final ReasoningAdapter adapter;
	private final PartialSymbol<A, C> partialSymbol;

	protected AbstractPartialInterpretationRefiner(ReasoningAdapter adapter, PartialSymbol<A, C> partialSymbol) {
		this.adapter = adapter;
		this.partialSymbol = partialSymbol;
	}

	@Override
	public ReasoningAdapter getAdapter() {
		return adapter;
	}

	@Override
	public PartialSymbol<A, C> getPartialSymbol() {
		return partialSymbol;
	}

	public abstract static class ConcretizationAware<A extends AbstractValue<A, C>, C> extends
			AbstractPartialInterpretationRefiner<A, C> {
		private final PropagationAdapter propagationAdapter;

		protected ConcretizationAware(ReasoningAdapter adapter, PartialSymbol<A, C> partialSymbol) {
			super(adapter, partialSymbol);
			propagationAdapter = adapter.getModel().tryGetAdapter(PropagationAdapter.class).orElse(null);
		}

		protected boolean concretizationInProgress() {
			return propagationAdapter != null && propagationAdapter.concretizationInProgress();
		}
	}
}
