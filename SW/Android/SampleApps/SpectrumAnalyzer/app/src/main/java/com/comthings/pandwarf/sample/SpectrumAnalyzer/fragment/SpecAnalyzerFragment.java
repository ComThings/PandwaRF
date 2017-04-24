package com.comthings.pandwarf.sample.SpectrumAnalyzer.fragment;

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
import com.comthings.pandwarf.specAn.R;
import com.sdsmdg.tastytoast.TastyToast;


public class SpecAnalyzerFragment extends Fragment {
    public static final int MHZ_TO_HZ = 1000000;
    public static final int DEFAULT_INC_KHZ = 25;
    public static final double DEFAULT_BASEFREQ_MHZ = 433;
    public static final int DEFAULT_SPECCHANS = 51;
    public static final int DEFAULT_SPECAN_REFRESH_RATE_MILLIS = 150;

    private final Handler mHandler = new Handler();
    private Runnable mRefreshTimer;
    private ToggleButton buttonStartStop;
    private Button buttonReset;

    private View contentView;

    private TextView textView, frequencyTextView, numberOfchannelsTextView, incrementTextView;

    // Paramètre d'entrées
    double basefreqMHz = DEFAULT_BASEFREQ_MHZ; // Frequence de base (MHz)
    int specchans = DEFAULT_SPECCHANS; // Nombres de channels
    int inckHz = DEFAULT_INC_KHZ; // Difference entre les channels (kHz)

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

        textView = (TextView) contentView.findViewById(R.id.resu);
        frequencyTextView = (TextView) contentView.findViewById(R.id.frequency);
        frequencyTextView.setText("Start Frequency: " + basefreqMHz + " MHz");
        numberOfchannelsTextView = (TextView) contentView.findViewById(R.id.channels);
        numberOfchannelsTextView.setText("Number of channels: " + specchans + " Hz");
        incrementTextView = (TextView) contentView.findViewById(R.id.incrementt);
        incrementTextView.setText("Increment: " + inckHz + " Hz");


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
                            //reset CC1111 to reset RF params because Specan modified them
                            GollumDongle.getInstance(getActivity()).hardResetChip(0, null);
                            // Re-enter normal TX mode, (acknowledged mode) to enable retransmission of lost packets
                            GollumDongle.getInstance(getActivity()).writeTxRetryMode(true);
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
        // Vérifie la validité des valeurs editText et empêche le start en cas de problème
        textView.setText("");
        GollumDongle.getInstance(getActivity()).rfSpecanStart(0, (int) (basefreqMHz * MHZ_TO_HZ), inckHz * 1000, specchans, DEFAULT_SPECAN_REFRESH_RATE_MILLIS, new GollumCallbackGetInteger() {
            @Override
            public void done(int integer) {
                // Enter in "UDP" like mode, as there is no interest in retransmiting old packets
                GollumDongle.getInstance(getActivity()).writeTxRetryMode(false);

                mRefreshTimer = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get RSSI values
                            byte[] rssi_buffer = new byte[specchans];
                            int numRssiMeas = GollumDongle.getInstance(getActivity()).rfSpecanGetRssi(rssi_buffer, specchans); // With Real Data
                            if (numRssiMeas == specchans) {
                                if (rssi_buffer.length == specchans) {
                                    textView.append(Utils.byteArrayToHexString(rssi_buffer) + "\n" + "\n");
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