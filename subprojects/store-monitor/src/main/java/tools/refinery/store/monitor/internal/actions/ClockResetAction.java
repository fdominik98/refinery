package tools.refinery.store.monitor.internal.actions;

import tools.refinery.store.monitor.internal.model.Clock;
import tools.refinery.store.monitor.internal.model.ClockHolder;

import java.util.Arrays;
import java.util.List;

public class ClockResetAction extends TransitionAction{
	public List<Clock> clocksToReset;

	public ClockResetAction(List<Clock> clocksToReset) {
		this.clocksToReset = clocksToReset;
	}

	public ClockResetAction(Clock... clocksToReset) {
		this.clocksToReset = Arrays.stream(clocksToReset).toList();
	}

	@Override
	public void execute(ClockHolder value, int now) {
		value.reset(clocksToReset, now);
	}
}
