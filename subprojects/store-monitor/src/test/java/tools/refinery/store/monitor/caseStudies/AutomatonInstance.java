package tools.refinery.store.monitor.caseStudies;

import tools.refinery.store.monitor.internal.model.StateMachine;

public abstract class AutomatonInstance {
	public final StateMachine stateMachine;

	public AutomatonInstance(double weight)  {
		stateMachine = new StateMachine(weight);
	}
}
