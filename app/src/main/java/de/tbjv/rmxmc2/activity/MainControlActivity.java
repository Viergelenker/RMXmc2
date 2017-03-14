package de.tbjv.rmxmc2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import de.tbjv.rmxmc2.R;
import de.tbjv.rmxmc2.fragment.HomeFragment;
import de.tbjv.rmxmc2.fragment.MappingFragment;
import de.tbjv.rmxmc2.fragment.SettingsFragment;

//TODO: Aussortieren
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

public class MainControlActivity extends AppCompatActivity {

    private static ThrottleFragment throttleFragment;
    private static SeekBar seekBar1;
    private static ThrottleScale throttleScale = new ThrottleScale(10, 127);
    public static Context context;
    private static TextView connectionStatus;
    private static TextView speed;
    private static Spinner trainSelector;
    private static boolean fromServer = true;
    private static boolean changedFromUser = false;
    private static ToggleButton buttonLight;
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
    
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtProfileName;
    private Toolbar toolbar;

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "http://wallpapercave.com/wp/gK56hec.jpg";
    private static final String urlProfileImg = "https://lh3.googleusercontent.com/eCtE_G34M9ygdkmOpYvCag1vBARCmZwnVS6rS5t4JLzJ6QgQSBquM0nuTsCpLhYbKljoyS-txg";

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_MAPPING = "mapping";
    private static final String TAG_SETTINGS = "settings";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtProfileName = (TextView) navHeader.findViewById(R.id.profileName);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
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
                    buttonLight.setChecked(true);
                } else {
                    buttonLight.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    buttonF1.setChecked(true);
                } else {
                    buttonF1.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    buttonF2.setChecked(true);
                } else {
                    buttonF2.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    buttonF3.setChecked(true);
                } else {
                    buttonF3.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    buttonF4.setChecked(true);
                } else {
                    buttonF4.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    buttonF5.setChecked(true);
                } else {
                    buttonF5.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    buttonF6.setChecked(true);
                } else {
                    buttonF6.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    buttonF7.setChecked(true);
                } else {
                    buttonF7.setChecked(false);
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
                } else {
                    buttonF8.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 1)) {
                    buttonF9.setChecked(true);
                } else {
                    buttonF9.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 2)) {
                    buttonF10.setChecked(true);
                } else {
                    buttonF10.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 3)) {
                    buttonF11.setChecked(true);
                } else {
                    buttonF11.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 4)) {
                    buttonF12.setChecked(true);
                } else {
                    buttonF12.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 5)) {
                    buttonF13.setChecked(true);
                } else {
                    buttonF13.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 6)) {
                    buttonF14.setChecked(true);
                } else {
                    buttonF14.setChecked(false);
                }

                if (UtilsByte.bitIsSet(modeByte, 7)) {
                    buttonF15.setChecked(true);
                } else {
                    buttonF15.setChecked(false);
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
                } else {
                    buttonF16.setChecked(false);
                }
            }
        }
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
        MainControlActivity.active = active;
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
                                    MainControlActivity.this);
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
                                            MainControlActivity
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
    
    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, profile name, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, profile name
        txtName.setText("RMXmc2");
        txtProfileName.setText("Profil: " + "Profil 1");

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);

    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                // mapping fragment
                MappingFragment mappingFragment = new MappingFragment();
                return mappingFragment;
            case 2:
                // settings fragment
                SettingsFragment settingsFragment = new SettingsFragment();
                return settingsFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_train_control:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_mapping:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_MAPPING;
                        break;
                    case R.id.nav_settings:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SETTINGS;
                        break;
                    case R.id.nav_profile:
                        navItemIndex = 2;
                        //TODO: StoppThread
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainControlActivity.this, MainActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        stoppThread();
        DataToGuiInterface.terminateThread();
        Intent intent = new Intent(MainControlActivity.this,
                MainActivity.class);
        MainControlActivity.this.startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.control_main, menu);
        }
        return true;
    }

}
