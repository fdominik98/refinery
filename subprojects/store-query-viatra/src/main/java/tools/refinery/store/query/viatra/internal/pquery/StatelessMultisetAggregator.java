package tools.refinery.store.query.viatra.internal.pquery;

import org.eclipse.viatra.query.runtime.matchers.psystem.aggregations.IMultisetAggregationOperator;
import tools.refinery.store.query.term.StatelessAggregator;

import java.util.stream.Stream;

record StatelessMultisetAggregator<R, T>(StatelessAggregator<R, T> aggregator)
		implements IMultisetAggregationOperator<T, R, R> {
	@Override
	public String getShortDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return aggregator.toString();
	}

	@Override
	public R createNeutral() {
		return aggregator.getEmptyResult();
	}

	@Override
	public boolean isNeutral(R result) {
		return createNeutral().equals(result);
	}

	@Override
	public R update(R oldResult, T updateValue, boolean isInsertion) {
		return isInsertion ? aggregator.add(oldResult, updateValue) : aggregator.remove(oldResult, updateValue);
	}

	@Override
	public R getAggregate(R result) {
		return result;
	}

	@Override
	public R clone(R original) {
		// Aggregate result is immutable.
		return original;
	}

	@Override
	public R aggregateStream(Stream<T> stream) {
		return aggregator.aggregateStream(stream);
	}
}
