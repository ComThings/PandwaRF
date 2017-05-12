package com.comthings.pandwarf.sample.sdk.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.gollum.api.gollumandroidlib.callback.GollumCallbackGetInteger;
import com.comthings.gollum.api.gollumandroidlib.utils.Utils;
import com.comthings.pandwarf.sample.sdk.R;
import com.sdsmdg.tastytoast.TastyToast;


public class SpecAnalyzerFragment extends Fragment {
	public static final int DEFAULT_SPECAN_REFRESH_RATE_MILLIS = 150;

	private final Handler mHandler = new Handler();
	private Runnable mRefreshTimer;
	private ToggleButton buttonStartStop;
	private Button buttonReset;
	private TextView textView, frequencyTextView, numberOfchannelsTextView, incrementTextView;

	private View contentView;


	// Input Parameters
	int basefreqHz = 433000000; // Base Frequency (Hz)
	int numChannels = 51; // Number of channels
	int channelIncrementHz = 25000; // Channel spacing (Hz)

	public SpecAnalyzerFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.fragment_specanalyser, container, false);

		buttonStartStop = (ToggleButton) contentView.findViewById(R.id.toggleButtonSpectrum);
		buttonReset = (Button) contentView.findViewById(R.id.buttonReset);

		textView = (TextView) contentView.findViewById(R.id.rssi_results);
		frequencyTextView = (TextView) contentView.findViewById(R.id.frequency);
		frequencyTextView.setText("Start Frequency: " + basefreqHz + " Hz");
		numberOfchannelsTextView = (TextView) contentView.findViewById(R.id.channels);
		numberOfchannelsTextView.setText("Number of channels: " + numChannels);
		incrementTextView = (TextView) contentView.findViewById(R.id.incrementt);
		incrementTextView.setText("Channel spacing: " + channelIncrementHz + " Hz");


		/**
		 * Reset View Button
		 */
		buttonReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				stopSpecan();
				resetSpecan();
				TastyToast.makeText(getActivity().getApplicationContext(), "Graph reset", TastyToast.LENGTH_LONG, TastyToast.INFO);
			}
		});

		/**
		 * Start/Stop Specan Button is programmatically changed (auto-refresh, ...)
		 */
		buttonStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					startSpecan();
				} else {

					mHandler.removeCallbacks(mRefreshTimer);
					GollumDongle.getInstance(getActivity()).rfSpecanStop(0, new GollumCallbackGetInteger() {
						@Override
						public void done(int integer) {
						}
					});
				}
			}
		});

		// Inflate the layout for this com.comthingsdev.pandwarf.specAn.fragment
		return contentView;
	}

	/**
	 * Stop Specan by causing onCheckedChanged() to be called for buttonStartStop
	 */
	private void stopSpecan() {
		buttonStartStop.setSelected(false);
		buttonStartStop.setChecked(false);
	}


	private void resetSpecan() {
		textView.setText("");
	}

	public void startSpecan() {
		textView.setText("");
		GollumDongle.getInstance(getActivity()).rfSpecanStart(0, basefreqHz, channelIncrementHz, numChannels, DEFAULT_SPECAN_REFRESH_RATE_MILLIS, new GollumCallbackGetInteger() {
			@Override
			public void done(int integer) {
				// Warning: you are responsible for calling rfSpecanGetRssi() in background thread and update the results in UI thread.
				// Below code is only for demo
				mRefreshTimer = new Runnable() {
					@Override
					public void run() {
						try {
							// Get RSSI values
							byte[] rssi_buffer = new byte[numChannels];
							int numRssiMeas = GollumDongle.getInstance(getActivity()).rfSpecanGetRssi(rssi_buffer, numChannels);
							if (numRssiMeas == numChannels) {
								if (rssi_buffer.length == numChannels) {
									textView.append(Utils.byteArrayToHexString(rssi_buffer) + "\n");
								}
							}
							mHandler.postDelayed(this, DEFAULT_SPECAN_REFRESH_RATE_MILLIS);
						} catch (Exception e) {
							e.printStackTrace();
							mHandler.removeCallbacks(mRefreshTimer);
							stopSpecan();
						}
					}
				};
				// Initial Kick off of the SpecAnalyzerFragment
				mHandler.postDelayed(mRefreshTimer, DEFAULT_SPECAN_REFRESH_RATE_MILLIS);
			}
		});
	}
}