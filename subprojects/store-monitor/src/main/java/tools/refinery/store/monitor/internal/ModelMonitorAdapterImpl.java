package tools.refinery.store.monitor.internal;

import tools.refinery.store.adapter.ModelStoreAdapter;
import tools.refinery.store.model.Model;
import tools.refinery.store.model.ModelListener;
import tools.refinery.store.monitor.ModelMonitorAdapter;
import tools.refinery.store.monitor.internal.model.Monitor;
import tools.refinery.store.query.ModelQueryAdapter;
import tools.refinery.store.representation.Symbol;
import tools.refinery.store.tuple.Tuple;
import tools.refinery.store.tuple.Tuple0;

public class ModelMonitorAdapterImpl implements ModelMonitorAdapter, ModelListener {

	private final Model model;
	private final ModelMonitorStoreAdapterImpl storeAdapter;
	private final Monitor monitor;
	private final Symbol<Integer> clockSymbol;
	private final ModelQueryAdapter queryEngine;

	ModelMonitorAdapterImpl(Model model, ModelMonitorStoreAdapterImpl storeAdapter,	Monitor monitor,
							Symbol<Integer> clockSymbol) {
		this.model = model;
		this.storeAdapter = storeAdapter;
		this.monitor = monitor;
		this.clockSymbol = clockSymbol;

		this.model.addListener(this);
		this.queryEngine = this.model.getAdapter(ModelQueryAdapter.class);
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
		refreshStates();
	}

	@Override
	public Monitor getMonitor() {
		return monitor;
	}

	@Override
	public void init() {
		var stateInterpretation = this.model.getInterpretation(this.monitor.getStartSymbol().symbol);

		int now = 0;
		if(clockSymbol != null){
			var clockInterpretation = this.model.getInterpretation(clockSymbol);
			now = clockInterpretation.get(Tuple.of());
		}
		this.monitor.clockHolder.reset(now);
		stateInterpretation.put(Tuple.of(), this.monitor.clockHolder);

		var fitnessInterpretation = this.model.getInterpretation(this.monitor.fitnessSymbol);
		fitnessInterpretation.put(Tuple.of(), monitor.stateMachine.startState.weight);

		queryEngine.flushChanges();
	}

	@Override
	public void refreshStates() {
		queryEngine.flushChanges();
		for (var action : storeAdapter.getActions()) {
			action.accept(model);
		}
	}
}
