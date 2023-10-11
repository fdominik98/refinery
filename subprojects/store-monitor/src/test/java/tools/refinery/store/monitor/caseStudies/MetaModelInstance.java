package tools.refinery.store.monitor.caseStudies;

import tools.refinery.store.dse.transition.Rule;
import tools.refinery.store.model.Model;
import tools.refinery.store.representation.Symbol;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaModelInstance {
	public final Symbol<Integer> clockSymbol = Symbol.of("Clock", 0, Integer.class);
	public final List<Symbol<?>> symbols = new ArrayList<>();
	public final List<Symbol<?>> symbolsWithClock = new ArrayList<>();
	public final List<Rule> transformationRules = new ArrayList<>();

	public MetaModelInstance() {
		this.symbolsWithClock.add(clockSymbol);
	}
	public abstract ModelInitializer createInitializer(Model model);
	public abstract AutomatonInstance createAutomaton();

	protected void addSymbol(Symbol<?> symbol) {
		symbols.add(symbol);
		symbolsWithClock.add(symbol);
	}
}