package com.ediattah.yakko;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.FragmentPatient.ChatbotPatientFragment;
import com.ediattah.yakko.FragmentPatient.FriendsPatientFragment;
import com.ediattah.yakko.FragmentPatient.SettingsPatientFragment;
import com.ediattah.yakko.Model.ChatRoom;
import com.ediattah.yakko.Model.MessageChat;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.Model.Vcall;
import com.ediattah.yakko.Service.PermissionCallback;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class MainActivityPatient extends BaseActivity {

    TextView txt_title;
    ImageButton btn_message, btn_logout;
    ImageView img_friends, img_chatbot, img_settings;
    LinearLayout ly_friends, ly_chatbot, ly_settings;
    FrameLayout frameLayout;
    FragmentTransaction transaction;
    public ProgressDialog progressDialog;
    public int MY_PERMISSION_STORAGE = 201;
    public int MY_PERMISSION_CALL = 301;
    public int MY_PERMISSION_CAMERA = 101;
    public PermissionCallback permissionCallback;
    int req_code_overlay = 1234;
    ValueAnimator valueAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);
        progressDialog = new ProgressDialog(this);

        txt_title = findViewById(R.id.txt_title);
        btn_message = findViewById(R.id.btn_message);
        btn_logout = findViewById(R.id.btn_logout);
        frameLayout = findViewById(R.id.frameLayout);
        img_friends = findViewById(R.id.img_friends);
        img_chatbot = findViewById(R.id.img_chatbot);
        img_settings = findViewById(R.id.img_settings);
        ly_friends = findViewById(R.id.ly_friends);
        ly_chatbot = findViewById(R.id.ly_chatbot);
        ly_settings = findViewById(R.id.ly_settings);

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_message.setColorFilter(getResources().getColor(R.color.white));
                valueAnimator.cancel();
                Intent intent = new Intent(MainActivityPatient.this, MessageActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ly_friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTabs();
                img_friends.setColorFilter(getResources().getColor(R.color.white));
                ly_friends.setBackground(getResources().getDrawable(R.drawable.oval_frame_orange));
                selectFragment(new FriendsPatientFragment());
            }
        });
        findViewById(R.id.ly_chatbot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTabs();
                img_chatbot.setColorFilter(getResources().getColor(R.color.white));
                ly_chatbot.setBackground(getResources().getDrawable(R.drawable.oval_frame_orange_50));
                selectFragment(new ChatbotPatientFragment());
            }
        });
        findViewById(R.id.ly_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTabs();
                img_settings.setColorFilter(getResources().getColor(R.color.white));
                ly_settings.setBackground(getResources().getDrawable(R.drawable.oval_frame_orange));
                selectFragment(new SettingsPatientFragment());
            }
        });
        selectFragment(new ChatbotPatientFragment());
        setDisconnectCase();
        updateUserInfo();
        checkVCallRequest();
        askOverLayPermission();

        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float fractionAnim = (float) valueAnimator.getAnimatedValue();

                btn_message.setColorFilter(ColorUtils.blendARGB(getColor(R.color.white)
                        , getColor(R.color.orange)
                        , fractionAnim));
            }
        });
        detectNewMessage();
    }
    void detectNewMessage() {
        MyUtils.mDatabase.child(MyUtils.tbl_chat).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if (flag) {
//                        valueAnimator.start();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if ( flag) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            ChatRoom chatRoom = ds.getValue(ChatRoom.class);
                            if (chatRoom.messages.size() > 1) {
                                MessageChat chat = chatRoom.messages.get(chatRoom.messages.size()-1);
                                if (chat.receiver_id.equals(MyUtils.cur_user.uid)) {
                                    if (!chat.seen) {
                                        valueAnimator.start();
                                    }
                                }
                            }
                        }

                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if (flag) {
//                        valueAnimator.start();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Database Error:", databaseError.getMessage());
            }
        });
    }
    public void setTitle(String title) {
        txt_title.setText(title);
    }
    public void setLogoutVisible(boolean flag) {
        if (flag) {
            btn_logout.setVisibility(View.VISIBLE);
        } else {
            btn_logout.setVisibility(View.GONE);
        }
    }
    void askOverLayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission required");
            builder.setMessage("UKAMEE supports video call. In order to get video call request, you must allow overlay permission.");
            builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, req_code_overlay);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
    void checkVCallRequest() {
        MyUtils.mDatabase.child(MyUtils.tbl_vcall).orderByChild("receiver_id").equalTo(MyUtils.cur_user.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        Vcall vcall = datas.getValue(Vcall.class);
                        vcall._id = datas.getKey();
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(vcall.sender_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user = snapshot.getValue(User.class);
                                    user.uid = snapshot.getKey();
                                    showCustomPopupMenu(user, vcall);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showCustomPopupMenu(User user, Vcall vcall)
    {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.ring);
        mp.setLooping(true);
        mp.start();

        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        // LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // View view = layoutInflater.inflate(R.layout.dummy_layout, null);
        ViewGroup valetModeWindow = (ViewGroup) View.inflate(this, R.layout.dialog_vcall_coming, null);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity= Gravity.TOP;
        params.x=0;
        params.y=0;
        windowManager.addView(valetModeWindow, params);

        ImageView img_photo = valetModeWindow.findViewById(R.id.img_photo);
        TextView txt_name = valetModeWindow.findViewById(R.id.txt_name);
        Glide.with(this).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_white).fitCenter()).into(img_photo);
        txt_name.setText(user.firstname + " " + user.lastname);
        valetModeWindow.findViewById(R.id.btn_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                windowManager.removeView(valetModeWindow);
                App.goToJoinVideoCall((user.uid + MyUtils.cur_user.uid), MainActivityPatient.this);
            }
        });
        valetModeWindow.findViewById(R.id.btn_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vcall._id).removeValue();
                windowManager.removeView(valetModeWindow);
            }
        });
        valetModeWindow.findViewById(R.id.btn_ignore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vcall._id).removeValue();
                windowManager.removeView(valetModeWindow);
            }
        });
//        window.setBackgroundDrawableResource(android.R.color.transparent);
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
    void updateUserInfo() {
        App.setStatus(1);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MyUtils.cur_user = snapshot.getValue(User.class);
                    MyUtils.cur_user.uid = snapshot.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void setDisconnectCase() {
        DatabaseReference ref_status = MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("status");
        OnDisconnect onDisconnect = ref_status.onDisconnect();
        onDisconnect.setValue(0);

        ref_status = MyUtils.mDatabase.child(MyUtils.tbl_vcall).orderByChild("sender_id").equalTo(MyUtils.cur_user.uid).getRef();
        onDisconnect = ref_status.onDisconnect();
        onDisconnect.setValue(null);
    }

    private void selectFragment(Fragment fragment) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }
    void initTabs() {
        img_friends.setColorFilter(getResources().getColor(R.color.gray));
        img_chatbot.setColorFilter(getResources().getColor(R.color.gray));
        img_settings.setColorFilter(getResources().getColor(R.color.gray));
        ly_friends.setBackground(getResources().getDrawable(R.drawable.oval_frame_primary_dark));
        ly_chatbot.setBackground(getResources().getDrawable(R.drawable.oval_frame_primary_50_dark));
        ly_settings.setBackground(getResources().getDrawable(R.drawable.oval_frame_primary_dark));
    }
    public void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.are_you_sure_to_quit_the_app));
        builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                ActivityCompat.finishAffinity(MainActivityPatient.this);
                System.exit(0);
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }
    public void createDirectory() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] _permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, _permissions, grantResults);
        if (grantResults.length > 0) {
            if  (requestCode == MY_PERMISSION_STORAGE) {
                permissionCallback.permissionGranted(requestCode);
//                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
            } else if (requestCode == MY_PERMISSION_CALL) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                }
            } else if  (requestCode == MY_PERMISSION_CAMERA) {
//                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
                permissionCallback.permissionGranted(requestCode);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == req_code_overlay) {
            askOverLayPermission();
        }
    }
}