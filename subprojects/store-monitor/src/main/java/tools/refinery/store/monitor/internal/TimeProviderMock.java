package tools.refinery.store.monitor.internal;

import tools.refinery.store.monitor.AbstractTimeProvider;
import tools.refinery.store.monitor.TimeListener;
import tools.refinery.store.representation.Symbol;

public class TimeProviderMock extends AbstractTimeProvider {
	int time = 0;
	void stepTime(){
		for(TimeListener listener : listeners) {
			listener.oneUnitPassed(++time);
		}
	}

	public void stepTime(int t){
		for(int i = 0; i < t; i++) {
			stepTime();
		}
	}
	@Override
	public int getTime() {
		return time;
	}
}
