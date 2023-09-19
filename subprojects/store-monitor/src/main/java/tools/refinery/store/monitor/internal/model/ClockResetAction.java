package tools.refinery.store.monitor.internal.model;

import java.util.Arrays;
import java.util.List;

public class ClockResetAction {
	public List<Clock> clocksToReset;

	public ClockResetAction(List<Clock> clocksToReset) {
		this.clocksToReset = clocksToReset;
	}

	public ClockResetAction(Clock... clocksToReset) {
		this.clocksToReset = Arrays.stream(clocksToReset).toList();
	}
}
