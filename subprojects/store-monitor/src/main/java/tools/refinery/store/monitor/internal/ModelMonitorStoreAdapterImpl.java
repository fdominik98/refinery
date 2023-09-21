package tools.refinery.store.monitor.internal;

import tools.refinery.store.adapter.ModelAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelStore;
import tools.refinery.store.monitor.internal.timeProviders.AbstractTimeProvider;
import tools.refinery.store.monitor.ModelMonitorStoreAdapter;
import tools.refinery.store.monitor.internal.model.Monitor;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class ModelMonitorStoreAdapterImpl implements ModelMonitorStoreAdapter {

	private final ModelStore store;
	private final List<BiConsumer<Model, Integer>> actionSet;
	private final Monitor monitor;
	private final AbstractTimeProvider timeProvider;

	ModelMonitorStoreAdapterImpl(ModelStore store, AbstractTimeProvider timeProvider,
								 List<BiConsumer<Model, Integer>> actionSet, Monitor monitor) {
		this.store = store;
		this.actionSet = actionSet;
		this.monitor = monitor;
		this.timeProvider = timeProvider;
	}
	@Override
	public ModelStore getStore() {
		return store;
	}

	@Override
	public Collection<BiConsumer<Model, Integer>> getActions() {
		return actionSet;
	}

	@Override
	public ModelAdapter createModelAdapter(Model model) {
		return new ModelMonitorAdapterImpl(model, this, timeProvider, monitor);
	}
}
