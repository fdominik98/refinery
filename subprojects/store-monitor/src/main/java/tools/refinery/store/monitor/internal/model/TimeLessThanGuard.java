package tools.refinery.store.monitor.internal.model;

import java.util.List;

public class TimeLessThanGuard extends Guard {
	public final int timeSpan;
	public TimeLessThanGuard(int timeSpan){
		super(List.of());
		this.timeSpan = timeSpan;
	}
}
