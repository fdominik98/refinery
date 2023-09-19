package tools.refinery.store.monitor.internal.model;

public class ClockGreaterThanTimeConstraint extends TimeConstraint{
	public ClockGreaterThanTimeConstraint(Clock clock, int value) {
		super(clock, value);
	}
}
