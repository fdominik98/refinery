package tools.refinery.store.monitor.internal;

import tools.refinery.store.adapter.ModelStoreAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelListener;
import tools.refinery.store.monitor.AbstractTimeProvider;
import tools.refinery.store.monitor.ModelMonitorAdapter;
import tools.refinery.store.monitor.TimeListener;
import tools.refinery.store.monitor.internal.model.StateMachineSummary;
import tools.refinery.store.monitor.internal.model.SymbolHolder;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.tuple.Tuple;

public class ModelMonitorAdapterImpl implements ModelMonitorAdapter, ModelListener, TimeListener {

	private final Model model;
	private final ModelMonitorStoreAdapterImpl storeAdapter;
	private final AbstractTimeProvider timeProvider;
	private final SymbolHolder symbolHolder;
	private final StateMachineSummary summary;

	ModelMonitorAdapterImpl(Model model, ModelMonitorStoreAdapterImpl storeAdapter,
							AbstractTimeProvider timeProvider,
							SymbolHolder symbolHolder, StateMachineSummary summary) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.timeProvider = timeProvider != null ? timeProvider : new TimeProviderMock();
		this.symbolHolder = symbolHolder;
		this.summary = summary;

		var queryEngine = this.model.getAdapter(ModelQueryAdapter.class);
		var stateInterpretation = this.model.getInterpretation(this.symbolHolder.getStartSymbols().symbol);
		this.symbolHolder.clockHolder.reset(this.timeProvider.getTime());
		stateInterpretation.put(Tuple.of(), this.symbolHolder.clockHolder);
		queryEngine.flushChanges();

		this.model.addListener(this);
		this.timeProvider.addListener(this);
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
	public SymbolHolder getSymbols() {
		return symbolHolder;
	}

	@Override
	public StateMachineSummary getSummary() {
		return summary;
	}


}
