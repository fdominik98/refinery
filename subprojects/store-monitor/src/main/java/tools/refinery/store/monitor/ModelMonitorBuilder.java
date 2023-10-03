package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapterBuilder;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.representation.Symbol;

public interface ModelMonitorBuilder extends ModelAdapterBuilder {

	ModelMonitorBuilder monitor(Monitor monitor);

	ModelMonitorBuilder clock(Symbol<Integer> clockSymbol);

	ModelMonitorBuilder withStateQueries();

}
