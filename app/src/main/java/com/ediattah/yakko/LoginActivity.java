package com.ediattah.yakko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.Service.SmsListener;
import com.ediattah.yakko.Service.SmsReceiver;
import com.ediattah.yakko.httpsModule.RestClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.jkb.vcedittext.VerificationAction;
import com.jkb.vcedittext.VerificationCodeEditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    String country_code, number, phone_number;
    int REQUEST_CAMERA_PERMISSIONS = 101, REQUEST_SMS_PERMISSIONS = 201;
    ProgressDialog progressDialog;
    Timer timer;
    int otp_sec = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        findViewById(R.id.txt_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpDialog();
            }
        });
        EditText edit_phone = findViewById(R.id.edit_phone);
        CountryCodePicker txt_countryCode = findViewById(R.id.txt_countryCode);

        findViewById(R.id.btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyUtils.isEmptyEditText(edit_phone)) {
                    country_code = txt_countryCode.getSelectedCountryCode();
                    number = edit_phone.getText().toString().trim();
                    number = number.replace(" ", "");
                    number = number.replace("-", "");
                    phone_number = country_code + number;
                    App.hideKeyboard(LoginActivity.this);
                    sendAuthSMS();
                } else {
                    MyUtils.showAlert(LoginActivity.this, getResources().getString(R.string.warning), getResources().getString(R.string.please_input_your_mobile_number));
                }
            }
        });
        phone_number = App.readPreference(App.NUMBER, "");
        if (phone_number.length() > 0) {
            checkExistingPhone();
        }
    }
    private void sendAuthSMS() {
        ArrayList<String> numbers = new ArrayList<>();
        numbers.add(phone_number);
        progressDialog.show();
        String to = TextUtils.join(";", numbers);
        String datetime = MyUtils.getCurrentDateTimeString();
        String code = MyUtils.getRandom6NumberString();
        if (phone_number.equals("2251234567880")) {
            code = "000000";
        } else if (phone_number.equals("2251234567881")) {
            code = "000000";
        } else if (phone_number.equals("2251234567882")) {
            code = "000000";
        } else if (phone_number.equals("2251234567883")) {
            code = "000000";
        } else if (phone_number.equals("2251234567884")) {
            code = "000000";
        } else if (phone_number.equals("2251234567885")) {
            code = "000000";
        } else if (phone_number.equals("2251234567886")) {
            code = "000000";
        } else if (phone_number.equals("2251234567887")) {
            code = "000000";
        } else if (phone_number.equals("2251234567890")) {
            code = "000000";
        }
        String message = getResources().getString(R.string.app_name) + " OTP Verification\nYour Code is " + code;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
        nameValuePairs.add(new BasicNameValuePair("username", App.sms_username));
        nameValuePairs.add(new BasicNameValuePair("password", App.sms_password));
        nameValuePairs.add(new BasicNameValuePair("sender", App.sms_senderID));
        nameValuePairs.add(new BasicNameValuePair("to", to));
        nameValuePairs.add(new BasicNameValuePair("text", message));
        nameValuePairs.add(new BasicNameValuePair("type", "text"));
        nameValuePairs.add(new BasicNameValuePair("datetime", datetime));

        final RestClient restClient = RestClient.getInstance();

        String finalCode = code;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = null;
                try {
                    response = restClient.postRequest1(App.ediaSMSUrl, nameValuePairs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response.contains("OK:")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Please check your SMS to verify OTP", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            openVerifyDialog(finalCode, phone_number);

                        }
                    });
                } else if (response.contains("ERROR:")) {
                    final String error = response.substring(7);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyUtils.showAlert(LoginActivity.this, "SMS API Error", error);
                            progressDialog.dismiss();
                        }
                    });
                }

            }
        }).start();
    }
    public void openVerifyDialog(String code, String phone_number) {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_o_t_p, null);
        TextView txt_remaining = view.findViewById(R.id.txt_remaining);
        VerificationCodeEditText edit_code = view.findViewById(R.id.edit_code);
        edit_code.setOnVerificationCodeChangedListener(new VerificationAction.OnVerificationCodeChangedListener() {
            @Override
            public void onVerCodeChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void onInputCompleted(CharSequence s) {
                txt_remaining.setVisibility(View.INVISIBLE);
                timer.purge();
                timer.cancel();
                String input_code = edit_code.getText().toString();
                try {
                    dlg.dismiss();
                    App.hideKeyboard(LoginActivity.this);
                    if (input_code.equals(code)) {
                        signInWithEmailPassword(App.FBemail, App.FBpassword);
                    } else {
                        MyUtils.showAlert(LoginActivity.this, getResources().getString(R.string.warning), "Verification code is incorrect");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        SmsReceiver smsReceiver = new SmsReceiver();
        smsReceiver.mListener = new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                edit_code.setText(messageText);
            }
        };
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(view);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dlg.show();
        edit_code.requestFocus();
        App.showKeyboard(this);
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (timer != null) {
                    edit_code.setText("");
                    timer.purge();
                    timer.cancel();
                }
            }
        });
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        otp_sec = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                otp_sec --;
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        txt_remaining.setText("Please input your SMS Verification Code. \nIt will be expired in " + String.valueOf(otp_sec) + " seconds.");
                        if (otp_sec == 0) {
                            txt_remaining.setText("Time has been out! Please resend the verification code.");
                            edit_code.setVisibility(View.INVISIBLE);
                            timer.purge();
                            timer.cancel();
                        }
                    }
                });
            }

        }, 0, 1000);
    }
    public void signInWithEmailPassword(String email, String password)
    {
        progressDialog.show();
        MyUtils.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
//                        App.goToMainPage(LoginActivity.this, progressDialog);
                        App.setPreference(App.NUMBER, phone_number);
                        checkExistingPhone();
                    }
                });
    }
    public void checkExistingPhone() {
        progressDialog.show();
        MyUtils.mDatabase.child(MyUtils.tbl_user).orderByChild("phone").equalTo(phone_number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot datas:dataSnapshot.getChildren()) {
                        MyUtils.cur_user = datas.getValue(User.class);
                        MyUtils.cur_user.uid = datas.getKey();
                    }
                    Intent intent;
                    if (MyUtils.cur_user.type.equals("ADMIN")) {
                        intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
                    } else {
                        intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                    intent.putExtra("phone", phone_number);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
    public void openHelpDialog() {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_help, null);
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
//        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
//        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(view);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
//        window.setLayout(width, height);
        dlg.show();
    }
}