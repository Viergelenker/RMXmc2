package de.tbjv.rmxmc2.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;

public class AddProfileActivity extends AppCompatActivity {

    Context context = this;
    private TextView profileName;
    private TextView ipAddress;
    private TextView port;
    private String profileNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        profileName = (TextView) findViewById(R.id.profileName);
        ipAddress = (TextView) findViewById(R.id.ipAddress);
        port = (TextView) findViewById(R.id.port);

        profileName.requestFocus();

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                // Dismiss keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                if (!saveProfile(view)) {
                    startActivity(new Intent(context, MainActivity.class));
                }

            }
        });

        Button saveAndConnectButton = (Button) findViewById(R.id.saveAndConnectButton);
        saveAndConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                // Dismiss keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                saveAndConnect(view);
            }
        });
    }

    /**
     * saves RMX server connection profile if needed fields are filled in correctly
     */
    private boolean saveProfile(View view) {

        profileNameText = profileName.getText().toString();
        String ipAddressText = ipAddress.getText().toString();
        String portText = port.getText().toString();
        boolean cancel = false;

        if (TextUtils.isEmpty(profileNameText) || !isNameValid(profileNameText)) {
            profileName.setError("Bitte geben Sie einen Namen ein");
            cancel = true;
        }

        if (!isNameValid(profileNameText)) {
            profileName.setError("Namen dürfen nicht länger als 10 Zeichen sein");
            cancel = true;
        }

        if (TextUtils.isEmpty(ipAddressText)) {
            ipAddress.setError("Es muss eine gültige IP-Adresse eingegeben werden");
            cancel = true;
        }

        if (TextUtils.isEmpty(portText)) {
            port.setError("Es muss ein gültiger Port eingegeben werden");
            cancel = true;
        }

        if (!cancel) {
            DataToGuiInterface.saveConfigObject(getBaseContext(), profileNameText, ipAddressText, portText, 0);
            Snackbar
                    .make(view, "Profil "+profileNameText+" gespeichert", Snackbar.LENGTH_LONG)
                    .setAction("Löschen", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            DataToGuiInterface.deleteConfigObject(context, profileNameText);
                            Snackbar.make(view, "Profil wurde gelöscht", Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .show();
        }
        return cancel;
    }

    private void saveAndConnect(View view) {
        if (!saveProfile(view)) {
            DataToGuiInterface.loadConfigObject(context, profileNameText);
            startActivity(new Intent(context, ControllerActivity.class));
        }
    }

    private boolean isNameValid(String name) {
        return name.length() <= 10;
    }
}

