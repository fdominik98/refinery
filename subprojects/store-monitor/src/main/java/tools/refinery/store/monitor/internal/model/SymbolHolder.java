package tools.refinery.store.monitor.internal.model;

import tools.refinery.store.query.dnf.RelationalQuery;
import tools.refinery.store.query.term.NodeVariable;
import tools.refinery.store.query.term.Variable;
import tools.refinery.store.representation.Symbol;
import java.util.*;

public class SymbolHolder {
	public final ClockHolder clockHolder;
	private final State startState;
	public final List<Symbol<ClockHolder>> symbolList = new ArrayList<>();
	public final List<RelationalQuery> queryList = new ArrayList<>();

	public SymbolHolder(State startState, ClockHolder clockHolder){
		this.startState = startState;
		this.clockHolder = clockHolder;
	}
	private Map<State, Map<List<NodeVariable>, StateSymbol>> symbols = new HashMap<>();

	public Map<List<NodeVariable>, StateSymbol> get(State s) {
		return symbols.get(s);
	}

	public StateSymbol get(State s, List<NodeVariable> ps) {
		return symbols.get(s).get(ps);
	}

	public void put(State s, List<NodeVariable> ps, StateSymbol sts) {
		symbols.putIfAbsent(s, new HashMap<>());
		symbols.get(s).put(ps, sts);
		symbolList.add(sts.symbol);
		queryList.add(sts.getQuery());
	}

	public StateSymbol getStartSymbols(){
		return symbols.get(startState).get(List.of());
	}

	public boolean containsKey(State s, List ps) {
		if (symbols.containsKey(s)){
			if (symbols.get(s).containsKey(ps)) {
				return true;
			}
		}
		return false;
	}
}
