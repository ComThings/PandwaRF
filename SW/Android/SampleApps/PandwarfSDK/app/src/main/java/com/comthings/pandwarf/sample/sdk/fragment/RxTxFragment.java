package com.comthings.pandwarf.sample.sdk.fragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.gollum.api.gollumandroidlib.callback.GollumCallbackGetInteger;
import com.comthings.gollum.api.gollumandroidlib.utils.Utils;
import com.comthings.pandwarf.sample.sdk.R;
import com.sdsmdg.tastytoast.TastyToast;

/**
 * @author Djamil
 */

public class RxTxFragment extends Fragment implements OnClickListener {

	private static final String TAG = "RxTxFrag";
	private static final int RX_PACKET_SIZE = 250;
	private static final int FRAME_LENGTH_DEFAULT_VALUE_BYTE = 52;  // 52 bytes
	private static int CHANNEL_FILTER_BANDWIDTH_HZ = 75000;		// Receiver Channel Filter Bandwidth to use (75 KHz)
	private ToggleButton button_Xmit, button_Listen;
	private Button button_Clear_Data;
	private static TextView dataDisplayResultTextView, frequencyTextView, modulationTextView, dataRateTextView;
	public static RadioTask ongoingRadioTask;
	private AsyncRadioRxTxTask mRxTxTask;

	static int freq = 433913879;    // Frequency to use
	static int drate = 3200;    // Data rate to use
	static int mod = 0x30;        // Modulation  to use
	static int frameLength = FRAME_LENGTH_DEFAULT_VALUE_BYTE; // Size of the CC1111 RX data frame (payload only)

	static byte[] bufferHex = new byte[RX_PACKET_SIZE];    // Hex buffer : range [0x00 to 0xFF]
	static int rx_size = 0; //Size of the bufferHex[] array
	static int rx_bytes_read = 0;    // Total RX bytes read
	static int requested_rx_bytes = 50;
	boolean rxDone = false;

	private View contentView;

	private enum RadioTask {
		RADIO_TX,
		RADIO_RX,
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");
		// Inflate the layout for this fragment
		contentView = inflater.inflate(R.layout.fragment_rx_tx, container, false);

		button_Listen = (ToggleButton) contentView.findViewById(R.id.listen_Button);
		button_Listen.setOnClickListener(this);
		button_Xmit = (ToggleButton) contentView.findViewById(R.id.xmit_Button);
		button_Xmit.setOnClickListener(this);

		button_Clear_Data = (Button) contentView.findViewById(R.id.clear_data_Button);
		button_Clear_Data.setOnClickListener(this);

		frequencyTextView = (TextView)contentView.findViewById(R.id.frequency);
		appendColoredText(frequencyTextView,freq+" Hz", Color.WHITE);

		modulationTextView = (TextView)contentView.findViewById(R.id.modulation);
		appendColoredText(modulationTextView,"ASK/OOK", Color.WHITE);

		dataRateTextView = (TextView)contentView.findViewById(R.id.datarate);
		appendColoredText(dataRateTextView,drate+" Bits/s", Color.WHITE);

		dataDisplayResultTextView = (TextView) contentView.findViewById(R.id.display_Result_Text);
		dataDisplayResultTextView.setMovementMethod(new ScrollingMovementMethod());

		// Intercept touch event before the parent scrollview got the event
		dataDisplayResultTextView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.getParent().requestDisallowInterceptTouchEvent(true);
				// listener has NOT consumed the event
				return false;
			}
		});

		return contentView;
	}

	public static void appendColoredText(TextView textView, String text, int color) {
		int start = textView.getText().length();
		textView.append(text);
		int end = textView.getText().length();

		Spannable spannableText = (Spannable) textView.getText();
		spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the RX/TX task
		cancelRxTxTask();
	}

	public void onClick(View v) {
		if (v == button_Listen) {
			Log.v(TAG, "RX Button clicked");

			if (button_Listen.isChecked()) {
				//Cancel the RX/TX task if already ongoing
				if (mRxTxTask != null) {
					cancelRxTxTask();
				}
				// Execute in parallel
				mRxTxTask = new AsyncRadioRxTxTask();
				mRxTxTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, RadioTask.RADIO_RX);
			} else {
				// Stop the RX/TX task
				cancelRxTxTask();
			}
		}

		if (v == button_Xmit) {
			if (button_Xmit.isChecked()) {
				if (dataDisplayResultTextView.getText().toString().length() == 0) {
					button_Xmit.setChecked(false);

					TastyToast.makeText(getContext(), "TX buffer empty", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
					return;
				}

				// Execute in parallel
				mRxTxTask = new AsyncRadioRxTxTask();
				mRxTxTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, RadioTask.RADIO_TX);
			} else {
				cancelRxTxTask();
			}
		}

		if (v == button_Clear_Data) {
			dataDisplayResultTextView.setText("");//clear the buffer area before to display new buffer
		}
	}

	private void cancelRxTxTask() {
		if (mRxTxTask != null) {
			mRxTxTask.cancel(true);
			mRxTxTask = null;
			Log.v(TAG, "AsyncRadioRxTxTask stopped");
		} else {
			Log.v(TAG, "AsyncRadioRxTxTask not stopped because not started");
		}
	}


	public void displayReceivedData() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				final String bufferHexString = Utils.byteArrayToHexString(bufferHex, rx_size);
				dataDisplayResultTextView.append(bufferHexString);
			}
		});
	}

	private class AsyncRadioRxTxTask extends AsyncTask<RadioTask, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(RadioTask... RxTx) {
			if (RxTx[0] == RadioTask.RADIO_RX) {
				rx_bytes_read = 0;
				rx_size = 0;

				// RX Setup phase - once
				ongoingRadioTask = RadioTask.RADIO_RX;

				// Use rxSetup() version which is executed in the same context, not background task
				GollumDongle.getInstance(getActivity()).rxSetup(freq, mod, drate, frameLength, CHANNEL_FILTER_BANDWIDTH_HZ);

				publishProgress();

				// Listen phase - until satisfaction
				while ((rx_bytes_read < requested_rx_bytes) && !isCancelled()) {
					rx_size = GollumDongle.getInstance(getActivity()).rxListen(bufferHex, frameLength);
					if (rx_size > 0) {
						try {
							rx_bytes_read += rx_size;
						} catch (IndexOutOfBoundsException e) {
							e.printStackTrace();

							return null;
						}

						publishProgress();
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}
			} else if (RxTx[0] == RadioTask.RADIO_TX) {
				// TX Setup phase - once
				ongoingRadioTask = RadioTask.RADIO_TX;

				GollumDongle.getInstance(getActivity()).txSetup(freq, mod, drate);

				// TX phase - once
				String data = dataDisplayResultTextView.getText().toString();
				GollumDongle.getInstance(getActivity()).txSend(data.getBytes(), data.getBytes().length / 2, true);
			}

			return null;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			displayReceivedData();
		}

		private void endTask() {
			Log.d(TAG, "endTask");
			if (ongoingRadioTask == RadioTask.RADIO_RX) {
				rxDone = true;
				GollumDongle.getInstance(getActivity()).rxStop(new GollumCallbackGetInteger() {
					@Override
					public void done(int result) {
						button_Listen.setChecked(false);
					}
				});
				dataDisplayResultTextView.append("\n");
			} else if (ongoingRadioTask == RadioTask.RADIO_TX) {
				button_Xmit.setChecked(false);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "onPostExecute()");

			endTask();
		}

		@Override
		protected void onCancelled(Void aVoid) {
			Log.d(TAG, "onCancelled()");

			endTask();
		}
	}
}
