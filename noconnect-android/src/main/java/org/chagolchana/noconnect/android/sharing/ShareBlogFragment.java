package org.chagolchana.noconnect.android.sharing;

import android.os.Bundle;

import org.chagolchana.chagol.api.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.chagol.api.nullsafety.ParametersNotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.contactselection.ContactSelectorController;
import org.chagolchana.noconnect.android.contactselection.ContactSelectorFragment;
import org.chagolchana.noconnect.android.contactselection.SelectableContactItem;

import javax.inject.Inject;

import static org.chagolchana.noconnect.android.activity.BriarActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ShareBlogFragment extends ContactSelectorFragment {

	public static final String TAG = ShareBlogFragment.class.getName();

	@Inject
	ShareBlogController controller;

	public static ShareBlogFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		ShareBlogFragment fragment = new ShareBlogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	protected ContactSelectorController<SelectableContactItem> getController() {
		return controller;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

}
