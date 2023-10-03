package tools.refinery.store.monitor;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.store.monitor.internal.ModelMonitorBuilderImpl;
import tools.refinery.store.monitor.internal.model.Monitor;

public interface ModelMonitorAdapter extends ModelAdapter {
	static ModelMonitorBuilder builder() {
		return new ModelMonitorBuilderImpl();
	}
	Monitor getMonitor();
	void init();
	void refreshStates();
}
