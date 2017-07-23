package org.chagolchana.chagol.system;

import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.api.system.Scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SystemModule {

	public static class EagerSingletons {
		@Inject
		@Scheduler
		ScheduledExecutorService scheduledExecutorService;
	}

	private final ScheduledExecutorService scheduler;

	public SystemModule() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	@Provides
	Clock provideClock() {
		return new SystemClock();
	}

	@Provides
	@Singleton
	@Scheduler
	ScheduledExecutorService provideScheduledExecutorService(
			LifecycleManager lifecycleManager) {
		lifecycleManager.registerForShutdown(scheduler);
		return scheduler;
	}
}
