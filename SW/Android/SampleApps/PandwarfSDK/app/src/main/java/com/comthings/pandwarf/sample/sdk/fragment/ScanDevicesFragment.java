package com.comthings.pandwarf.sample.sdk.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.ToggleButton;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.pandwarf.sample.sdk.R;
import com.comthings.pandwarf.sample.sdk.utils.ControllerBleDevice;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import no.nordicsemi.android.nrftoolbox.scanner.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this com.comthingsdev.pandwarf.specAn.fragment must implement the
 * to handle interaction events.
 * create an instance of this com.comthingsdev.pandwarf.specAn.fragment.
 */
public class ScanDevicesFragment extends Fragment implements Observer {
	private static final String TAG = "ScanFragment";
	private static final int INVALID_DEVICE_POSITION = -1;

	private View contentView;

	private ArrayList<ExtendedBluetoothDevice> devices = new ArrayList<ExtendedBluetoothDevice>();
	private ExtendedBluetoothDevice currentDevice = null;
	private int currentDevicePosition = INVALID_DEVICE_POSITION;
	private int previousDevicePosition = INVALID_DEVICE_POSITION;
	private ControllerBleDevice bleManagerCallbacks = null;

	private ToggleButton buttonScanDevices;
	private ProgressBar progressBarScanDevices;
	//Listview
	private ListView deviceListView;
	private SimpleAdapter deviceListAdapter;
	private List<HashMap<String, String>> deviceListItem;

	public boolean bleDeviceConnected = false;           // flag used to track when connected, to not display a disconnect message at startup when bleDeviceDisconnectingByUser is false

	public ScanDevicesFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
	}

	public void setControllerBleDevice(ControllerBleDevice controller) {
		bleManagerCallbacks = controller;
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	private void initListView() {
		deviceListView = (ListView) contentView.findViewById(R.id.list);
		deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				try {
					// Save previous device to be able to update the device in ScanDevicesFragment list when onDeviceDisconnect-ing()/ed() arrive
					previousDevicePosition = currentDevicePosition;
					currentDevice = findBluetoothDeviceByAddress((HashMap<String, String>) deviceListView.getAdapter().getItem(position));
					currentDevicePosition = position;
					// Avoid closing and re-opening same device if we are already connected to it
					if ((currentDevicePosition == previousDevicePosition) && bleDeviceConnected) {

						if (getActivity() != null) {
							TastyToast.makeText(getActivity().getApplicationContext(), getString(R.string.msg_warning_already_connected_to_same_device_skipping), TastyToast.LENGTH_SHORT, TastyToast.WARNING);
						}
						return;
					}

					if (currentDevice == null) {

						if (getActivity() != null) {
							TastyToast.makeText(getActivity().getApplicationContext(), "error", TastyToast.LENGTH_LONG, TastyToast.ERROR);
						}
						return;
					}

					// Stop BLE scanForDevices
					GollumDongle.getInstance(getActivity()).stopSearchDevice();

					GollumDongle.getInstance(getActivity()).openGollumBleDevice(currentDevice, true, false, true, bleManagerCallbacks);

				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		});

		deviceListView.setAdapter(deviceListAdapter);
	}

	private void initModel() {
		deviceListItem = new ArrayList<HashMap<String, String>>();
		deviceListAdapter = new SimpleAdapter(getActivity(), deviceListItem, R.layout.device_item, new String[]{"name", "address"}, new int[]{R.id.name, R.id.address});

	}

	public static void checkBluetoothStatus(Context anActivity) {
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			AlertDialog alertDialog = new AlertDialog.Builder(anActivity).create();
			alertDialog.setTitle("Bluetooth not available");
			alertDialog.setMessage("Your device does not support Bluetooth");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				AlertDialog alertDialog = new AlertDialog.Builder(anActivity).create();
				alertDialog.setTitle("Bluetooth not available");
				alertDialog.setMessage("Do you want to turn on Bluetooth");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								mBluetoothAdapter.enable();
								dialog.dismiss();
							}
						});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				alertDialog.show();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this com.comthingsdev.pandwarf.specAn.fragment
		contentView = inflater.inflate(R.layout.fragment_scan, container, false);
		Log.d(TAG, "onCreateView()");
		initModel();
		updateView();
		progressBarScanDevices = (ProgressBar) contentView.findViewById(R.id.progressBar);
		buttonScanDevices = (ToggleButton) contentView.findViewById(R.id.buttonScan);
		buttonScanDevices.setChecked(false);
		// We use OnClickListener instead of OnCheckedChangeListener because we don't want this listener to be called when there is a programmatic
		// call to buttonScanDevices.setChecked(). Otherwise this leads to spurious call to stopScanForDevices() when onSignalEndScan() is called
		buttonScanDevices.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				boolean isChecked = ((ToggleButton) buttonView).isChecked();

				if (isChecked) {
					// Start BLE ScanDevicesFragment
					scanForDevices();
					checkBluetoothStatus(getActivity());
				} else {
					// Stop BLE ScanDevicesFragment
					stopScanForDevices();
				}
			}
		});

		initListView();
		deviceListView.setVisibility(View.GONE);

		progressBarScanDevices.setVisibility(View.INVISIBLE);

		return contentView;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d(TAG, "onActivityCreated()");

		// Make an initial ScanDevicesFragment as soon as the app start
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ((mBluetoothAdapter != null) && mBluetoothAdapter.isEnabled()) {
			scanForDevices();
		}
	}

	private void updateView() {
		//update the list
		deviceListItem.clear();
		for (final ExtendedBluetoothDevice device : devices) {
			final HashMap<String, String> map = new HashMap<String, String>();

			map.put("name", device.getName());
			map.put("address", device.getAddress());

			deviceListItem.add(map);

		}
		deviceListAdapter.notifyDataSetChanged();

		//display the deviceListView
		if (!deviceListItem.isEmpty()) {
			deviceListView.setVisibility(View.VISIBLE);
		}
	}

	private ExtendedBluetoothDevice findBluetoothDeviceByAddress(HashMap<String, String> map) {
		for (ExtendedBluetoothDevice device : devices) {
			if (map.get("address").equals(device.getAddress())) {
				return device;
			}
		}
		return null;
	}

	void addDevice(ExtendedBluetoothDevice device) {
		devices.add(device);
		updateView();
	}

	private void scanForDevices() {
		progressBarScanDevices.setVisibility(View.VISIBLE);
		buttonScanDevices.setChecked(true);
		devices.clear();

		GollumDongle.getInstance(getActivity()).searchDevice(new ScannerListener() {
			@Override
			public void onSignalNewDevice(final ExtendedBluetoothDevice device) {
				// Check if the com.comthingsdev.pandwarf.specAn.fragment is currently added to its activity
				if (!isAdded() || getActivity() == null) {
					return;
				}

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addDevice(device);
					}
				});

			}

			@Override
			public void onSignalUpdateDevice(ExtendedBluetoothDevice device) {
			}

			@Override
			public void onSignalEndScan(Exception e) {

			}

			@Override
			public void onSignalConnectedDevice(ExtendedBluetoothDevice extendedBluetoothDevice) {

			}
		});
	}

	private void stopScanForDevices() {

		progressBarScanDevices.setVisibility(View.VISIBLE);
		buttonScanDevices.setChecked(false);

		GollumDongle.getInstance(getActivity()).stopSearchDevice();
	}


	@Override
	public void update(Observable o, Object arg) {

		HashMap<String, Object> data = (HashMap<String, Object>) arg;
		String action = (String) data.get("action");

		if (action.equals("onDeviceConnected")) {
			onDeviceConnected();
		} else if (action.equals("onDeviceReady")) {
			onDeviceReady();
		} else if (action.equals("onDeviceDisconnected")) {
			onDeviceDisconnected();
		}
	}

	private void onDeviceConnected() {
		TastyToast.makeText(getActivity().getApplicationContext(), "Connected", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
	}

	private void onDeviceReady() {
		FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new SpecAnalyzerFragment()).addToBackStack(null);
		fragmentTransaction.commit();
		TastyToast.makeText(getActivity().getApplicationContext(), "Ready", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
	}

	private void onDeviceDisconnected() {
		TastyToast.makeText(getActivity().getApplicationContext(), "Disconnected", TastyToast.LENGTH_SHORT, TastyToast.INFO);
	}
}
