package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.store.monitor.internal.ModelMonitorBuilderImpl;
import tools.refinery.store.monitor.internal.model.StateMachineSummary;
import tools.refinery.store.monitor.internal.model.SymbolHolder;

public interface ModelMonitorAdapter extends ModelAdapter {
	static ModelMonitorBuilder builder() {
		return new ModelMonitorBuilderImpl();
	}

	SymbolHolder getSymbols();

	StateMachineSummary getSummary();
}
