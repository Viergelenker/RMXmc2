package de.tbjv.rmxmc2.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;
import eu.esu.mobilecontrol2.sdk.MobileControl2;
import eu.esu.mobilecontrol2.sdk.ThrottleFragment;

public class MappingActivity extends AppCompatActivity {

    Context context = this;

    private static boolean mappingStarted;
    public int keyToMap;

    private TextView mappingDescription;
    private View button_F1;
    private View button_F2;
    private View button_F3;
    private View button_F4;
    private View button_F5;
    private View button_F6;
    private View button_F7;
    private View button_F8;
    private View button_F9;
    private View button_F10;
    private View button_F11;
    private View button_F12;
    private View button_F13;
    private View button_F14;
    private View button_F15;
    private View button_F16;
    private View button_Direction;
    private View button_Light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeObjects();
        hideButtons(true);

        mappingDescription.setText("Drücke eine Taste auf der Mobile Control II, der Du eine Funktion für \"" + ControllerActivity.trainName + "\" zuweisen möchtest.");

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mappingStarted) {

                    switch (v.getId()) {
                        case R.id.button_F1:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 1);
                            break;
                        case R.id.button_F2:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 2);
                            break;
                        case R.id.button_F3:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 3);
                            break;
                        case R.id.button_F4:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 4);
                            break;
                        case R.id.button_F5:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 5);
                            break;
                        case R.id.button_F6:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 6);
                            break;
                        case R.id.button_F7:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 7);
                            break;
                        case R.id.button_F8:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 8);
                            break;
                        case R.id.button_F9:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 9);
                            break;
                        case R.id.button_F10:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 10);
                            break;
                        case R.id.button_F11:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 11);
                            break;
                        case R.id.button_F12:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 12);
                            break;
                        case R.id.button_F13:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 13);
                            break;
                        case R.id.button_F14:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 14);
                            break;
                        case R.id.button_F15:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 15);
                            break;
                        case R.id.button_F16:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 16);
                            break;
                        case R.id.button_Direction:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 19);
                            break;
                        case R.id.button_Light:
                            setMapping(ControllerActivity.currentTrain, keyToMap, 0);
                            break;

                        default:
                            break;
                    }
                }

            }
        };

        button_F1.setOnClickListener(listener);
        button_F2.setOnClickListener(listener);
        button_F3.setOnClickListener(listener);
        button_F4.setOnClickListener(listener);
        button_F5.setOnClickListener(listener);
        button_F6.setOnClickListener(listener);
        button_F7.setOnClickListener(listener);
        button_F8.setOnClickListener(listener);
        button_F9.setOnClickListener(listener);
        button_F10.setOnClickListener(listener);
        button_F11.setOnClickListener(listener);
        button_F12.setOnClickListener(listener);
        button_F13.setOnClickListener(listener);
        button_F14.setOnClickListener(listener);
        button_F15.setOnClickListener(listener);
        button_F16.setOnClickListener(listener);
        button_Direction.setOnClickListener(listener);
        button_Light.setOnClickListener(listener);

    }


    /**
     * initializes buttons and fixes null object reference
     */
    private void initializeObjects() {

        button_F1 = (LinearLayout) findViewById(R.id.button_F1);
        button_F2 = (LinearLayout) findViewById(R.id.button_F2);
        button_F3 = (LinearLayout) findViewById(R.id.button_F3);
        button_F4 = (LinearLayout) findViewById(R.id.button_F4);
        button_F5 = (LinearLayout) findViewById(R.id.button_F5);
        button_F6 = (LinearLayout) findViewById(R.id.button_F6);
        button_F7 = (LinearLayout) findViewById(R.id.button_F7);
        button_F8 = (LinearLayout) findViewById(R.id.button_F8);
        button_F9 = (LinearLayout) findViewById(R.id.button_F9);
        button_F10 = (LinearLayout) findViewById(R.id.button_F10);
        button_F11 = (LinearLayout) findViewById(R.id.button_F11);
        button_F12 = (LinearLayout) findViewById(R.id.button_F12);
        button_F13 = (LinearLayout) findViewById(R.id.button_F13);
        button_F14 = (LinearLayout) findViewById(R.id.button_F14);
        button_F15 = (LinearLayout) findViewById(R.id.button_F15);
        button_F16 = (LinearLayout) findViewById(R.id.button_F16);
        button_Direction = (LinearLayout) findViewById(R.id.button_Direction);
        button_Light = (LinearLayout) findViewById(R.id.button_Light);
        mappingDescription = (TextView) findViewById(R.id.mappingDescription);

    }

    /**
     * hides all buttons when mapping process has not started yet or is already finished
     *
     * @param bool whether buttons should be hidden or visible
     */
    private void hideButtons(boolean bool) {

        if (bool) {
            button_F1.setVisibility(View.INVISIBLE);
            button_F2.setVisibility(View.INVISIBLE);
            button_F3.setVisibility(View.INVISIBLE);
            button_F4.setVisibility(View.INVISIBLE);
            button_F5.setVisibility(View.INVISIBLE);
            button_F6.setVisibility(View.INVISIBLE);
            button_F7.setVisibility(View.INVISIBLE);
            button_F8.setVisibility(View.INVISIBLE);
            button_F9.setVisibility(View.INVISIBLE);
            button_F10.setVisibility(View.INVISIBLE);
            button_F11.setVisibility(View.INVISIBLE);
            button_F12.setVisibility(View.INVISIBLE);
            button_F13.setVisibility(View.INVISIBLE);
            button_F14.setVisibility(View.INVISIBLE);
            button_F15.setVisibility(View.INVISIBLE);
            button_F16.setVisibility(View.INVISIBLE);
            button_Direction.setVisibility(View.INVISIBLE);
            button_Light.setVisibility(View.INVISIBLE);

        } else {
            button_F1.setVisibility(View.VISIBLE);
            button_F2.setVisibility(View.VISIBLE);
            button_F3.setVisibility(View.VISIBLE);
            button_F4.setVisibility(View.VISIBLE);
            button_F5.setVisibility(View.VISIBLE);
            button_F6.setVisibility(View.VISIBLE);
            button_F7.setVisibility(View.VISIBLE);
            button_F8.setVisibility(View.VISIBLE);
            button_F9.setVisibility(View.VISIBLE);
            button_F10.setVisibility(View.VISIBLE);
            button_F11.setVisibility(View.VISIBLE);
            button_F12.setVisibility(View.VISIBLE);
            button_F13.setVisibility(View.VISIBLE);
            button_F14.setVisibility(View.VISIBLE);
            button_F15.setVisibility(View.VISIBLE);
            button_F16.setVisibility(View.VISIBLE);
            button_Direction.setVisibility(View.VISIBLE);
            button_Light.setVisibility(View.VISIBLE);
            mappingDescription.setText("Wähle nun eine Funktion aus.");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case ThrottleFragment.KEYCODE_THROTTLE_WAKEUP:
                // Ignore the wake up key. You must return true here to avoid further input key handling.
                return true;
            case MobileControl2.KEYCODE_TOP_LEFT:
                mappingStarted = true;
                keyToMap = 0;
                hideButtons(false);
                return true;
            case MobileControl2.KEYCODE_BOTTOM_LEFT:
                mappingStarted = true;
                keyToMap = 1;
                hideButtons(false);
                return true;
            case MobileControl2.KEYCODE_TOP_RIGHT:
                mappingStarted = true;
                keyToMap = 2;
                hideButtons(false);
                return true;
            case MobileControl2.KEYCODE_BOTTOM_RIGHT:
                mappingStarted = true;
                keyToMap = 3;
                hideButtons(false);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * sets mapping (button) for given train and given mc2 key
     * @param currentTrain
     * @param keyToMap which button of the mc2 should be used
     * @param function which button/function should be mapped to mc2 button
     */
    public void setMapping(int currentTrain, int keyToMap, int function) {

        if (ControllerActivity.currentTrain >= 0) {

            // Add a zero to the function number so the final string has always the same size
            String functionNumberAsString;
            if (function < 10) {
                functionNumberAsString = "0";
                functionNumberAsString = functionNumberAsString + String.valueOf(function);
            } else functionNumberAsString = String.valueOf(function);

            // Load the current mapping of the selected profile and train
            SharedPreferences mapping = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
            // The second string is the value to return if this preference does not exist.
            String functionMappingString = mapping.getString(String.valueOf(currentTrain), "00010203");

            List<String> functionList;
            functionList = splitMappingStringIntoList(functionMappingString);

            // Now the value of the new function is set within the array list, at the corresponding
            // index of the keyToMap
            functionList.set(keyToMap, functionNumberAsString);

            SharedPreferences settings = getSharedPreferences(DataToGuiInterface.getAccountName(), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(String.valueOf(ControllerActivity.currentTrain), functionList.toString().replaceAll("\\W", ""));

            // and commit the edits (i used apply() instead of commit, because this way it gets handled
            // in the background whereas commit() blocks the thread)
            editor.apply();
        }
        hideButtons(true);
        mappingStarted = false;
        mappingDescription.setText("Die Tastenbelegung wurde erfolgreich für \"" + ControllerActivity.trainName + "\" übernommen!\r\n\nDrücke eine weitere Taste auf der Mobile Control II, der Du eine Funktion zuweisen möchtest.");
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

}
