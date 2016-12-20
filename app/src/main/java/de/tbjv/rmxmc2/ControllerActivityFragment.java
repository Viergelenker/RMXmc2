package de.tbjv.rmxmc2;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import de.ccck.rmxmobile.data_management.DataToGuiInterface;

/**
 * A placeholder fragment containing a simple view.
 */
public class ControllerActivityFragment extends Fragment {

    public ControllerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        System.out.println(DataToGuiInterface.generateTrainNameList());
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    public static void updateConnectionStatus(int connectionStatus) {

    }
}
