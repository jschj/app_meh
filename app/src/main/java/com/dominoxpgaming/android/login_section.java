package com.dominoxpgaming.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by dominoxp on 30.07.2016.
 */

public class login_section extends Fragment{

    private static EditText username;
    private static EditText password;

    loginsectionlistener activityCommander;

    public interface loginsectionlistener{
        public void onInput(String username, String password);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            activityCommander = (loginsectionlistener) activity;
        }
        catch(ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_section, container, false);

        username = (EditText) view.findViewById(R.id.usernameInput);
        password = (EditText) view.findViewById(R.id.passwordInput);
        final Button button = (Button) view.findViewById(R.id.loginButton);

        //Button Click Event register
        button.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                       buttonClicked(v);
                    }
                }
        );

        return view;
    }

    //If the Button gets clickt do stuff
    public void buttonClicked(View view){
        activityCommander.onInput(username.getText().toString(),
                password.getText().toString());
    }

}
