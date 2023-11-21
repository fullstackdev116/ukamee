package com.ediattah.yakko.FragmentPatient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ediattah.yakko.CategoryActivity;
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;

public class ChatbotPatientFragment extends Fragment {
    MainActivityPatient activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chatbot_patient, container, false);
        activity.setTitle(getResources().getString(R.string.app_name) + " Bot");
        activity.setLogoutVisible(false);

        v.findViewById(R.id.card_health).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CategoryActivity.class);
                intent.putExtra("type", MyUtils.type_health);
                startActivity(intent);
            }
        });
        v.findViewById(R.id.card_mental).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CategoryActivity.class);
                intent.putExtra("type", MyUtils.type_mental);
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityPatient) context;
    }
}