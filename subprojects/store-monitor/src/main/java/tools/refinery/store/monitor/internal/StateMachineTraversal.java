package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.store.representation.Symbol;

import java.util.*;

public class StateMachineTraversal {
	public final SymbolHolder symbolHolder;

	public StateMachineTraversal(State startState){
		symbolHolder = new SymbolHolder(startState);
		getStateMap(startState);
	}

	private static class BFSNode {
		State state;
		List<Parameter> parameters;

		public BFSNode(State state, List<Parameter> parameters) {
			this.state = state;
			this.parameters = parameters;
		}
	}

	private void getStateMap(State startState) {
		Queue<BFSNode> queue = new LinkedList<>();

		// Starting node
		queue.add(new BFSNode(startState, new ArrayList<>()));

		while (!queue.isEmpty()) {
			BFSNode currentNode = queue.poll();

			// Generate string representation
			String representation = currentNode.state.toString() + currentNode.parameters.toString();

			var symbol = new StateSymbol(Symbol.of(representation, currentNode.parameters.size(), Integer.class,
					null));

			symbolHolder.put(currentNode.state, new ArrayList<>(currentNode.parameters), symbol);

			// Process outgoing transitions
			for (Transition transition : currentNode.state.transitionsOut) {
				List<Parameter> newParamsList = new ArrayList<>(currentNode.parameters);
				for(Parameter p : transition.parameters) {
					if(!newParamsList.contains(p)) {
						newParamsList.add(p);
					}
				}
				if (symbolHolder.containsKey(transition.to, newParamsList)){
					continue;
				}
				queue.add(new BFSNode(transition.to, newParamsList));
			}
		}
	}
}
