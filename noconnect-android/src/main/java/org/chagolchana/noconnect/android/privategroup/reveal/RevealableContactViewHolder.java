package org.chagolchana.noconnect.android.privategroup.reveal;

import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;

import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.contact.BaseContactListAdapter.OnContactClickListener;
import org.chagolchana.noconnect.android.contactselection.BaseSelectableContactHolder;

import javax.annotation.Nullable;

import static org.chagolchana.noconnect.android.privategroup.VisibilityHelper.getVisibilityIcon;
import static org.chagolchana.noconnect.android.privategroup.VisibilityHelper.getVisibilityString;
import static org.chagolchana.noconnect.android.util.UiUtils.GREY_OUT;

@UiThread
@NotNullByDefault
class RevealableContactViewHolder
		extends BaseSelectableContactHolder<RevealableContactItem> {

	private final ImageView icon;

	RevealableContactViewHolder(View v) {
		super(v);

		icon = (ImageView) v.findViewById(R.id.visibilityView);
	}

	@Override
	protected void bind(RevealableContactItem item, @Nullable
			OnContactClickListener<RevealableContactItem> listener) {
		super.bind(item, listener);

		icon.setImageResource(getVisibilityIcon(item.getVisibility()));
		info.setText(
				getVisibilityString(info.getContext(), item.getVisibility(),
						item.getContact().getAuthor().getName()));
	}

	@Override
	protected void grayOutItem(boolean gray) {
		super.grayOutItem(gray);
		float alpha = gray ? GREY_OUT : 1f;
		icon.setAlpha(alpha);
	}

}
