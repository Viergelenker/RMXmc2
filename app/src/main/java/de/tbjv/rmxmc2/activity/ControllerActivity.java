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
import java.util.List;

import de.ccck.rmxmobile.communication.Connection;
import de.ccck.rmxmobile.data_management.DataToComInterface;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;
import eu.esu.mobilecontrol2.sdk.StopButtonFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleScale;

public class ControllerActivity extends AppCompatActivity {

    private static ThrottleFragment throttleFragment;
    private static SeekBar seekBar1;
    private static ThrottleScale throttleScale = new ThrottleScale(10, 127);
    public static Context context;
    private static TextView connectionStatus;
    private static Spinner trainSelector;
    private static boolean fromServer = true;
    private static boolean changedFromUser = false;

    // ErrorThread benötigte Variablen
    private Thread ErrorThread;
    private static boolean active;
    private static List<String> errorList;
    private static TrainSelectorHandler trainSelectorHandler = new TrainSelectorHandler();
    private static ConnectionHandler connectionHandler = new ConnectionHandler();
    private static TrainSpeedHandler trainSpeedHandler = new TrainSpeedHandler();

    private static int currentTrain = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startThread();

        context = this.getApplicationContext();

        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        trainSelector = (Spinner) findViewById(R.id.trainSelector);
        trainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (currentTrain >= 0) {
                    currentTrain = i + 1;

                    // Sets the position of the seekbar and throttle wheel to the running notch of the selected train
                    int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);
                    seekBar1.setProgress(trainSpeed);
                    throttleFragment.moveThrottle(throttleScale.stepToPosition(trainSpeed));
                }
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
        seekBar1.setMax(126); // Maximum of mThrottleScale
        seekBar1.setOnSeekBarChangeListener(onSeekBarChangeListener);

        getSupportFragmentManager().beginTransaction()
                .add(throttleFragment, "mc2:throttle")
                .add(stopButtonFragment, "mc2:stopKey")
                .commit();


    }


    public static void updateTrainSelector() {

        if (!changedFromUser) {
            trainSelectorHandler.sendEmptyMessage(0);
        }
    }

    static class TrainSelectorHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            ArrayList<String> trainList = DataToGuiInterface.generateTrainNameList();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_list_item, trainList);
            //specify the layout to appear list items
            adapter.setDropDownViewResource(R.layout.spinner_list_item);
            //data bind adapter with both spinners
            trainSelector.setAdapter(adapter);
            //set the currentTrain if the list isn't empty
            if (!trainList.isEmpty()) {
                currentTrain = 0;
            }
        }
    }

    public static void updateTrainSpeed(int trainNumber) {

        trainSpeedHandler.sendEmptyMessage(trainNumber);
    }

    static class TrainSpeedHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            if (currentTrain == message.what) {

                int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);
                int maxTrainSpeed = DataToGuiInterface.getMaxRunningNotch(currentTrain);
                float proportionalSpeed = (float) (126.0 / maxTrainSpeed);
                double trainSpeedSetting = Math.ceil(proportionalSpeed * trainSpeed);

                fromServer = true;
                // Sets the position of the seekbar and throttle wheel to the running notch of the selected train

                seekBar1.setProgress((int) trainSpeedSetting);
                throttleFragment.moveThrottle(throttleScale.stepToPosition((int) trainSpeedSetting));
            }
        }
    }

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

    static class ConnectionHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            switch (Integer.valueOf(message.what)) {
                case 0: connectionStatus.setText("Null");
                    break;
                case 1: connectionStatus.setText("Verbinden...");
                    break;
                case 2: connectionStatus.setText("Verbunden");
                    break;
                case 3: connectionStatus.setText("Verbindung trennen...");
                    break;
                case 4: connectionStatus.setText("Nicht verbunden");
                    break;
                default: connectionStatus.setText("Unbekannter Status");
                    break;
            }
        }
    }

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

            // Changes the train direction if the user turns the throttle wheel all the way counter-clockwise
            if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                DataToGuiInterface.setDirection(currentTrain, (byte) 1);
            }
            else DataToGuiInterface.setDirection(currentTrain, (byte) 0);

        }

        @Override
        public void onButtonUp() {

        }

        @Override
        public void onPositionChanged(int position) {
            seekBar1.setProgress(throttleScale.positionToStep(position));

        }
    };

    /**
     * Repositions the throttle wheel if the seekbar/slider on screen is changed
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int position = throttleScale.stepToPosition(progress);

            if (currentTrain >= 0) {

                int maxTrainSpeed = DataToGuiInterface.getMaxRunningNotch(currentTrain);
                float proportionalMaxSpeed = (float) (maxTrainSpeed / 255.0);
                double trainSpeed = Math.ceil(proportionalMaxSpeed * position);

                if (!fromServer) {
                    changedFromUser = true;
                    DataToGuiInterface.setRunningNotch(currentTrain, (int) trainSpeed);
                } else fromServer = false;

            }

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
            while (isActive()) {

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