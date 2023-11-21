package com.ediattah.yakko;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.Model.BotCnt;
import com.ediattah.yakko.Model.User;

import java.util.Date;

public class SignupActivity extends BaseActivity {
    String phone_number, firstname, lastname, address;
    EditText edit_phone, edit_firstname, edit_lastname, edit_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        phone_number = getIntent().getStringExtra("phone");
        if (phone_number == null) {
            phone_number = "";
        }
        edit_phone = findViewById(R.id.edit_phone);
        edit_firstname = findViewById(R.id.edit_firstname);
        edit_lastname = findViewById(R.id.edit_lastname);
        edit_address = findViewById(R.id.edit_address);
        edit_phone.setText("+" + phone_number);


        findViewById(R.id.txt_change_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.removePreference(App.NUMBER);
                finish();
            }
        });
        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstname = edit_firstname.getText().toString().trim();
                lastname = edit_lastname.getText().toString().trim();
                address = edit_address.getText().toString().trim();
                if (firstname.length()*lastname.length()*address.length() == 0) {
                    MyUtils.showAlert(SignupActivity.this, getResources().getString(R.string.warning), getResources().getString(R.string.please_fill_in_blank_field));
                    return;
                }
                String token = MyUtils.getDeviceToken(SignupActivity.this);
                Date date = new Date();
                String expiry = MyUtils.getDateString(MyUtils.getNextMonth(date));
                MyUtils.cur_user = new User("", firstname, lastname, phone_number, "", "", MyUtils.PATIENT, 1, token, 0, MyUtils.getDateString(date), "", "", address, 0, expiry, 0);
                String key = MyUtils.mDatabase.child(MyUtils.tbl_user).push().getKey();
                MyUtils.mDatabase.child(MyUtils.tbl_user).child(key).setValue(MyUtils.cur_user);
                MyUtils.cur_user.uid = key;
                MyUtils.mDatabase.child(MyUtils.tbl_botcnt).push().setValue(new BotCnt("", key, 10));
                DesignToast.makeText(SignupActivity.this, "Successfully registered!", DesignToast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                openJoinDialog();
            }
        });
    }
    public void openJoinDialog() {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_join_message, null);
        view.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openJoinWhyDialog();
                dlg.dismiss();
            }
        });
        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                Intent intent = new Intent(SignupActivity.this, MainActivityPatient.class);
                startActivity(intent);
                finish();
            }
        });
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.3);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setCancelable(false);
        dlg.setContentView(view);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
//        window.setLayout(width, height);
        dlg.show();
    }
    public void openJoinWhyDialog() {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_join_why, null);
        view.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                ActivityCompat.finishAffinity(SignupActivity.this);
                System.exit(0);
            }
        });
        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, MainActivityPatient.class);
                startActivity(intent);
                finish();
            }
        });
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.3);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setCancelable(false);
        dlg.setContentView(view);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
//        window.setLayout(width, height);
        dlg.show();
    }
}