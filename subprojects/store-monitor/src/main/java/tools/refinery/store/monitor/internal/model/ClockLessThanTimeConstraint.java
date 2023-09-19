package tools.refinery.store.monitor.internal.model;

public class ClockLessThanTimeConstraint extends TimeConstraint{
	public ClockLessThanTimeConstraint(Clock clock, int value) {
		super(clock, value);
	}
}
