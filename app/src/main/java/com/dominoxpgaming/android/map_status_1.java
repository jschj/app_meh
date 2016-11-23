package com.dominoxpgaming.android;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.dominoxpgaming.android.mutilitys.ConvertUnits;

import java.util.HashMap;


/**
 * Created by Jan on 02.08.2016.
 */

public class map_status_1 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_status_1, container, false);


        //ImageButton
        ImageButton b = (ImageButton) view.findViewById(R.id.go_to_settings);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(),SettingsActivity.class);
                startActivity(intent);
            }
        });



        return view;
    }
}
