package org.chagolchana.chagol.plugin;

import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.plugin.BackoffFactory;
import org.chagolchana.chagol.api.plugin.ConnectionManager;
import org.chagolchana.chagol.api.plugin.ConnectionRegistry;
import org.chagolchana.chagol.api.plugin.PluginManager;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.api.system.Scheduler;

import java.security.SecureRandom;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PluginModule {

	public static class EagerSingletons {
		@Inject
		PluginManager pluginManager;
		@Inject
		Poller poller;
	}

	@Provides
	BackoffFactory provideBackoffFactory() {
		return new BackoffFactoryImpl();
	}

	@Provides
	@Singleton
	Poller providePoller(@IoExecutor Executor ioExecutor,
			@Scheduler ScheduledExecutorService scheduler,
			ConnectionManager connectionManager,
			ConnectionRegistry connectionRegistry, PluginManager pluginManager,
			SecureRandom random, Clock clock, EventBus eventBus) {
		Poller poller = new Poller(ioExecutor, scheduler, connectionManager,
				connectionRegistry, pluginManager, random, clock);
		eventBus.addListener(poller);
		return poller;
	}

	@Provides
	@Singleton
	ConnectionManager provideConnectionManager(
			ConnectionManagerImpl connectionManager) {
		return connectionManager;
	}

	@Provides
	@Singleton
	ConnectionRegistry provideConnectionRegistry(
			ConnectionRegistryImpl connectionRegistry) {
		return connectionRegistry;
	}

	@Provides
	@Singleton
	PluginManager providePluginManager(LifecycleManager lifecycleManager,
			PluginManagerImpl pluginManager) {
		lifecycleManager.registerService(pluginManager);
		return pluginManager;
	}
}
