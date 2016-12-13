package de.tbjv.rmxmc2.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import de.ccck.rmxmobile.communication.Connection;
import de.ccck.rmxmobile.data_management.DataToComInterface;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.MainActivity;
import eu.esu.mobilecontrol2.sdk.MobileControl2;

/**
 * Created by Julien on 05.12.16.
 */

public class AsyncConnect extends AsyncTask<Object, Object, Integer> {

    private WeakReference<Activity> activityWeakReference;
    private AlertDialog alertDialog;

    // Constructor to get the application context from the calling activity
    public AsyncConnect(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onCancelled() {
        MobileControl2.setLedState(MobileControl2.LED_GREEN, false);
    }

    @Override
    protected void onPreExecute()
    {
        connectionDialog();
    }

    @Override
    protected Integer doInBackground(Object... voids) {
        DataToGuiInterface.connect();

        // TODO: Implement a timeout (Connection.getConnectionStatus() != 2 || after x seconds)
        while (Connection.getConnectionStatus() != 2) {
            if (isCancelled()) break;
        }
        return Connection.getConnectionStatus();
    }

    @Override
    protected void onPostExecute(Integer result) {
        // Lässt die grüne LED leuchten
        MobileControl2.setLedState(MobileControl2.LED_GREEN, true);
        // DataToComInterface.changeConnectionStatus(Connection.getConnectionStatus());
        alertDialog.dismiss();
    }

    private void connectionDialog() {

        // Lässt die grüne LED blinken
        MobileControl2.setLedState(MobileControl2.LED_GREEN, 250, 250);

        AlertDialog.Builder connectionDialog = new AlertDialog.Builder(activityWeakReference.get());

        // Dialog Titel
        connectionDialog.setTitle("Verbindung zur RMX Zentrale");

        // Dialog Nachricht
        connectionDialog
                .setMessage("Verbindung wird hergestellt...")
                .setCancelable(true)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Bricht den Verbindungsaufbau ab.
                        cancel(true);
                        DataToGuiInterface.terminateThread();
                        while (Connection.getConnectionStatus() != 4){

                        }
                        activityWeakReference.get().finish();
                    }
                });

        // Dialog erstellen und anzeigen
        alertDialog = connectionDialog.create();
        alertDialog.show();
    }
}
