/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.store.query.view;

import tools.refinery.store.representation.Symbol;
import tools.refinery.logic.term.truthvalue.TruthValue;
import tools.refinery.store.tuple.Tuple;

public class MustView extends TuplePreservingView<TruthValue> {
	public MustView(Symbol<TruthValue> symbol) {
		super(symbol, "must");
	}

	@Override
	protected boolean doFilter(Tuple key, TruthValue value) {
		return value.must();
	}
}
