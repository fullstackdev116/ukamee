package com.ediattah.yakko.FragmentAdmin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ediattah.yakko.Adapter.AdminGalleryAdapter;
import com.ediattah.yakko.Adapter.PatientGalleryAdapter;
import com.ediattah.yakko.Adapter.SupportListAdapter;
import com.ediattah.yakko.MainActivityAdmin;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.Model.Vcall;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import timber.log.Timber;

public class FriendsAdminFragment extends Fragment {
    MainActivityAdmin activity;
    GridView gallery;
    EditText edit_search;
    ImageView img_cancel;
    AdminGalleryAdapter adminGalleryAdapter;
    ArrayList<User> users = new ArrayList<>();
    ArrayList<User> users_filter = new ArrayList<>();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };
    String vCallKey = "";
    public User sel_user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends_admin, container, false);
        activity.setTitle("Patients");
        activity.setLogoutVisible(false);

        gallery = v.findViewById(R.id.gallery);
        edit_search = v.findViewById(R.id.edit_search);
        img_cancel = v.findViewById(R.id.img_cancel);
        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_search.setText("");
            }
        });
        adminGalleryAdapter = new AdminGalleryAdapter(users_filter, activity, this);
        gallery.setAdapter(adminGalleryAdapter);
        JitSiConfig();

        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = edit_search.getText().toString().trim();
                if (str.length() > 0) {
                    img_cancel.setVisibility(View.VISIBLE);
                } else {
                    img_cancel.setVisibility(View.GONE);
                }
                filterByKey(str);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return v;
    }
    void filterByKey(String key) {
        users_filter.clear();
        if (key.length() == 0) {
            users_filter = new ArrayList<>(users);
        } else {
            for (User user:users) {
                String name = user.firstname + " " + user.lastname;
                if (name.toLowerCase().contains(key.toLowerCase())) {
                    users_filter.add(user);
                } else if (user.phone.contains(key)) {
                    users_filter.add(user);
                }
            }
        }
        showGallery();
    }
    void loadAllUsers() {
        users.clear();users_filter.clear();
        MyUtils.mDatabase.child(MyUtils.tbl_user).orderByChild("type").equalTo("PATIENT").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot datas:dataSnapshot.getChildren()) {
                        User user = datas.getValue(User.class);
                        user.uid = datas.getKey();
                        if (user.uid.equals(MyUtils.cur_user.uid)) continue;
                        if (user.state == 1) {
                            users.add(user);
                        }
                    }
                    users_filter = new ArrayList<>(users);
                }
                showGallery();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    void showGallery() {
        if (users_filter.size() == 0) {
            String[] listItems = {"No data"};
            ArrayAdapter simpleAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, listItems) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor(getResources().getColor(R.color.gray));
                    textView.setGravity(Gravity.CENTER);
                    return textView;
                }
            };
            gallery.setAdapter(simpleAdapter);
        } else {
            adminGalleryAdapter = new AdminGalleryAdapter(users_filter, activity, FriendsAdminFragment.this);
            gallery.setAdapter(adminGalleryAdapter);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadAllUsers();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityAdmin) context;
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

        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, intentFilter);
    }


    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        final MediaPlayer mp = MediaPlayer.create(activity, R.raw.calling);
        mp.setLooping(true);

        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    mp.start();
                    Vcall vcall = new Vcall("", MyUtils.cur_user.uid, sel_user.uid, 0);
                    vCallKey = MyUtils.mDatabase.child(MyUtils.tbl_vcall).push().getKey();
                    MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vCallKey).setValue(vcall);

                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    Toast.makeText(activity, "CONFERENCE JOINED", Toast.LENGTH_SHORT).show();
                    break;
                case PARTICIPANT_JOINED:
                    mp.stop();
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    Toast.makeText(activity, "PARTICIPANT JOINED", Toast.LENGTH_SHORT).show();
                    break;
                case CONFERENCE_TERMINATED:
                    mp.stop();
                    MyUtils.mDatabase.child(MyUtils.tbl_vcall).child(vCallKey).removeValue();
                    Toast.makeText(activity, "CONFERENCE TERMINATED", Toast.LENGTH_SHORT).show();
                    break;
                case PARTICIPANT_LEFT:
                    mp.stop();
                    Toast.makeText(activity, "PARTICIPANT_LEFT", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(activity).sendBroadcast(hangupBroadcastIntent);
    }
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}