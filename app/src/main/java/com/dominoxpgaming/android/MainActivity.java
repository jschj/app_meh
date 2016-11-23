package com.dominoxpgaming.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements login_section.loginsectionlistener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void enter_map(View view){
        Intent i = new Intent(this, MainMapView.class);
        startActivity(i);
    }

    public void onInput(String username,String password){
        final String abc = username+password;
        Log.i("DominoDebug",abc);
    }
}
