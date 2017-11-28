package com.comthings.pandwarf.sample.sdk;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.pandwarf.sample.sdk.fragment.JavaScriptFragment;
import com.comthings.pandwarf.sample.sdk.fragment.RxTxFragment;
import com.comthings.pandwarf.sample.sdk.fragment.ScanDevicesFragment;
import com.comthings.pandwarf.sample.sdk.fragment.SpecAnalyzerFragment;
import com.comthings.pandwarf.sample.sdk.utils.ControllerBleDevice;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = "MainActivity";
	private static final boolean KEEP_CONNECTION_IN_BACKGROUND = true;

	FragmentTransaction fragmentTransaction;
	ControllerBleDevice controllerBleDevice;
	ScanDevicesFragment scanDevicesFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		controllerBleDevice = new ControllerBleDevice(this);
		scanDevicesFragment = new ScanDevicesFragment();
		scanDevicesFragment.setControllerBleDevice(controllerBleDevice);
		controllerBleDevice.addObserver(scanDevicesFragment);

		fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment, scanDevicesFragment).addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_scan) {
			// Handle the camera action
			fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment, scanDevicesFragment).addToBackStack(null);
			fragmentTransaction.commit();
		} else if (id == R.id.nav_specAn) {
			fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new SpecAnalyzerFragment()).addToBackStack(null);
			fragmentTransaction.commit();
		} else if (id == R.id.nav_rxtx) {
			fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new RxTxFragment()).addToBackStack(null);
			fragmentTransaction.commit();
		} else if (id == R.id.nav_javascript) {
			fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new JavaScriptFragment()).addToBackStack(null);
			fragmentTransaction.commit();
		} else if (id == R.id.disconnect) {
			GollumDongle.getInstance(this).closeDevice();
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	protected void onDestroy() {
		GollumDongle.getInstance(this).destroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!KEEP_CONNECTION_IN_BACKGROUND) {
			Log.d(TAG, "BLE connection pause, going to background");
			GollumDongle.getInstance(this).pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!KEEP_CONNECTION_IN_BACKGROUND) {
			Log.d(TAG, "Auto reconnect - onResume() - to last BLE mac address");
			GollumDongle.getInstance(this).reconnect("");
		}
	}
}
