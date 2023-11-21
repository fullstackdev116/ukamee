package com.ediattah.yakko;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.Model.Feedback;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.Model.Vcall;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import timber.log.Timber;

public class SupportDetailActivity extends BaseActivity {
    User user;
    LinearLayout ly_cover;
    ArrayList<Feedback> arrayList = new ArrayList<>();
    TextView txt_name, txt_title, txt_rate;
    ImageView img_photo;
    RatingBar ratingBar;
    public int MY_PERMISSION_STORAGE = 201;
    public int MY_PERMISSION_CALL = 301;
    String vCallKey = "";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user = (User) getIntent().getSerializableExtra("user");
        setTitle("Support Detail");

        ly_cover = findViewById(R.id.ly_cover);
        txt_name = findViewById(R.id.txt_name);
        txt_title = findViewById(R.id.txt_title);
        txt_rate = findViewById(R.id.txt_rate);
        img_photo = findViewById(R.id.img_photo);
        ratingBar = findViewById(R.id.rate);

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedbackDialog();
            }
        });
        findViewById(R.id.ly_videocall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.goToStartVideoCallPage(user, SupportDetailActivity.this);
            }
        });
        findViewById(R.id.ly_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SupportDetailActivity.this, READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SupportDetailActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(WRITE_EXTERNAL_STORAGE);
                    arrPermissionRequests.add(READ_EXTERNAL_STORAGE);
                    ActivityCompat.requestPermissions(SupportDetailActivity.this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), MY_PERMISSION_STORAGE);
                    return;
                } else {
                    // call
                    createDirectory();
                    App.goToChatPage(SupportDetailActivity.this, user.uid);
                }
            }
        });
        findViewById(R.id.ly_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SupportDetailActivity.this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(CALL_PHONE);
                    ActivityCompat.requestPermissions(SupportDetailActivity.this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), SupportDetailActivity.this.MY_PERMISSION_CALL);
                } else {
                    // call
                    App.dialNumber(user.phone, SupportDetailActivity.this);
                }
            }
        });
        loadProfile();
        loadFeedbacks();
        JitSiConfig();
    }

    void JitSiConfig() {
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();
    }
    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }


    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.calling);
        mp.setLooping(true);

        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    mp.start();
                    Vcall vcall = new Vcall("", MyUtils.cur_user.uid, user.uid, 0);
                    vCallKey = MyUtils.mDatabase.child(MyUtils.tbl_vcall).push().getKey();
                    MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vCallKey).setValue(vcall);

                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    Toast.makeText(this, "CONFERENCE JOINED", Toast.LENGTH_SHORT).show();
                    break;
                case PARTICIPANT_JOINED:
                    mp.stop();
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    Toast.makeText(this, "PARTICIPANT JOINED", Toast.LENGTH_SHORT).show();
                    break;
                case CONFERENCE_TERMINATED:
                    mp.stop();
                    MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vCallKey).removeValue();
                    Toast.makeText(this, "CONFERENCE TERMINATED", Toast.LENGTH_SHORT).show();
                    break;
                case PARTICIPANT_LEFT:
                    mp.stop();
                    Toast.makeText(this, "PARTICIPANT_LEFT", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    void loadProfile() {
        Glide.with(this).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);
        txt_name.setText(user.firstname + " " + user.lastname);
        txt_rate.setText(String.valueOf(user.rate));
        ratingBar.setRating(user.rate);
        MyUtils.mDatabase.child(MyUtils.tbl_category).orderByChild("admin_id").equalTo(user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        Category category = datas.getValue(Category.class);
                        category._id = datas.getKey();
                        txt_title.setText(category.name + " " + "Specialist");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openFeedbackDialog() {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_add_feedback, null);
        EditText edit_feedback = view.findViewById(R.id.edit_feedback);
        RatingBar ratingBar1 = view.findViewById(R.id.rate);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed = edit_feedback.getText().toString().trim();
                if (feed.length() == 0) {
                    MyUtils.showAlert(SupportDetailActivity.this, "Warning", "Please fill in blank field");
                    return;
                }
                float rate = ratingBar1.getRating();
                Feedback feedback = new Feedback("", MyUtils.cur_user.uid, user.uid, feed, rate, MyUtils.getDateString(new Date()));
                MyUtils.mDatabase.child(MyUtils.tbl_feedback).push().setValue(feedback);
                if (user.rate == 0) {
                    user.rate = rate;
                } else {
                    user.rate = (user.rate + rate)/2;
                }
                MyUtils.mDatabase.child(MyUtils.tbl_user).child(user.uid).child("rate").setValue(user.rate);
                DesignToast.makeText(SupportDetailActivity.this, "Successfully submitted", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                dlg.dismiss();
                loadFeedbacks();
                loadProfile();
            }
        });
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        TextView txt_rate = view.findViewById(R.id.txt_rate);
        RatingBar ratingBar = view.findViewById(R.id.rate);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                txt_rate.setText(String.valueOf(rating));
            }
        });
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.4);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setCancelable(false);
        dlg.setContentView(view);
        window.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.round_frame_white));
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, height);
        dlg.show();
    }

    void loadFeedbacks() {
        arrayList.clear();
        ly_cover.removeAllViews();
        MyUtils.mDatabase.child(MyUtils.tbl_feedback).orderByChild("admin_id").equalTo(user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        Feedback feedback = datas.getValue(Feedback.class);
                        feedback._id = datas.getKey();
                        arrayList.add(feedback);
                        LayoutInflater inflater = LayoutInflater.from(SupportDetailActivity.this);
                        View view = inflater.inflate(R.layout.cell_feedback, null);
                        RatingBar ratingBar = view.findViewById(R.id.rate);
                        TextView txt_name = view.findViewById(R.id.txt_name);
                        TextView txt_feedback = view.findViewById(R.id.txt_feedback);
                        TextView txt_date = view.findViewById(R.id.txt_date);
                        ImageView img_photo = view.findViewById(R.id.img_photo);
                        txt_feedback.setText(feedback.feedback);
                        txt_date.setText(feedback.date);
                        ratingBar.setRating(feedback.rate);
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(feedback.user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user1 = snapshot.getValue(User.class);
                                    txt_name.setText(user1.firstname + " " + user1.lastname);
                                    Glide.with(SupportDetailActivity.this).load(user1.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ly_cover.addView(view);
                    }
                } else {
                    TextView txt = new TextView(SupportDetailActivity.this);
                    txt.setText("No feedback");
                    txt.setTextSize(16);
                    ly_cover.addView(txt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void createDirectory() {
        getExternalFilesDir(null);
        File assets = getExternalFilesDir("assets");
        if (!assets.exists()) {
            assets.mkdir();
        }
        App.MY_APP_PATH = assets.getAbsolutePath();
        File f3 = new File(App.MY_APP_PATH, "audio");
        if (!f3.exists()) {
            f3.mkdir();
        }
        App.MY_AUDIO_PATH = f3.getAbsolutePath();

        File pictures = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures", this.getResources().getString(R.string.app_name));
        if (!pictures.exists()) {
            pictures.mkdir();
        }
        App.MY_IMAGE_PATH = pictures.getAbsolutePath();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] _permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, _permissions, grantResults);
        if (grantResults.length > 0) {
            if  (requestCode == MY_PERMISSION_STORAGE) {
                createDirectory();
                App.goToChatPage(SupportDetailActivity.this, user.uid);

            } else if (requestCode == MY_PERMISSION_CALL) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    App.dialNumber(user.phone, this);
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                }
            }
        }
    }

}