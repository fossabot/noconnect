package org.chagolchana.noconnect.android.navdrawer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.plugin.BluetoothConstants;
import org.chagolchana.chagol.api.plugin.LanTcpConstants;
import org.chagolchana.chagol.api.plugin.TorConstants;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.activity.BriarActivity;
import org.chagolchana.noconnect.android.blog.FeedFragment;
import org.chagolchana.noconnect.android.contact.ContactListFragment;
import org.chagolchana.noconnect.android.controller.handler.UiResultHandler;
import org.chagolchana.noconnect.android.forum.ForumListFragment;
import org.chagolchana.noconnect.android.fragment.BaseFragment;
import org.chagolchana.noconnect.android.fragment.BaseFragment.BaseFragmentListener;
import org.chagolchana.noconnect.android.fragment.SignOutFragment;
import org.chagolchana.noconnect.android.privategroup.list.GroupListFragment;
import org.chagolchana.noconnect.android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.chagolchana.noconnect.android.util.UiUtils.getDaysUntilExpiry;

public class NavDrawerActivity extends BriarActivity implements
		BaseFragmentListener, TransportStateListener,
		OnNavigationItemSelectedListener {

	public static final String INTENT_CONTACTS = "intent_contacts";
	public static final String INTENT_GROUPS = "intent_groups";
	public static final String INTENT_FORUMS = "intent_forums";
	public static final String INTENT_BLOGS = "intent_blogs";

	private static final Logger LOG =
			Logger.getLogger(NavDrawerActivity.class.getName());

	private ActionBarDrawerToggle drawerToggle;

	@Inject
	NavDrawerController controller;

	private DrawerLayout drawerLayout;
	private NavigationView navigation;

	private List<Transport> transports;
	private BaseAdapter transportsAdapter;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		exitIfStartupFailed(intent);
		// TODO don't create new instances if they are on the stack (#606)
		if (intent.getBooleanExtra(INTENT_GROUPS, false)) {
			startFragment(GroupListFragment.newInstance(), R.id.nav_btn_groups);
		} else if (intent.getBooleanExtra(INTENT_FORUMS, false)) {
			startFragment(ForumListFragment.newInstance(), R.id.nav_btn_forums);
		} else if (intent.getBooleanExtra(INTENT_CONTACTS, false)) {
			startFragment(ContactListFragment.newInstance(), R.id.nav_btn_contacts);
		} else if (intent.getBooleanExtra(INTENT_BLOGS, false)) {
			startFragment(FeedFragment.newInstance(), R.id.nav_btn_blogs);
		}
		setIntent(null);
	}

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		exitIfStartupFailed(getIntent());
		setContentView(R.layout.activity_nav_drawer);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		navigation = (NavigationView) findViewById(R.id.navigation);
		GridView transportsView = (GridView) findViewById(R.id.transportsView);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
				R.string.nav_drawer_open_description,
				R.string.nav_drawer_close_description);
		drawerLayout.addDrawerListener(drawerToggle);
		navigation.setNavigationItemSelectedListener(this);

		initializeTransports(getLayoutInflater());
		transportsView.setAdapter(transportsAdapter);

		if (state == null) {
			startFragment(ContactListFragment.newInstance(), R.id.nav_btn_contacts);
		}
		if (getIntent() != null) {
			onNewIntent(getIntent());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		updateTransports();
		controller.showExpiryWarning(new UiResultHandler<Boolean>(this) {
			@Override
			public void onResultUi(Boolean showWarning) {
				if (showWarning) showExpiryWarning();
			}
		});
	}

	private void exitIfStartupFailed(Intent intent) {
		if (intent.getBooleanExtra(KEY_STARTUP_FAILED, false)) {
			finish();
			LOG.info("Exiting");
			System.exit(0);
		}
	}

	private void loadFragment(int fragmentId) {
		// TODO re-use fragments from the manager when possible (#606)
		switch (fragmentId) {
			case R.id.nav_btn_contacts:
				startFragment(ContactListFragment.newInstance());
				break;
			case R.id.nav_btn_groups:
				startFragment(GroupListFragment.newInstance());
				break;
			case R.id.nav_btn_forums:
				startFragment(ForumListFragment.newInstance());
				break;
			case R.id.nav_btn_blogs:
				startFragment(FeedFragment.newInstance());
				break;
			case R.id.nav_btn_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.nav_btn_signout:
				signOut();
				break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		drawerLayout.closeDrawer(START);
		clearBackStack();
		loadFragment(item.getItemId());
		//Don't display the Settings Item as checked
        return item.getItemId() != R.id.nav_btn_settings;
    }

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(START)) {
			drawerLayout.closeDrawer(START);
		} else if (getSupportFragmentManager().getBackStackEntryCount() == 0 &&
				getSupportFragmentManager()
						.findFragmentByTag(ContactListFragment.TAG) != null) {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else if (getSupportFragmentManager().getBackStackEntryCount() == 0 &&
				getSupportFragmentManager()
						.findFragmentByTag(ContactListFragment.TAG) == null) {
			/*
			 * This Makes sure that the first fragment (ContactListFragment) the
			 * user sees is the same as the last fragment the user sees before
			 * exiting. This models the typical Google navigation behaviour such
			 * as in Gmail/Inbox.
			 */
			startFragment(ContactListFragment.newInstance(), R.id.nav_btn_contacts);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	private void signOut() {
		drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
		startFragment(new SignOutFragment());
		signOut(false);
	}

	private void startFragment(BaseFragment fragment, int itemId){
		navigation.setCheckedItem(itemId);
		startFragment(fragment);
	}

	private void startFragment(BaseFragment fragment) {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0)
			startFragment(fragment, false);
		else startFragment(fragment, true);
	}

	private void startFragment(BaseFragment fragment,
			boolean isAddedToBackStack) {
		FragmentTransaction trans =
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.fade_in,
								R.anim.fade_out, R.anim.fade_in,
								R.anim.fade_out)
						.replace(R.id.fragmentContainer, fragment,
								fragment.getUniqueTag());
		if (isAddedToBackStack) {
			trans.addToBackStack(fragment.getUniqueTag());
		}
		trans.commit();
	}

	private void clearBackStack() {
		getSupportFragmentManager().popBackStackImmediate(null,
				POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public void handleDbException(DbException e) {
		// Do nothing for now
	}

	@SuppressWarnings("ConstantConditions")
	private void showExpiryWarning() {
		int daysUntilExpiry = getDaysUntilExpiry();
		if (daysUntilExpiry < 0) signOut();

		// show expiry warning text
		final ViewGroup
				expiryWarning = (ViewGroup) findViewById(R.id.expiryWarning);
		TextView expiryWarningText =
				(TextView) expiryWarning.findViewById(R.id.expiryWarningText);
		expiryWarningText.setText(getResources()
				.getQuantityString(R.plurals.expiry_warning, daysUntilExpiry,
						daysUntilExpiry));

		// make close button functional
		ImageView expiryWarningClose =
				(ImageView) expiryWarning.findViewById(R.id.expiryWarningClose);
		expiryWarningClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				controller.expiryWarningDismissed();
				expiryWarning.setVisibility(GONE);
			}
		});

		expiryWarning.setVisibility(VISIBLE);
	}

	private void initializeTransports(final LayoutInflater inflater) {
		transports = new ArrayList<>(3);

		Transport tor = new Transport();
		tor.id = TorConstants.ID;
		tor.enabled = controller.isTransportRunning(tor.id);
		tor.iconId = R.drawable.transport_tor;
		tor.textId = R.string.transport_tor;
		transports.add(tor);

		Transport bt = new Transport();
		bt.id = BluetoothConstants.ID;
		bt.enabled = controller.isTransportRunning(bt.id);
		bt.iconId = R.drawable.transport_bt;
		bt.textId = R.string.transport_bt;
		transports.add(bt);

		Transport lan = new Transport();
		lan.id = LanTcpConstants.ID;
		lan.enabled = controller.isTransportRunning(lan.id);
		lan.iconId = R.drawable.transport_lan;
		lan.textId = R.string.transport_lan;
		transports.add(lan);

		transportsAdapter = new BaseAdapter() {
			@Override
			public int getCount() {
				return transports.size();
			}

			@Override
			public Transport getItem(int position) {
				return transports.get(position);
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View view;
				if (convertView != null) {
					view = convertView;
				} else {
					view = inflater.inflate(R.layout.list_item_transport,
							parent, false);
				}

				Transport t = getItem(position);
				int c;
				if (t.enabled) {
					c = ContextCompat.getColor(NavDrawerActivity.this,
							R.color.briar_green_light);
				} else {
					c = ContextCompat.getColor(NavDrawerActivity.this,
							android.R.color.tertiary_text_light);
				}

				ImageView icon = (ImageView) view.findViewById(R.id.imageView);
				icon.setImageDrawable(ContextCompat
						.getDrawable(NavDrawerActivity.this, t.iconId));
				icon.setColorFilter(c);

				TextView text = (TextView) view.findViewById(R.id.textView);
				text.setText(getString(t.textId));

				return view;
			}
		};
	}

	private void setTransport(final TransportId id, final boolean enabled) {
		runOnUiThreadUnlessDestroyed(new Runnable() {
			@Override
			public void run() {
				if (transports == null || transportsAdapter == null) return;
				for (Transport t : transports) {
					if (t.id.equals(id)) {
						t.enabled = enabled;
						transportsAdapter.notifyDataSetChanged();
						break;
					}
				}
			}
		});
	}

	private void updateTransports() {
		if (transports == null || transportsAdapter == null) return;
		for (Transport t : transports) {
			t.enabled = controller.isTransportRunning(t.id);
		}
		transportsAdapter.notifyDataSetChanged();
	}

	@Override
	public void stateUpdate(TransportId id, boolean enabled) {
		setTransport(id, enabled);
	}

	private static class Transport {

		private TransportId id;
		private boolean enabled;
		private int iconId;
		private int textId;
	}
}
