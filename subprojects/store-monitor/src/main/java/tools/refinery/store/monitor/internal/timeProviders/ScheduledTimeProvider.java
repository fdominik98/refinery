package tools.refinery.store.monitor.internal.timeProviders;

import tools.refinery.store.monitor.internal.timeProviders.AbstractTimeProvider;
import tools.refinery.store.monitor.TimeListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTimeProvider extends AbstractTimeProvider {
	public ScheduledTimeProvider(){
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		executor.scheduleAtFixedRate(() -> {
			time++;
			for(TimeListener listener : listeners) {
				listener.oneUnitPassed(time);
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	private int time = 0;
	@Override
	public int getTime() {
		return time;
	}
}
