package tools.refinery.store.monitor.internal.model;

import java.util.List;

public class TimeGreaterThanGuard extends Guard {
	public final int timeSpan;
	public TimeGreaterThanGuard(int timeSpan){
		super(List.of());
		this.timeSpan = timeSpan;
	}
}
