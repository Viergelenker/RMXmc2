package de.tbjv.rmxmc2.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.zagum.switchicon.SwitchIconView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.ccck.rmxmobile.UtilsByte;
import de.ccck.rmxmobile.communication.Connection;
import de.ccck.rmxmobile.data_management.DataToComInterface;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.ccck.rmxmobile.data_management.TrainDepotMap;
import de.ccck.rmxmobile.data_management.TrainObject;
import de.tbjv.rmxmc2.R;
import eu.esu.mobilecontrol2.sdk.MobileControl2;
import eu.esu.mobilecontrol2.sdk.StopButtonFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleScale;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class ControllerActivity extends AppCompatActivity {

    private static ThrottleFragment throttleFragment;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar progressBar;
    private static ThrottleScale throttleScale;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    private static TextView connectionStatus;
    @SuppressLint("StaticFieldLeak")
    private static TextView speed;
    @SuppressLint("StaticFieldLeak")
    private static Spinner trainSelector;
    public static String trainName;
    public static ArrayList<String> trainList;
    public static Map<Integer, String> trainMap;

    private static SwitchIconView switchIconLight;
    private static SwitchIconView switchIconF1;
    private static SwitchIconView switchIconF2;
    private static SwitchIconView switchIconF3;
    private static SwitchIconView switchIconF4;
    private static SwitchIconView switchIconF5;
    private static SwitchIconView switchIconF6;
    private static SwitchIconView switchIconF7;
    private static SwitchIconView switchIconF8;
    private static SwitchIconView switchIconF9;
    private static SwitchIconView switchIconF10;
    private static SwitchIconView switchIconF11;
    private static SwitchIconView switchIconF12;
    private static SwitchIconView switchIconF13;
    private static SwitchIconView switchIconF14;
    private static SwitchIconView switchIconF15;
    private static SwitchIconView switchIconF16;

    private View button_switchLight;
    private View button_switchF1;
    private View button_switchF2;
    private View button_switchF3;
    private View button_switchF4;
    private View button_switchF5;
    private View button_switchF6;
    private View button_switchF7;
    private View button_switchF8;
    private View button_switchF9;
    private View button_switchF10;
    private View button_switchF11;
    private View button_switchF12;
    private View button_switchF13;
    private View button_switchF14;
    private View button_switchF15;
    private View button_switchF16;

    private static StickySwitch switchDirection;

    private String functionMappingString;

    // ErrorThread benötigte Variablen
    private Thread ErrorThread;
    private static boolean active;
    private static List<String> errorList;

    // Handler
    private static TrainSelectorHandler trainSelectorHandler = new TrainSelectorHandler();
    private static ConnectionHandler connectionHandler = new ConnectionHandler();
    private static TrainSpeedHandler trainSpeedHandler = new TrainSpeedHandler();
    private static TrainDirectionHandler trainDirectionHandler = new TrainDirectionHandler();
    private static TrainMode0to7Handler trainMode0to7Handler = new TrainMode0to7Handler();
    private static TrainMode8to15Handler trainMode8to15Handler = new TrainMode8to15Handler();
    private static TrainMode16to23Handler trainMode16to23Handler = new TrainMode16to23Handler();

    public static int currentTrain = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        trainList = DataToGuiInterface.generateTrainNameList();
        trainMap = DataToGuiInterface.getTrainMap();

        startThread();

        context = this.getApplicationContext();

        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        speed = (TextView) findViewById(R.id.speedTextView);
        throttleScale = new ThrottleScale(10, 127);
        trainSelector = (Spinner) findViewById(R.id.trainSelector);
        switchDirection = (StickySwitch) findViewById(R.id.switch_direction);
        trainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (currentTrain >= 0) {
                    currentTrain = i + 1;

                    trainName = trainList.get(currentTrain - 1);

                    for (Map.Entry<Integer, String> entry : DataToGuiInterface.getTrainMap().entrySet()) {
                        if (entry.getValue().equals(trainName)) {
                            currentTrain = entry.getKey();
                        }
                    }

                    // currentTrain key is not equal to the position within the trainDepotMap

                    throttleScale = new ThrottleScale(0, DataToGuiInterface.getMaxRunningNotch(currentTrain) + 1);
                    progressBar.setMax(DataToGuiInterface.getMaxRunningNotch(currentTrain));

                    // Sets the position of the seekbar and throttle wheel to the running notch of the selected train
                    int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);
                    progressBar.setProgress(trainSpeed);
                    speed.setText(String.valueOf(trainSpeed));

                    trainMode0to7Handler.sendEmptyMessage(currentTrain);
                    trainMode8to15Handler.sendEmptyMessage(currentTrain);
                    trainMode16to23Handler.sendEmptyMessage(currentTrain);

                    if (DataToGuiInterface.getDirection(currentTrain) == 1) {
                        switchDirection.setDirection(StickySwitch.Direction.LEFT);
                    } else if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                        switchDirection.setDirection(StickySwitch.Direction.RIGHT);
                    }

                    // Load the current mapping of the selected profile and train
                    SharedPreferences mapping = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
                    // The second string is the value to return if this preference does not exist.
                    functionMappingString = mapping.getString(String.valueOf(currentTrain), "00010203");
                    startRepeatingTask();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // tries to connect to RMX server
        DataToComInterface.deleteAllTrains();
        DataToGuiInterface.connect();

        throttleFragment = ThrottleFragment.newInstance(1);
        throttleFragment.setOnThrottleListener(onThrottleListener);

        StopButtonFragment stopButtonFragment = StopButtonFragment.newInstance();
        stopButtonFragment.setOnStopButtonListener(mStopButtonListener);

        // Set up views
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        getSupportFragmentManager().beginTransaction()
                .add(throttleFragment, "mc2:throttle")
                .add(stopButtonFragment, "mc2:stopKey")
                .commit();

        /*
         * The following Listeners detect button changes, made by the user and synchronizes the changes with the server
         */

        switchDirection.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String text) {

                if (Connection.getConnectionStatus() == 2) {
                    if (switchDirection.getDirection() == StickySwitch.Direction.RIGHT) {
                        DataToGuiInterface.setDirection(currentTrain, (byte) 0);
                    } else DataToGuiInterface.setDirection(currentTrain, (byte) 1);
                } else noConnectionMethod();
            }
        });

        /*
         * listens if a button is pressed
         */
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // button can only be used if rmxmc2 is connected to RMX Server
                if (Connection.getConnectionStatus() == 2) {

                    switch (v.getId()) {
                        case R.id.button_switchF1:

                            byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 1)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                                switchIconF1.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                                switchIconF1.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF2:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 2)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 2));
                                switchIconF2.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 2));
                                switchIconF2.setIconEnabled(true);
                            }

                            break;

                        case R.id.button_switchF3:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 3)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 3));
                                switchIconF3.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 3));
                                switchIconF3.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF4:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 4)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 4));
                                switchIconF4.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 4));
                                switchIconF4.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF5:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 5)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 5));
                                switchIconF5.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 5));
                                switchIconF5.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF6:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 6)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 6));
                                switchIconF6.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 6));
                                switchIconF6.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF7:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 7)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 7));
                                switchIconF7.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 7));
                                switchIconF7.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF8:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 0)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 0));
                                switchIconF8.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 0));
                                switchIconF8.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF9:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 1)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 1));
                                switchIconF9.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 1));
                                switchIconF9.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF10:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 2)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 2));
                                switchIconF10.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 2));
                                switchIconF10.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF11:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 3)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 3));
                                switchIconF11.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 3));
                                switchIconF11.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF12:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 4)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 4));
                                switchIconF12.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 4));
                                switchIconF12.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF13:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 5)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 5));
                                switchIconF13.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 5));
                                switchIconF13.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF14:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 6)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 6));
                                switchIconF14.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 6));
                                switchIconF14.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF15:

                            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 7)) {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 7));
                                switchIconF15.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 7));
                                switchIconF15.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchF16:

                            modeByte = DataToGuiInterface.getModeF16F23(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 0)) {
                                DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToZero(modeByte, 0));
                                switchIconF16.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToOne(modeByte, 0));
                                switchIconF16.setIconEnabled(true);
                            }
                            break;

                        case R.id.button_switchLight:

                            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                            if (UtilsByte.bitIsSet(modeByte, 0)) {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 0));
                                switchIconLight.setIconEnabled(false);
                            } else {
                                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 0));
                                switchIconLight.setIconEnabled(true);
                            }
                            break;

                        default:
                            break;
                    }
                } else noConnectionMethod();

            }
        };

        switchIconF1 = (SwitchIconView) findViewById(R.id.switchIconViewF1);
        button_switchF1 = findViewById(R.id.button_switchF1);
        button_switchF1.setOnClickListener(listener);

        switchIconLight = (SwitchIconView) findViewById(R.id.switchIconViewLight);
        button_switchLight = findViewById(R.id.button_switchLight);
        button_switchLight.setOnClickListener(listener);

        switchIconF2 = (SwitchIconView) findViewById(R.id.switchIconViewF2);
        button_switchF2 = findViewById(R.id.button_switchF2);
        button_switchF2.setOnClickListener(listener);

        switchIconF3 = (SwitchIconView) findViewById(R.id.switchIconViewF3);
        button_switchF3 = findViewById(R.id.button_switchF3);
        button_switchF3.setOnClickListener(listener);

        switchIconF4 = (SwitchIconView) findViewById(R.id.switchIconViewF4);
        button_switchF4 = findViewById(R.id.button_switchF4);
        button_switchF4.setOnClickListener(listener);

        switchIconF5 = (SwitchIconView) findViewById(R.id.switchIconViewF5);
        button_switchF5 = findViewById(R.id.button_switchF5);
        button_switchF5.setOnClickListener(listener);

        switchIconF6 = (SwitchIconView) findViewById(R.id.switchIconViewF6);
        button_switchF6 = findViewById(R.id.button_switchF6);
        button_switchF6.setOnClickListener(listener);

        switchIconF7 = (SwitchIconView) findViewById(R.id.switchIconViewF7);
        button_switchF7 = findViewById(R.id.button_switchF7);
        button_switchF7.setOnClickListener(listener);

        switchIconF8 = (SwitchIconView) findViewById(R.id.switchIconViewF8);
        button_switchF8 = findViewById(R.id.button_switchF8);
        button_switchF8.setOnClickListener(listener);

        switchIconF9 = (SwitchIconView) findViewById(R.id.switchIconViewF9);
        button_switchF9 = findViewById(R.id.button_switchF9);
        button_switchF9.setOnClickListener(listener);

        switchIconF10 = (SwitchIconView) findViewById(R.id.switchIconViewF10);
        button_switchF10 = findViewById(R.id.button_switchF10);
        button_switchF10.setOnClickListener(listener);

        switchIconF11 = (SwitchIconView) findViewById(R.id.switchIconViewF11);
        button_switchF11 = findViewById(R.id.button_switchF11);
        button_switchF11.setOnClickListener(listener);

        switchIconF12 = (SwitchIconView) findViewById(R.id.switchIconViewF12);
        button_switchF12 = findViewById(R.id.button_switchF12);
        button_switchF12.setOnClickListener(listener);

        switchIconF13 = (SwitchIconView) findViewById(R.id.switchIconViewF13);
        button_switchF13 = findViewById(R.id.button_switchF13);
        button_switchF13.setOnClickListener(listener);

        switchIconF14 = (SwitchIconView) findViewById(R.id.switchIconViewF14);
        button_switchF14 = findViewById(R.id.button_switchF14);
        button_switchF14.setOnClickListener(listener);

        switchIconF15 = (SwitchIconView) findViewById(R.id.switchIconViewF15);
        button_switchF15 = findViewById(R.id.button_switchF15);
        button_switchF15.setOnClickListener(listener);

        switchIconF16 = (SwitchIconView) findViewById(R.id.switchIconViewF16);
        button_switchF16 = findViewById(R.id.button_switchF16);
        button_switchF16.setOnClickListener(listener);
    }

    /**
     * Splits the retrieved string and build an array list with it
     *
     * @param functionMappingString actual mapping combination as a string
     * @return functionList
     */
    private List<String> splitMappingStringIntoList(String functionMappingString) {

        List<String> functionList = new ArrayList<>();
        int index = 0;
        while (index < functionMappingString.length()) {
            functionList.add(functionMappingString.substring(index, Math.min(index + 2,
                    functionMappingString.length())));
            index = index + 2;
        }
        return functionList;
    }

    /**
     * gets the mapping (=button) for the given key
     *
     * @param key of the MC2
     */
    private void getMapping(int key) {

        byte modeByte;

        List<String> functionList = splitMappingStringIntoList(functionMappingString);

        int functionValueOfKey = Integer.parseInt(functionList.get(key));

        if (functionValueOfKey < 8) {
            modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
            if (UtilsByte.bitIsSet(modeByte, functionValueOfKey)) {
                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, functionValueOfKey));
            } else {
                DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, functionValueOfKey));
            }
        }
        if (functionValueOfKey > 7 && functionValueOfKey < 16) {
            modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
            if (UtilsByte.bitIsSet(modeByte, functionValueOfKey - 8)) {
                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, functionValueOfKey - 8));
            } else {
                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, functionValueOfKey - 8));
            }
        }
        if (functionValueOfKey == 16) {
            modeByte = DataToGuiInterface.getModeF16F23(currentTrain);
            if (UtilsByte.bitIsSet(modeByte, functionValueOfKey - 16)) {
                DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToZero(modeByte, 0));
            } else {
                DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToOne(modeByte, 0));
            }
        }
        if (functionValueOfKey == 17) {
            if (DataToGuiInterface.getRunningNotch(currentTrain) > 0) {
                DataToGuiInterface.setRunningNotch(currentTrain, DataToGuiInterface.getRunningNotch(currentTrain) - 1);
            }
        }
        if (functionValueOfKey == 18) {
            if (DataToGuiInterface.getRunningNotch(currentTrain) < DataToGuiInterface.getMaxRunningNotch(currentTrain)) {
                DataToGuiInterface.setRunningNotch(currentTrain, DataToGuiInterface.getRunningNotch(currentTrain) + 1);
            }
        }
        if (functionValueOfKey == 19) {
            if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                DataToGuiInterface.setDirection(currentTrain, (byte) 1);
            } else DataToGuiInterface.setDirection(currentTrain, (byte) 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case ThrottleFragment.KEYCODE_THROTTLE_WAKEUP:
                // Ignore the wake up key. You must return true here to avoid further input key handling.
                return true;
            case MobileControl2.KEYCODE_TOP_LEFT:
                getMapping(0);
                return true;
            case MobileControl2.KEYCODE_BOTTOM_LEFT:
                getMapping(1);
                return true;
            case MobileControl2.KEYCODE_TOP_RIGHT:
                getMapping(2);
                return true;
            case MobileControl2.KEYCODE_BOTTOM_RIGHT:
                getMapping(3);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    public static void updateTrainSelector() {

        boolean changedFromUser = false;
        trainSelectorHandler.sendEmptyMessage(0);
    }

    private static class TrainSelectorHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            trainList = DataToGuiInterface.generateTrainNameList();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_list_item, trainList);
            //specify the layout to appear list items
            adapter.setDropDownViewResource(R.layout.spinner_list_item);
            //data bind adapter with both spinners
            trainSelector.setAdapter(adapter);
            //set the currentTrain if the list isn't empty
            if (!trainList.isEmpty()) {
                currentTrain = 0;
                trainName = trainList.get(currentTrain);
            }
        }
    }

    /**
     * moves the ThrottleWheel to correct position (speed) if trainSpeed is different
     */
    public static void moveThrottleWheelIfChanged() {

        if (currentTrain >= 0) {

            int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);

            int throttleWheelPosition = throttleFragment.getLastPosition();

            float segment = 255 / DataToGuiInterface.getMaxRunningNotch(currentTrain);

            float throttleWheelPositionForTrainSpeed = segment * trainSpeed;

            if (throttleWheelPosition > throttleWheelPositionForTrainSpeed + segment || throttleWheelPosition < throttleWheelPositionForTrainSpeed - segment) {
                throttleFragment.moveThrottle(throttleScale.stepToPosition(trainSpeed));
            }
        }
    }

    /**
     * moves the ProgressBar to correct position (speed) if trainSpeed is different
     */
    public static void moveProgressBarIfChanged() {

        if (DataToGuiInterface.getRunningNotch(currentTrain) != progressBar.getProgress()) {
            progressBar.setProgress(DataToGuiInterface.getRunningNotch(currentTrain));
        }
    }

    public static void updateTrainSpeed(int trainNumber) {

        trainSpeedHandler.sendEmptyMessage(trainNumber);
    }

    private static class TrainSpeedHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            if (currentTrain == message.what) {

                int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);

                speed.setText(String.valueOf(trainSpeed));
                moveProgressBarIfChanged();
            }
        }
    }

    // Updates the throttle wheel every 3 seconds
    private final static int INTERVAL = 3000;
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            moveThrottleWheelIfChanged();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask() {
        mHandlerTask.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    /**
     * Update the trainDirection button within the gui
     *
     * @param trainNumber int of the currently selected train
     */
    public static void updateTrainDirection(int trainNumber) {

        trainDirectionHandler.sendEmptyMessage(trainNumber);
    }

    private static class TrainDirectionHandler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {
                if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                    switchDirection.setDirection(StickySwitch.Direction.RIGHT);
                } else {
                    switchDirection.setDirection(StickySwitch.Direction.LEFT);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber int of the currently selected train
     */
    public static void updateTrainMode0to7(int trainNumber) {

        trainMode0to7Handler.sendEmptyMessage(trainNumber);
    }

    private static class TrainMode0to7Handler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {

                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);

                // This is so unbelievable ugly, pls fix it if you find a better solution
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    switchIconLight.setIconEnabled(true);
                } else {
                    switchIconLight.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    switchIconF1.setIconEnabled(true);
                } else {
                    switchIconF1.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    switchIconF2.setIconEnabled(true);
                } else {
                    switchIconF2.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    switchIconF3.setIconEnabled(true);
                } else {
                    switchIconF3.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    switchIconF4.setIconEnabled(true);
                } else {
                    switchIconF4.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    switchIconF5.setIconEnabled(true);
                } else {
                    switchIconF5.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    switchIconF6.setIconEnabled(true);
                } else {
                    switchIconF6.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    switchIconF7.setIconEnabled(true);
                } else {
                    switchIconF7.setIconEnabled(false);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber int of the currently selected train
     */
    public static void updateTrainMode8to15(int trainNumber) {

        trainMode8to15Handler.sendEmptyMessage(trainNumber);
    }

    private static class TrainMode8to15Handler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {

                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);

                // This is so unbelievable ugly, pls fix it if you find a better solution
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    switchIconF8.setIconEnabled(true);
                } else {
                    switchIconF8.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    switchIconF9.setIconEnabled(true);
                } else {
                    switchIconF9.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    switchIconF10.setIconEnabled(true);
                } else {
                    switchIconF10.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    switchIconF11.setIconEnabled(true);
                } else {
                    switchIconF11.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    switchIconF12.setIconEnabled(true);
                } else {
                    switchIconF12.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    switchIconF13.setIconEnabled(true);
                } else {
                    switchIconF13.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    switchIconF14.setIconEnabled(true);
                } else {
                    switchIconF14.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    switchIconF15.setIconEnabled(true);
                } else {
                    switchIconF15.setIconEnabled(false);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber int of the currently selected train
     */
    public static void updateTrainMode16to23(int trainNumber) {

        trainMode16to23Handler.sendEmptyMessage(trainNumber);
    }

    private static class TrainMode16to23Handler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {

                byte modeByte = DataToGuiInterface.getModeF16F23(currentTrain);

                // This is so unbelievable ugly, pls fix it if you find a better solution
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    switchIconF16.setIconEnabled(true);
                } else {
                    switchIconF16.setIconEnabled(false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        stoppThread();
        stopRepeatingTask();
        DataToGuiInterface.terminateThread();
        currentTrain = -1;
        Intent intent = new Intent(ControllerActivity.this,
                MainActivity.class);
        ControllerActivity.this.startActivity(intent);
        finish();
    }

    public static void updateConnectionStatus(int connectionStatus) {
        connectionHandler.sendEmptyMessage(connectionStatus);
    }

    private static class ConnectionHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case 0:
                    connectionStatus.setText(R.string.ConnectNull);
                    break;
                case 1:
                    connectionStatus.setText(R.string.ConnectVerbinden);
                    break;
                case 2:
                    connectionStatus.setText(R.string.ConnectVerbunden);
                    break;
                case 3:
                    connectionStatus.setText(R.string.ConnectTrennen);
                    break;
                case 4:
                    connectionStatus.setText(R.string.ConnectNichtVerbunden);
                    break;
                default:
                    connectionStatus.setText(R.string.ConnectUnbekannt);
                    break;
            }
        }
    }

    private StopButtonFragment.OnStopButtonListener mStopButtonListener = new StopButtonFragment.OnStopButtonListener() {
        @Override
        public void onStopButtonDown() {

            if (Connection.getConnectionStatus() == 2) {
                DataToGuiInterface.sendPanic();
            } else noConnectionMethod();
        }

        @Override
        public void onStopButtonUp() {
            // Don't know yet
        }
    };

    private ThrottleFragment.OnThrottleListener onThrottleListener = new ThrottleFragment.OnThrottleListener() {
        @Override
        public void onButtonDown() {

            if (Connection.getConnectionStatus() == 2) {
                // Changes the train direction if the user turns the throttle wheel all the way counter-clockwise
                if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                    DataToGuiInterface.setDirection(currentTrain, (byte) 1);
                } else DataToGuiInterface.setDirection(currentTrain, (byte) 0);
            } else noConnectionMethod();

        }

        @Override
        public void onButtonUp() {

        }

        @Override
        public void onPositionChanged(int position) {
            if (currentTrain >= 0) {
                DataToGuiInterface.setRunningNotch(currentTrain, throttleScale.positionToStep(position));
            }
        }
    };

    /**
     * starts ErrorThread
     */
    protected void startThread() {

        if (ErrorThread == null) {
            setActive(true);
            ErrorThread = new Thread(new ErrorThreadCreator());
            ErrorThread.start();
        }
    }

    /**
     * stops ErrorThread
     */
    protected void stoppThread() {
        setActive(false);
        ErrorThread = null;
    }

    /**
     * checks if ErrorThread is active
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * sets ErrorThread as active
     *
     * @param active describes whether the error thread is active or not
     */
    public static void setActive(boolean active) {
        ControllerActivity.active = active;
    }

    /**
     * creates an errorList with errors that occure while connection
     *
     * @author Arthur Kaul, Tobias Ilg
     */
    private class ErrorThreadCreator implements Runnable {

        @Override
        public void run() {
            while (isActive()) {

                if (DataToGuiInterface.getErrorList().size() > 0) {
                    noConnectionMethod();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }

    }

    /**
     * handles connection Errors and displays dialog with info
     */
    private void noConnectionMethod() {

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
                                stoppThread();
                                stopRepeatingTask();
                                DataToGuiInterface.terminateThread();
                                currentTrain = -1;
                                Intent intent = new Intent(ControllerActivity.this,
                                        MainActivity.class);
                                ControllerActivity.this.startActivity(intent);
                                finish();

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

    public static List<String> getErrorList() {
        return errorList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_mapping) {
            startActivity(new Intent(ControllerActivity.this, MappingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        // Load the current mapping of the selected profile and train
        SharedPreferences mapping = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
        // The second string is the value to return if this preference does not exist.
        functionMappingString = mapping.getString(String.valueOf(currentTrain), "00010203");
    }
}