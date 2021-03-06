package org.chagolchana.noconnect.android.navdrawer;

import android.app.Activity;

import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventListener;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.chagol.api.nullsafety.ParametersNotNullByDefault;
import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.PluginManager;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.event.TransportDisabledEvent;
import org.chagolchana.chagol.api.plugin.event.TransportEnabledEvent;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.noconnect.android.controller.DbControllerImpl;
import org.chagolchana.noconnect.android.controller.handler.ResultHandler;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.chagolchana.noconnect.android.BriarApplication.EXPIRY_DATE;
import static org.chagolchana.noconnect.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class NavDrawerControllerImpl extends DbControllerImpl
		implements NavDrawerController, EventListener {

	private static final Logger LOG =
			Logger.getLogger(NavDrawerControllerImpl.class.getName());
	private static final String EXPIRY_DATE_WARNING = "expiryDateWarning";

	private final PluginManager pluginManager;
	private final SettingsManager settingsManager;
	private final EventBus eventBus;

	private volatile TransportStateListener listener;

	@Inject
	NavDrawerControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, PluginManager pluginManager,
			SettingsManager settingsManager, EventBus eventBus) {
		super(dbExecutor, lifecycleManager);
		this.pluginManager = pluginManager;
		this.settingsManager = settingsManager;
		this.eventBus = eventBus;
	}

	@Override
	public void onActivityCreate(Activity activity) {
		listener = (TransportStateListener) activity;
	}

	@Override
	public void onActivityStart() {
		eventBus.addListener(this);
	}

	@Override
	public void onActivityStop() {
		eventBus.removeListener(this);
	}

	@Override
	public void onActivityDestroy() {

	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof TransportEnabledEvent) {
			TransportId id = ((TransportEnabledEvent) e).getTransportId();
			if (LOG.isLoggable(INFO)) {
				LOG.info("TransportEnabledEvent: " + id.getString());
			}
			transportStateUpdate(id, true);
		} else if (e instanceof TransportDisabledEvent) {
			TransportId id = ((TransportDisabledEvent) e).getTransportId();
			if (LOG.isLoggable(INFO)) {
				LOG.info("TransportDisabledEvent: " + id.getString());
			}
			transportStateUpdate(id, false);
		}
	}

	private void transportStateUpdate(final TransportId id,
			final boolean enabled) {
		listener.runOnUiThreadUnlessDestroyed(new Runnable() {
			@Override
			public void run() {
				listener.stateUpdate(id, enabled);
			}
		});
	}

	@Override
	public void showExpiryWarning(final ResultHandler<Boolean> handler) {
		runOnDbThread(new Runnable() {
			@Override
			public void run() {
				try {
					Settings settings =
							settingsManager.getSettings(SETTINGS_NAMESPACE);
					int warningInt = settings.getInt(EXPIRY_DATE_WARNING, 0);

					if (warningInt == 0) {
						// we have not warned before
						handler.onResult(true);
					} else {
						long warningLong = warningInt * 1000L;
						long now = System.currentTimeMillis();
						long daysSinceLastWarning =
								(now - warningLong) / 1000 / 60 / 60 / 24;
						long daysBeforeExpiry =
								(EXPIRY_DATE - now) / 1000 / 60 / 60 / 24;

						if (daysSinceLastWarning >= 30) {
							handler.onResult(true);
							return;
						}
						if (daysBeforeExpiry <= 3 && daysSinceLastWarning > 0) {
							handler.onResult(true);
							return;
						}
						handler.onResult(false);
					}
				} catch (DbException e) {
					if (LOG.isLoggable(WARNING))
						LOG.log(WARNING, e.toString(), e);
				}
			}
		});
	}

	@Override
	public void expiryWarningDismissed() {
		runOnDbThread(new Runnable() {
			@Override
			public void run() {
				try {
					Settings settings = new Settings();
					int date = (int) (System.currentTimeMillis() / 1000L);
					settings.putInt(EXPIRY_DATE_WARNING, date);
					settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
				} catch (DbException e) {
					if (LOG.isLoggable(WARNING))
						LOG.log(WARNING, e.toString(), e);
				}
			}
		});
	}

	@Override
	public boolean isTransportRunning(TransportId transportId) {
		Plugin plugin = pluginManager.getPlugin(transportId);

		return plugin != null && plugin.isRunning();
	}

}
