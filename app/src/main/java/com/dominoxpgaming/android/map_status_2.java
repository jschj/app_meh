package com.dominoxpgaming.android;


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

public class map_status_2 extends Fragment {

    public Float screenwidthdp = 100f;
    public Float screenheightdp = 100f;

    private GridLayout expandablemapicons;
    private HashMap expandablemapicons_hash;
    private Boolean expandablemapiconsstate;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_status_2, container, false);


        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Integer screenwidth  = displaymetrics.widthPixels;
        Integer screenheight = displaymetrics.heightPixels;

        this.screenwidthdp = ConvertUnits.convertPixelsToDp(screenwidth,getContext());
        this.screenheightdp = ConvertUnits.convertPixelsToDp(screenwidth,getContext());



        //((Activity) getContext()).getWindowManager()
        //        .getDefaultDisplay()
        //        .getMetrics(displayMetrics);







        //ImageButton
        ImageButton b = (ImageButton) view.findViewById(R.id.object_visibility);
        //ImageButton Click Event register
        b.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        ontoggleExpandMapIcons(v);
                    }
                }
        );
        //ImageButton Submenu
        expandablemapicons = (GridLayout) view.findViewById(R.id.object_visibilty_context);

        //Submenu Hexmap for icon connection
        HashMap <String,Integer> expandablemapicons_hash = new HashMap<>();

        //Icon description <-> Icon resource id
        expandablemapicons_hash.put("all",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("food",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("food2",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("food3",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("food4",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("food5",R.drawable.map_icon__burger);
        expandablemapicons_hash.put("drinking",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking2",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking3",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking4",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking5",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking6",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("drinking7",R.drawable.map_icon__bar_coktail);
        expandablemapicons_hash.put("meetingpoint",R.drawable.map_icon__conference);
        expandablemapicons_hash.put("meetingpoint2",R.drawable.map_icon__conference);
        expandablemapicons_hash.put("meetingpoint3",R.drawable.map_icon__conference);
        expandablemapicons_hash.put("meetingpoint4",R.drawable.map_icon__conference);
        expandablemapicons_hash.put("transportation",R.drawable.map_icon__convertible);


        int row = 0;
        int column = 0;

        int maxcolumn = (int) ((this.screenwidthdp-10f)/50f);


        for (int value: expandablemapicons_hash.values())
        {
            //Create new ImageButton
            ImageButton newImageButton = new ImageButton(getContext());

            //Add Layout Parameters
            GridLayout.LayoutParams newLayout = new GridLayout.LayoutParams();

            //size
            newLayout.width = (int) ConvertUnits.convertDpToPixel(50f,this.getContext());
            newLayout.height = (int) ConvertUnits.convertDpToPixel(50f,this.getContext());

            //gridpos
            newLayout.columnSpec = GridLayout.spec(column);
            newLayout.rowSpec = GridLayout.spec(row);

            //Adding layout features
            newImageButton.setLayoutParams(newLayout);

            //newImageButton.setId(layoutids);

            //apply image
            newImageButton.setImageResource(value);

            //set image alpha
            newImageButton.setAlpha(0.8f);

            //Add to Grid
            expandablemapicons.addView(newImageButton);

            //count up for Gridlayout
            ++column;
            if (column>= maxcolumn){
                column = 0;
                ++row;
            }
        }

        expandablemapiconsstate = false;



        return view;
    }

    //If the Button gets clickt do stuff
    public void ontoggleExpandMapIcons(View view){
        if (this.expandablemapiconsstate)
            {
                this.expandablemapicons.setVisibility(View.INVISIBLE);
                this.expandablemapiconsstate = false;
            }
        else
            {
                this.expandablemapicons.setVisibility(View.VISIBLE);
                this.expandablemapiconsstate = true;
            }

    }


}
