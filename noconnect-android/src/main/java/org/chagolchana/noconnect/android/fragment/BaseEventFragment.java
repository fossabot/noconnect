package org.chagolchana.noconnect.android.fragment;

import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventListener;

import javax.inject.Inject;

public abstract class BaseEventFragment extends BaseFragment implements
		EventListener {

	@Inject
	protected volatile EventBus eventBus;

	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
	}
}
