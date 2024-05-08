package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.internal.model.*;
import tools.refinery.logic.term.NodeVariable;
import tools.refinery.store.representation.Symbol;
import java.util.*;

public class StateMachineTraversal {
	public final Monitor monitor;

	public StateMachineTraversal(StateMachine stateMachine){
		monitor = new Monitor(stateMachine, stateMachine.clockHolder);
		getStateMap(stateMachine.startState);
	}

	private static class BFSNode {
		State state;
		List<NodeVariable> parameters;

		public BFSNode(State state, List<NodeVariable> parameters) {
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

			var symbol = new StateSymbol(Symbol.of(representation, currentNode.parameters.size(), ClockHolder.class,
					null));

			monitor.put(currentNode.state, new ArrayList<>(currentNode.parameters), symbol);

			// Process outgoing transitions
			for (Transition transition : currentNode.state.transitionsOut) {
				List<NodeVariable> newParamsList = new ArrayList<>(currentNode.parameters);
				for(NodeVariable p : transition.getParameters()) {
					if(!newParamsList.contains(p)) {
						newParamsList.add(p);
					}
				}
				if (monitor.containsKey(transition.to, newParamsList)){
					continue;
				}
				queue.add(new BFSNode(transition.to, newParamsList));
			}
		}
	}
}
