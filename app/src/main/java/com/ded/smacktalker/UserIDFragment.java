package com.ded.smacktalker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class UserIDFragment extends Fragment {

    EditText userIDText;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialization here.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        Button confirmUserID = (Button) view.findViewById(R.id.confirmUserID);
        userIDText = (EditText) view.findViewById(R.id.userID);

        return view;
    }

    public void setUserID(View view){
        MainActivity.userID = userIDText.getText().toString();
    }

    @Override
    public void onPause() {
        //If the fragment is to be destroyed, this will run first.
        //Good spot to save small amounts of data.
        super.onPause();
    }
}
