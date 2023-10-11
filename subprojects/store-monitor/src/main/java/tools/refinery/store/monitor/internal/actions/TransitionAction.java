package tools.refinery.store.monitor.internal.actions;

import tools.refinery.store.monitor.internal.model.ClockHolder;

public abstract class TransitionAction {
	public abstract void execute(ClockHolder value, int now);
}
