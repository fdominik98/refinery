package tools.refinery.store.monitor.internal.model;

public abstract class TimeConstraint {
	public final Clock clock;
	public final int timeSpan;

	public TimeConstraint(Clock clock, int timeSpan) {
		this.clock = clock;
		this.timeSpan = timeSpan;
	}
}
