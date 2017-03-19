package de.tbjv.rmxmc2.activity;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.zagum.switchicon.SwitchIconView;

import java.util.ArrayList;
import java.util.List;

import de.ccck.rmxmobile.UtilsByte;
import de.ccck.rmxmobile.communication.Connection;
import de.ccck.rmxmobile.data_management.DataToComInterface;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;
import eu.esu.mobilecontrol2.sdk.MobileControl2;
import eu.esu.mobilecontrol2.sdk.StopButtonFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;
import eu.esu.mobilecontrol2.sdk.ThrottleScale;

public class ControllerActivity extends AppCompatActivity {

    private static ThrottleFragment throttleFragment;
    private static SeekBar seekBar1;
    private static ThrottleScale throttleScale = new ThrottleScale(10, 127);
    public static Context context;
    private static TextView connectionStatus;
    private static TextView speed;
    private static Spinner trainSelector;
    private static boolean fromServer = true;
    private static boolean changedFromUser = false;
    private static ToggleButton buttonF1;
    private static ToggleButton buttonF2;
    private static ToggleButton buttonF3;
    private static ToggleButton buttonF4;
    private static ToggleButton buttonF5;
    private static ToggleButton buttonF6;
    private static ToggleButton buttonF7;
    private static ToggleButton buttonF8;
    private static ToggleButton buttonF9;
    private static ToggleButton buttonF10;
    private static ToggleButton buttonF11;
    private static ToggleButton buttonF12;
    private static ToggleButton buttonF13;
    private static ToggleButton buttonF14;
    private static ToggleButton buttonF15;
    private static ToggleButton buttonF16;
    private static ToggleButton directionButton;

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
        speed = (TextView) findViewById(R.id.speedTextView);
        trainSelector = (Spinner) findViewById(R.id.trainSelector);
        trainSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (currentTrain >= 0) {
                    currentTrain = i + 1;

                    // Sets the position of the seekbar and throttle wheel to the running notch of the selected train
                    int trainSpeed = DataToGuiInterface.getRunningNotch(currentTrain);
                    seekBar1.setProgress(trainSpeed);
                    speed.setText(String.valueOf(trainSpeed));
                    throttleFragment.moveThrottle(throttleScale.stepToPosition(trainSpeed));
                    trainMode0to7Handler.sendEmptyMessage(currentTrain);
                    trainMode8to15Handler.sendEmptyMessage(currentTrain);
                    trainMode16to23Handler.sendEmptyMessage(currentTrain);

                    // Load the current mapping of the selected profile and train
                    SharedPreferences mapping = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
                    // The second string is the value to return if this preference does not exist.
                    functionMappingString = mapping.getString(String.valueOf(currentTrain), "00010203");
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

        directionButton = (ToggleButton) findViewById(R.id.directionButton);
        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                    DataToGuiInterface.setDirection(currentTrain, (byte) 1);
                } else DataToGuiInterface.setDirection(currentTrain, (byte) 0);
            }
        });

        // Button onClickListener
        switchIconLight = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchLight = findViewById(R.id.button_switch);
        button_switchLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 0));
                    switchIconLight.setIconEnabled(false);

                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 0));
                    switchIconLight.setIconEnabled(true);
                }

            }
        });

        switchIconF1 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF1 = findViewById(R.id.button_switch);
        button_switchF1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF1.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF1.setIconEnabled(true);
                }
            }
        });

        switchIconF2 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF2 = findViewById(R.id.button_switch);
        button_switchF2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF2.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF2.setIconEnabled(true);
                }
            }
        });

        switchIconF3 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF3 = findViewById(R.id.button_switch);
        button_switchF3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF3.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF3.setIconEnabled(true);
                }
            }
        });

        switchIconF4 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF4 = findViewById(R.id.button_switch);
        button_switchF4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF4.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF4.setIconEnabled(true);
                }
            }
        });

        switchIconF5 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF5 = findViewById(R.id.button_switch);
        button_switchF5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF5.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF5.setIconEnabled(true);
                }
            }
        });

        switchIconF6 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF6 = findViewById(R.id.button_switch);
        button_switchF6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF6.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF6.setIconEnabled(true);
                }
            }
        });

        switchIconF7 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF7 = findViewById(R.id.button_switch);
        button_switchF7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF7.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF7.setIconEnabled(true);
                }
            }
        });

        switchIconF8 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF8 = findViewById(R.id.button_switch);
        button_switchF8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF8.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF8.setIconEnabled(true);
                }
            }
        });
        switchIconF9 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF9 = findViewById(R.id.button_switch);
        button_switchF9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF9.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF9.setIconEnabled(true);
                }
            }
        });

        switchIconF10 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF10 = findViewById(R.id.button_switch);
        button_switchF10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF10.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF10.setIconEnabled(true);
                }
            }
        });
        switchIconF11 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF11 = findViewById(R.id.button_switch);
        button_switchF11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF11.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF11.setIconEnabled(true);
                }
            }
        });
        switchIconF12 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF12 = findViewById(R.id.button_switch);
        button_switchF12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF12.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF12.setIconEnabled(true);
                }
            }
        });
        switchIconF13 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF13 = findViewById(R.id.button_switch);
        button_switchF13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF13.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF13.setIconEnabled(true);
                }
            }
        });
        switchIconF14 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF14 = findViewById(R.id.button_switch);
        button_switchF14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF14.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF14.setIconEnabled(true);
                }
            }
        });

        switchIconF15 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF15 = findViewById(R.id.button_switch);
        button_switchF15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF15.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF15.setIconEnabled(true);
                }
            }
        });

        switchIconF16 = (SwitchIconView) findViewById(R.id.switchIconView1);
        button_switchF16 = findViewById(R.id.button_switch);
        button_switchF16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 1));
                    switchIconF16.setIconEnabled(false);
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 1));
                    switchIconF16.setIconEnabled(true);
                }
            }
        });

        buttonF2 = (ToggleButton) findViewById(R.id.button_F2);
        buttonF2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 2));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 2));
                }
            }
        });

        buttonF3 = (ToggleButton) findViewById(R.id.button_F3);
        buttonF3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 3));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 3));
                }
            }
        });

        buttonF4 = (ToggleButton) findViewById(R.id.button_F4);
        buttonF4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 4));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 4));
                }
            }
        });

        buttonF5 = (ToggleButton) findViewById(R.id.button_F5);
        buttonF5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 5));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 5));
                }
            }
        });

        buttonF6 = (ToggleButton) findViewById(R.id.button_F6);
        buttonF6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 6));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 6));
                }
            }
        });

        buttonF7 = (ToggleButton) findViewById(R.id.button_F7);
        buttonF7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF0F7(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToZero(modeByte, 7));
                } else {
                    DataToGuiInterface.setModeF0F7(currentTrain, UtilsByte.setToOne(modeByte, 7));
                }
            }
        });

        buttonF8 = (ToggleButton) findViewById(R.id.button_F8);
        buttonF8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 0));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 0));
                }
            }
        });

        buttonF9 = (ToggleButton) findViewById(R.id.button_F9);
        buttonF9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 1));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 1));
                }
            }
        });

        buttonF10 = (ToggleButton) findViewById(R.id.button_F10);
        buttonF10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 2));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 2));
                }
            }
        });

        buttonF11 = (ToggleButton) findViewById(R.id.button_F11);
        buttonF11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 3));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 3));
                }
            }
        });

        buttonF12 = (ToggleButton) findViewById(R.id.button_F12);
        buttonF12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 4));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 4));
                }
            }
        });

        buttonF13 = (ToggleButton) findViewById(R.id.button_F13);
        buttonF13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 5));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 5));
                }
            }
        });

        buttonF14 = (ToggleButton) findViewById(R.id.button_F14);
        buttonF14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 6));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 6));
                }
            }
        });

        buttonF15 = (ToggleButton) findViewById(R.id.button_F15);
        buttonF15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, 7));
                } else {
                    DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, 7));
                }
            }
        });

        buttonF16 = (ToggleButton) findViewById(R.id.button_F16);
        buttonF16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte modeByte = DataToGuiInterface.getModeF16F23(currentTrain);
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToZero(modeByte, 0));
                } else {
                    DataToGuiInterface.setModeF16F23(currentTrain, UtilsByte.setToOne(modeByte, 0));
                }
            }
        });
    }

    private void setMapping(int keyToMap, int function) {

        if (currentTrain >= 0) {

            // Add a zero to the function number so the final string has always the same size
            String functionNumberAsString;
            if (function < 10) {
                functionNumberAsString = "0";
                functionNumberAsString = functionNumberAsString + String.valueOf(function);
            } else functionNumberAsString = String.valueOf(function);

            List<String> functionList;
            functionList = splitMappingStringIntoList(functionMappingString);

            // Now the value of the new function is set within the array list, at the corresponding
            // index of the keyToMap
            functionList.set(keyToMap, functionNumberAsString);

            SharedPreferences settings = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(String.valueOf(currentTrain), functionList.toString().replaceAll("\\W", ""));

            // and commit the edits (i used apply() instead of commit, because this way it gets handled
            // in the background whereas commit() blocks the thread)
            editor.apply();

            functionMappingString = functionList.toString().replaceAll("\\W", "");
        }
    }

    private List<String> splitMappingStringIntoList(String functionMappingString) {

        // Split the retrieved string and build an array list with it
        List<String> functionList = new ArrayList<>();
        int index = 0;
        while (index < functionMappingString.length()) {
            functionList.add(functionMappingString.substring(index, Math.min(index + 2,
                    functionMappingString.length())));
            index = index + 2;
        }
        return functionList;
    }

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
            if (UtilsByte.bitIsSet(modeByte, functionValueOfKey)) {
                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToZero(modeByte, functionValueOfKey-8));
            } else {
                DataToGuiInterface.setModeF8F15(currentTrain, UtilsByte.setToOne(modeByte, functionValueOfKey-8));
            }
        }
        if (functionValueOfKey == 16) {
            modeByte = DataToGuiInterface.getModeF16F23(currentTrain);
            if (UtilsByte.bitIsSet(modeByte, functionValueOfKey)) {
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

                speed.setText(String.valueOf(trainSpeed));
                // Sets the position of the seekbar and throttle wheel to the running notch of the selected train
                seekBar1.setProgress((int) trainSpeedSetting);
                throttleFragment.moveThrottle(throttleScale.stepToPosition((int) trainSpeedSetting));
            }
        }
    }

    /**
     * Update the trainDirection button within the gui
     *
     * @param trainNumber
     */
    public static void updateTrainDirection(int trainNumber) {

        trainDirectionHandler.sendEmptyMessage(trainNumber);
    }

    static class TrainDirectionHandler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {
                if (DataToGuiInterface.getDirection(currentTrain) == 0) {
                    directionButton.setChecked(true);
                } else {
                    directionButton.setChecked(false);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber
     */
    public static void updateTrainMode0to7(int trainNumber) {

        trainMode0to7Handler.sendEmptyMessage(trainNumber);
    }

    static class TrainMode0to7Handler extends Handler {

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
                    //buttonF1.setChecked(false);
                    switchIconF1.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    buttonF2.setChecked(true);
                    // switchIconF2.setIconEnabled(true);
                } else {
                    buttonF2.setChecked(false);
                    // switchIconF2.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    buttonF3.setChecked(true);
                    // switchIconF3.setIconEnabled(true);
                } else {
                    buttonF3.setChecked(false);
                    // switchIconF3.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    buttonF4.setChecked(true);
                    // switchIconF4.setIconEnabled(true);
                } else {
                    buttonF4.setChecked(false);
                    // switchIconF4.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    buttonF5.setChecked(true);
                    // switchIconF5.setIconEnabled(true);
                } else {
                    buttonF5.setChecked(false);
                    // switchIconF5.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    buttonF6.setChecked(true);
                    // switchIconF6.setIconEnabled(true);
                } else {
                    buttonF6.setChecked(false);
                    // switchIconF6.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    buttonF7.setChecked(true);
                    // switchIconF7.setIconEnabled(true);
                } else {
                    buttonF7.setChecked(false);
                    // switchIconF7.setIconEnabled(false);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber
     */
    public static void updateTrainMode8to15(int trainNumber) {

        trainMode8to15Handler.sendEmptyMessage(trainNumber);
    }

    static class TrainMode8to15Handler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {

                byte modeByte = DataToGuiInterface.getModeF8F15(currentTrain);

                // This is so unbelievable ugly, pls fix it if you find a better solution
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    buttonF8.setChecked(true);
                    // switchIconF8.setIconEnabled(true);
                } else {
                    buttonF8.setChecked(false);
                    // switchIconF8.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    buttonF9.setChecked(true);
                    // switchIconF9.setIconEnabled(true);
                } else {
                    buttonF9.setChecked(false);
                    // switchIconF9.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    buttonF10.setChecked(true);
                    // switchIconF10.setIconEnabled(true);
                } else {
                    buttonF10.setChecked(false);
                    // switchIconF10.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    buttonF11.setChecked(true);
                    // switchIconF11.setIconEnabled(true);
                } else {
                    buttonF11.setChecked(false);
                    // switchIconF11.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    buttonF12.setChecked(true);
                    // switchIconF12.setIconEnabled(false);
                } else {
                    buttonF12.setChecked(false);
                    // switchIconF12.setIconEnabled(true);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    buttonF13.setChecked(true);
                    // switchIconF13.setIconEnabled(true);
                } else {
                    buttonF13.setChecked(false);
                    // switchIconF13.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    buttonF14.setChecked(true);
                    // switchIconF14.setIconEnabled(true);
                } else {
                    buttonF14.setChecked(false);
                    // switchIconF14.setIconEnabled(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    buttonF15.setChecked(true);
                    // switchIconF15.setIconEnabled(false);
                } else {
                    buttonF15.setChecked(false);
                    // switchIconF15.setIconEnabled(false);
                }
            }
        }
    }

    /**
     * Update the train mode buttons within the gui
     *
     * @param trainNumber
     */
    public static void updateTrainMode16to23(int trainNumber) {

        trainMode16to23Handler.sendEmptyMessage(trainNumber);
    }

    static class TrainMode16to23Handler extends Handler {

        public void handleMessage(Message message) {

            // Only change the direction if its the currently selected train
            if (currentTrain == message.what) {

                byte modeByte = DataToGuiInterface.getModeF16F23(currentTrain);

                // This is so unbelievable ugly, pls fix it if you find a better solution
                if (UtilsByte.bitIsSet(modeByte, 0)) {
                    buttonF16.setChecked(true);
                    // switchIconF16.setIconEnabled(true);
                } else {
                    buttonF16.setChecked(false);
                    // switchIconF16.setIconEnabled(false);
                }
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
                case 0:
                    connectionStatus.setText("Null");
                    break;
                case 1:
                    connectionStatus.setText("Verbinden...");
                    break;
                case 2:
                    connectionStatus.setText("Verbunden");
                    break;
                case 3:
                    connectionStatus.setText("Verbindung trennen...");
                    break;
                case 4:
                    connectionStatus.setText("Nicht verbunden");
                    break;
                default:
                    connectionStatus.setText("Unbekannter Status");
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
            } else DataToGuiInterface.setDirection(currentTrain, (byte) 0);

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
                    String speedString = String.valueOf((int) trainSpeed);
                    speed.setText(speedString);
                } else fromServer = false;

            }

            /* Not working with the current api level
            if (fromUser) {
                throttleFragment.moveThrottle(position);
            }*/
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