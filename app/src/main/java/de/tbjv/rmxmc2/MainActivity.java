package de.tbjv.rmxmc2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.ccck.rmxmobile.data_management.ConfigObject;
import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.Service.AsyncConnect;
import eu.esu.mobilecontrol2.sdk.MobileControl2;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private AdapterView profileListView;
    private AsyncConnect connect;
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addProfile = (FloatingActionButton) findViewById(R.id.addProfile);
        addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, AddProfileActivity.class));
            }
        });

        loadAndPublishProfiles();

        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // The selected item
                final String item = ((TextView)view).getText().toString();

                final AlertDialog.Builder connectionDiagBuilder = new AlertDialog.Builder(context);

                // Dialog Titel
                connectionDiagBuilder.setTitle("Profil "+item);

                // Dialog Nachricht
                connectionDiagBuilder
                        .setCancelable(true)
                        .setPositiveButton("Verbinden", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                DataToGuiInterface.loadConfigObject(context, item);
                                Intent intent = new Intent(context, Interface.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNeutralButton("Löschen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DataToGuiInterface.deleteConfigObject(context, item);
                                loadAndPublishProfiles();
                            }
                        });
                AlertDialog alertDialog = connectionDiagBuilder.create();
                alertDialog.show();

            }
        });
    }

    private void loadAndPublishProfiles() {
        // Load available profiles...
        ArrayList<String> profiles = new ArrayList<>(DataToGuiInterface.generateConfigNameList(getBaseContext()));
        ArrayAdapter<String> profileListAdapter = new ArrayAdapter<>(
                getBaseContext(),
                R.layout.profile_list_item,
                R.id.profilesListItem,
                profiles
        );

        // and show them within the listView
        profileListView = (ListView) findViewById(R.id.profilesListView);
        profileListView.setAdapter(profileListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadAndPublishProfiles();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
