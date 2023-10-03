package tools.refinery.store.monitor.internal;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.ModelMonitorStoreAdapter;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.representation.Symbol;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ModelMonitorStoreAdapterImpl implements ModelMonitorStoreAdapter {

	private final ModelStore store;
	private final List<Consumer<Model>> actionSet;
	private final Monitor monitor;
	private final Symbol<Integer> clockSymbol;

	ModelMonitorStoreAdapterImpl(ModelStore store, List<Consumer<Model>> actionSet,
								 Monitor monitor, Symbol<Integer> clockSymbol) {
		this.store = store;
		this.actionSet = actionSet;
		this.monitor = monitor;
		this.clockSymbol = clockSymbol;
	}
	@Override
	public ModelStore getStore() {
		return store;
	}

	@Override
	public Collection<Consumer<Model>> getActions() {
		return actionSet;
	}

	@Override
	public ModelAdapter createModelAdapter(Model model) {
		return new ModelMonitorAdapterImpl(model, this, monitor, clockSymbol);
	}
}
