package tools.refinery.store.monitor.internal;

import tools.refinery.store.adapter.ModelStoreAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelListener;
import tools.refinery.store.monitor.internal.timeProviders.AbstractTimeProvider;
import tools.refinery.store.monitor.ModelMonitorAdapter;
import tools.refinery.store.monitor.TimeListener;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.monitor.internal.timeProviders.TimeProviderMock;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.tuple.Tuple;

public class ModelMonitorAdapterImpl implements ModelMonitorAdapter, ModelListener, TimeListener {

	private final Model model;
	private final ModelMonitorStoreAdapterImpl storeAdapter;
	private final AbstractTimeProvider timeProvider;
	private final Monitor monitor;

	ModelMonitorAdapterImpl(Model model, ModelMonitorStoreAdapterImpl storeAdapter,
							AbstractTimeProvider timeProvider,
							Monitor monitor) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.timeProvider = timeProvider != null ? timeProvider : new TimeProviderMock();
		this.monitor = monitor;

		var queryEngine = this.model.getAdapter(ModelQueryAdapter.class);
		var stateInterpretation = this.model.getInterpretation(this.monitor.getStartSymbol().symbol);
		this.monitor.clockHolder.reset(this.timeProvider.getTime());
		stateInterpretation.put(Tuple.of(), this.monitor.clockHolder);
		queryEngine.flushChanges();

		this.model.addListener(this);
		this.timeProvider.addListener(this);

		var fitnessInterpretation = this.model.getInterpretation(this.monitor.fitnessSymbol);
		fitnessInterpretation.put(Tuple.of(), (double)monitor.stateMachine.startState.weight);
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public ModelStoreAdapter getStoreAdapter() {
		return storeAdapter;
	}

	@Override
	public void beforeCommit() {
		for (var action : storeAdapter.getActions()) {
			action.accept(model, timeProvider.getTime());
		}
	}

	@Override
	public void oneUnitPassed(int now) {
		for (var action : storeAdapter.getActions()) {
			action.accept(model, now);
		}
	}

	@Override
	public Monitor getMonitor() {
		return monitor;
	}
}
