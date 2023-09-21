package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapterBuilder;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.monitor.internal.model.StateMachine;
import tools.refinery.store.monitor.internal.timeProviders.AbstractTimeProvider;

public interface ModelMonitorBuilder extends ModelAdapterBuilder {

	ModelMonitorBuilder monitor(Monitor monitor);

	ModelMonitorBuilder timeProvider(AbstractTimeProvider timeProvider);

	ModelMonitorBuilder withStateQueries();

}
