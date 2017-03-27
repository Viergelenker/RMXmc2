package de.tbjv.rmxmc2.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import de.tbjv.rmxmc2.R;

public class MappingActivity extends AppCompatActivity {

    private static boolean mappingStarted = true;
    public static Context context;

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

        context = this.getApplicationContext();

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mappingStarted) {

                    switch (v.getId()) {
                        case R.id.button_F1:
                            System.out.println("F1 gedrückt");
                            break;
                        case R.id.button_F2:
                            break;
                        case R.id.button_F3:
                            break;
                        case R.id.button_F4:
                            break;
                        case R.id.button_F5:
                            break;
                        case R.id.button_F6:
                            break;
                        case R.id.button_F7:
                            break;
                        case R.id.button_F8:
                            break;
                        case R.id.button_F9:
                            break;
                        case R.id.button_F10:
                            break;
                        case R.id.button_F11:
                            break;
                        case R.id.button_F12:
                            break;
                        case R.id.button_F13:
                            break;
                        case R.id.button_F14:
                            break;
                        case R.id.button_F15:
                            break;
                        case R.id.button_F16:
                            break;
                        case R.id.button_Direction:
                            break;
                        case R.id.button_Light:
                            break;

                        default:
                            break;
                    }

                }

            }
        };

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


}
