package tools.refinery.store.monitor.internal.timeProviders;

import tools.refinery.store.monitor.TimeListener;

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
