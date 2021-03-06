package org.chagolchana.noconnect.android.privategroup.creation;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.chagolchana.chagol.util.StringUtils;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.fragment.BaseFragment;

import static org.chagolchana.noconnect.api.privategroup.PrivateGroupConstants.MAX_GROUP_NAME_LENGTH;

public class CreateGroupFragment extends BaseFragment {

	public final static String TAG = CreateGroupFragment.class.getName();

	private CreateGroupListener listener;
	private EditText nameEntry;
	private Button createGroupButton;
	private TextView feedback;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (CreateGroupListener) context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_create_group, container,
				false);
		nameEntry = (EditText) v.findViewById(R.id.name);
		nameEntry.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int lengthBefore, int lengthAfter) {
				enableOrDisableCreateButton();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		nameEntry.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent e) {
				createGroup();
				return true;
			}
		});

		feedback = (TextView) v.findViewById(R.id.feedback);

		createGroupButton = (Button) v.findViewById(R.id.button);
		createGroupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGroup();
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		listener.showSoftKeyboard(nameEntry);
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	private void enableOrDisableCreateButton() {
		if (createGroupButton == null) return; // Not created yet
		createGroupButton.setEnabled(validateName());
	}

	private boolean validateName() {
		String name = nameEntry.getText().toString();
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_GROUP_NAME_LENGTH) {
			feedback.setText(R.string.name_too_long);
			return false;
		}
		feedback.setText("");
		return length > 0;
	}

	private void createGroup() {
		if (!validateName()) return;
		listener.hideSoftKeyboard(nameEntry);
		listener.onGroupNameChosen(nameEntry.getText().toString());
	}
}
