package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapterBuilder;
import tools.refinery.store.monitor.internal.model.StateMachine;
import tools.refinery.store.representation.Symbol;
import java.util.Collection;

public interface ModelMonitorBuilder extends ModelAdapterBuilder {

	ModelMonitorBuilder monitor(StateMachine monitor);

	ModelMonitorBuilder relations(Symbol<Boolean>... relations);

	ModelMonitorBuilder relations(Collection<Symbol<Boolean>> relations);

	ModelMonitorBuilder timeProvider(AbstractTimeProvider timeProvider);

	ModelMonitorBuilder withStateQueries();
}
