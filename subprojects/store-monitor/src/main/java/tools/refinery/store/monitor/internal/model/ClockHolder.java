package tools.refinery.store.monitor.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClockHolder{
	public final Map<Clock, Integer> clocks;

	public ClockHolder(ClockHolder clockHolder) {
		this.clocks = new HashMap<>(clockHolder.clocks);
	}

	public ClockHolder() {
		this.clocks = new HashMap<>();
	}

	public void reset(int now){
		for (Clock clock : clocks.keySet()) {
			clocks.put(clock, now);
		}
	}

	public void put(Clock clock, int now) {
		clocks.put(clock, now);
	}

	public int get(Clock clock){
		return clocks.get(clock);
	}

	public void reset(List<Clock> clocksToReset, int now){
		for (Clock c : clocksToReset) {
			if (clocks.containsKey(c)) {
				clocks.put(c, now);
			}
		}
	}

	@Override
	public String toString() {
		return clocks.toString();
	}
}
