package tools.refinery.store.monitor;

public class TimeProviderMock extends AbstractTimeProvider {
	int time = 0;
	void stepTime(){
		for(TimeListener listener : listeners) {
			listener.oneUnitPassed(++time);
		}
	}

	void stepTime(int t){
		for(int i = 0; i < t; i++) {
			stepTime();
		}
	}
	@Override
	public int getTime() {
		return time;
	}
}
