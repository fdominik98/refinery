package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapterBuilder;
import tools.refinery.store.monitor.internal.model.StateMachine;

public interface ModelMonitorBuilder extends ModelAdapterBuilder {

	ModelMonitorBuilder monitor(StateMachine monitor);

	ModelMonitorBuilder timeProvider(AbstractTimeProvider timeProvider);

	ModelMonitorBuilder withStateQueries();
}
