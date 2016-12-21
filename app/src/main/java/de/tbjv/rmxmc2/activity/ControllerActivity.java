package de.tbjv.rmxmc2.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ccck.rmxmobile.communication.Connection;
import de.ccck.rmxmobile.data_management.DataToComInterface;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;
import eu.esu.mobilecontrol2.sdk.StopButtonFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleScale;

public class ControllerActivity extends AppCompatActivity {

    private ThrottleFragment throttleFragment;
    private SeekBar seekBar1;
    private ThrottleScale throttleScale = new ThrottleScale(10, 29);
    public static Context context;
    private static TextView textView;
    private static Spinner trainSelector;

    // ErrorThread benötigte Variablen
    private Thread ErrorThread;
    private static boolean active;
    private static List<String> errorList;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;

    private int currentTrain = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startThread();

        context = this.getApplicationContext();

        textView = (TextView) findViewById(R.id.textView);
        trainSelector = (Spinner) findViewById(R.id.trainSelector);
        trainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                currentTrain = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Versucht eine Verbindung herzustellen
        DataToComInterface.deleteAllTrains();
        DataToGuiInterface.connect();

        throttleFragment = ThrottleFragment.newInstance(1);
        throttleFragment.setOnThrottleListener(onThrottleListener);

        StopButtonFragment stopButtonFragment = StopButtonFragment.newInstance();
        stopButtonFragment.setOnStopButtonListener(mStopButtonListener);

        // Set up views
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        seekBar1.setMax(28); // Maximum of mThrottleScale
        seekBar1.setOnSeekBarChangeListener(onSeekBarChangeListener);

        getSupportFragmentManager().beginTransaction()
                .add(throttleFragment, "mc2:throttle")
                .add(stopButtonFragment, "mc2:stopKey")
                .commit();


    }


    public static void updateTrainSelector() {

        trainSelectorHandler.sendEmptyMessage(0);
    }

    private static Handler trainSelectorHandler = new Handler() {

        public  void handleMessage(Message message) {

            ArrayList<String> trainList = DataToGuiInterface.generateTrainNameList();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,trainList);
            //specify the layout to appear list items
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //data bind adapter with both spinners
            trainSelector.setAdapter(adapter);
        }
    };

    @Override
    public void onBackPressed() {

        stoppThread();
        DataToGuiInterface.terminateThread();
        Intent intent = new Intent(ControllerActivity.this,
                MainActivity.class);
        ControllerActivity.this.startActivity(intent);
        finish();
    }

    public static void updateConnectionStatus(int connectionStatus) {
        connectionHandler.sendEmptyMessage(connectionStatus);
    }

    private static Handler connectionHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            textView.setText(String.valueOf(message.what));
        }
    };

    private StopButtonFragment.OnStopButtonListener mStopButtonListener = new StopButtonFragment.OnStopButtonListener() {
        @Override
        public void onStopButtonDown() {
            DataToGuiInterface.sendPanic();
        }

        @Override
        public void onStopButtonUp() {
            // Don't know yet
        }
    };

    private ThrottleFragment.OnThrottleListener onThrottleListener = new ThrottleFragment.OnThrottleListener() {
        @Override
        public void onButtonDown() {
            // Happens when you turn the thottle wheel all the way counter clockwise
        }

        @Override
        public void onButtonUp() {
            // ... and when you release it after
        }

        @Override
        public void onPositionChanged(int position) {
            seekBar1.setProgress(throttleScale.positionToStep(position));

            if (currentTrain >= 0) {
                DataToGuiInterface.setRunningNotch(currentTrain, position);
            }
        }
    };

    /**
     * Repositions the throttle wheel if the seekbar/slider on screen is changed
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int position = throttleScale.stepToPosition(progress);

            if (fromUser) {
                throttleFragment.moveThrottle(position);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * startThread - Startet den ErrorThread
     */
    protected void startThread() {

        if (ErrorThread == null) {
            setActive(true);
            ErrorThread = new Thread(new ErrorThreadCreator());
            ErrorThread.start();
        }
    }

    /**
     * stoppThread - Stopt den ErrorThread
     */
    protected void stoppThread() {
        setActive(false);
        ErrorThread = null;
    }

    /**
     * isActive- ob der ErrorThread grade Active ist oder nicht
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setter für die active vom ErrorThread
     *
     * @param active
     */
    public static void setActive(boolean active) {
        ControllerActivity.active = active;
    }

    /**
     * ErrorThread für ein Verbindungsaufbau Fehler
     *
     * @author Arthur Kaul, Tobias Ilg
     */
    private class ErrorThreadCreator implements Runnable {

        @Override
        public void run() {
            while (isActive() == true) {

                if (DataToGuiInterface.getErrorList().size() > 0) {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            final Dialog dialog = new Dialog(
                                    ControllerActivity.this);
                            dialog.setContentView(R.layout.dialog_error);
                            dialog.setTitle("Verbindungsfehler");
                            dialog.setCanceledOnTouchOutside(false);

                            TextView txtSubTitle = (TextView) dialog
                                    .findViewById(R.id.errorView);
                            txtSubTitle.setText(readArray());
                            Button closeButton = (Button) dialog
                                    .findViewById(R.id.close);
                            closeButton
                                    .setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            ControllerActivity
                                                    .setActive(true);
                                            setActive(true);
                                            dialog.dismiss();

                                        }
                                    });

                            dialog.show();

                        }

                        private CharSequence readArray() {
                            String string = "";
                            errorList = Connection.getErrorList();
                            for (int i = 0; i < getErrorList().size(); i++) {
                                string = string
                                        + (Integer.toString(i + 1) + ": "
                                        + getErrorList().get(i) + System
                                        .getProperty("line.separator"));
                            }
                            Connection.clearErrorList();
                            return string;

                        }
                    });
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }

    }

    public static List<String> getErrorList() {
        return errorList;
    }
}