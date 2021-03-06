package de.tbjv.rmxmc2.activity;

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

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.ccck.rmxmobile.data_management.DataToGuiInterface;
import de.tbjv.rmxmc2.R;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private AdapterView profileListView;
    private TextView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        profile = (TextView) findViewById(R.id.profile);
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
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {

                // The selected profile
                final String item = ((TextView)view).getText().toString();

                final AlertDialog.Builder connectionDiagBuilder = new AlertDialog.Builder(context);

                // Dialog Titel
                connectionDiagBuilder.setTitle("Profil "+item);

                // Dialog Nachricht
                connectionDiagBuilder
                        .setCancelable(true)
                        .setPositiveButton("Verbinden", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // Loads the specific profile
                                DataToGuiInterface.loadConfigObject(context, item);
                                Intent intent = new Intent(context, ControllerActivity.class);
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
                                DataToGuiInterface.loadConfigObject(context, item);
                                final String ipAdress = DataToGuiInterface.getIpAdress();
                                final String port = DataToGuiInterface.getPort();
                                DataToGuiInterface.deleteConfigObject(context, item);
                                loadAndPublishProfiles();
                                Snackbar
                                        .make(adapterView, "Profil "+item+" wurde gelöscht", Snackbar.LENGTH_LONG)
                                        .setAction("Rückgängig", new View.OnClickListener() {

                                            @Override
                                            public void onClick(View view) {
                                                DataToGuiInterface.saveConfigObject(context, item, ipAdress, port, 0);
                                                loadAndPublishProfiles();
                                                Snackbar.make(view, "Profil wurde wiederhergestellt", Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .show();
                            }
                        });
                AlertDialog alertDialog = connectionDiagBuilder.create();
                alertDialog.show();

            }
        });
    }

    /**
     * loads available profiles and shows them within profileListView
     */
    private void loadAndPublishProfiles() {

        ArrayList<String> profiles = new ArrayList<>(DataToGuiInterface.generateConfigNameList(getBaseContext()));
        ArrayAdapter<String> profileListAdapter = new ArrayAdapter<>(
                getBaseContext(),
                R.layout.profile_list_item,
                R.id.profilesListItem,
                profiles
        );

        profileListView = (ListView) findViewById(R.id.profilesListView);
        profileListView.setAdapter(profileListAdapter);

        if (profiles.size() == 0) {
            profile.setText("Es existiert noch kein Profil. \r\nFüge ein neues hinzu, indem Du auf das + klickst.");
        } else profile.setText(R.string.MainProfile);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAndPublishProfiles();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
