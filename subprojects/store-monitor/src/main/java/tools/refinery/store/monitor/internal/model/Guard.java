package tools.refinery.store.monitor.internal.model;

import java.util.List;

public abstract class Guard {
	public final List<Parameter> parameters;
	public Guard(List<Parameter> parameters){
		this.parameters = parameters;
	}
}
