package tools.refinery.store.monitor.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClockHolder extends HashMap<Clock, Integer>{

	public ClockHolder(ClockHolder clockHolder) {
		super(clockHolder);
	}

	public ClockHolder() {
		super();
	}

	public void reset(int now){
		for(var key : keySet()) {
			put(key, now);
		}
	}

	public void reset(List<Clock> clocksToReset, int now){
		for (Clock c : clocksToReset) {
			if (containsKey(c)) {
				put(c, now);
			}
		}
	}
}
