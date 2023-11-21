package com.ediattah.yakko.FragmentPatient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.Adapter.PatientGalleryAdapter;
import com.ediattah.yakko.Adapter.SupportListAdapter;
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.SupportDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsPatientFragment extends Fragment {
    MainActivityPatient activity;
    Button btn_friends, btn_supports;
    View view_friends, view_supports;
    GridView gallery;
    ListView listView;
    LinearLayout ly_support, ly_cover_support_top;
    PatientGalleryAdapter patientGalleryAdapter;
    SupportListAdapter listAdapter;
    ArrayList<User> admins = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends_patient, container, false);
        activity.setTitle("Friends");
        activity.setLogoutVisible(false);

        gallery = v.findViewById(R.id.gallery);
        listView = v.findViewById(R.id.listView);
        ly_support = v.findViewById(R.id.ly_support);
        ly_cover_support_top = v.findViewById(R.id.ly_cover_support_top);
        patientGalleryAdapter = new PatientGalleryAdapter(users, activity);
        listAdapter = new SupportListAdapter(activity, admins);
        gallery.setAdapter(patientGalleryAdapter);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = admins.get(position);
                Intent intent = new Intent(activity, SupportDetailActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        btn_friends = v.findViewById(R.id.btn_friends);
        btn_supports = v.findViewById(R.id.btn_supports);
        view_friends = v.findViewById(R.id.view_friends);
        view_supports = v.findViewById(R.id.view_supports);
        btn_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initButtons();
                btn_friends.setTextColor(activity.getResources().getColor(R.color.primary));
                view_friends.setVisibility(View.VISIBLE);
                gallery.setVisibility(View.VISIBLE);
                ly_support.setVisibility(View.GONE);
                loadAllUsers();
            }
        });
        btn_supports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyUtils.cur_user.membership == 2) {
                    initButtons();
                    btn_supports.setTextColor(activity.getResources().getColor(R.color.primary));
                    view_supports.setVisibility(View.VISIBLE);
                    ly_support.setVisibility(View.VISIBLE);
                    gallery.setVisibility(View.GONE);
                    loadAllAdmins();
                } else {
                    MyUtils.showAlert(activity, "Warning", "In order to get supports, you need to buy a PRO membership in settings");
                    return;
                }

            }
        });

//        loadAllUsers();
        return v;
    }
    void loadAllUsers() {
        users.clear();
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
                }
                if (users.size() == 0) {
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
                    patientGalleryAdapter = new PatientGalleryAdapter(users, activity);
                    gallery.setAdapter(patientGalleryAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    void loadAllAdmins() {
        admins.clear(); ly_cover_support_top.removeAllViews();
        MyUtils.mDatabase.child(MyUtils.tbl_user).orderByChild("type").equalTo("ADMIN").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot datas:dataSnapshot.getChildren()) {
                        User user = datas.getValue(User.class);
                        user.uid = datas.getKey();
                        if (user.uid.equals(MyUtils.cur_user.uid)) continue;
                        if (user.state == 1) {
                            admins.add(user);

                            if (user.rate >= 4.5) {  // top rated supports
                                LayoutInflater inflater = LayoutInflater.from(activity);
                                View view = inflater.inflate(R.layout.cell_support_top, null);
                                ImageView img_photo = view.findViewById(R.id.img_photo);
                                Glide.with(activity).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);
                                TextView txt_name = view.findViewById(R.id.txt_name);
                                TextView txt_title = view.findViewById(R.id.txt_title);
                                TextView txt_rate = view.findViewById(R.id.txt_rate);
                                txt_name.setText(user.firstname + " " + user.lastname);
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
                                txt_rate.setText(String.valueOf(user.rate));
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(activity, SupportDetailActivity.class);
                                        intent.putExtra("user", user);
                                        startActivity(intent);
                                    }
                                });
                                ly_cover_support_top.addView(view);
                            }

                        }
                    }
                    if (ly_cover_support_top.getChildCount() == 0) {
                        TextView textView = new TextView(activity);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(15,40,15,40);
                        textView.setLayoutParams(params);
                        textView.setText("There are no top rated supports yet.");
                        ly_cover_support_top.addView(textView);
                    }
                }
                if (admins.size() == 0) {
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
                    listView.setAdapter(simpleAdapter);
                } else {
                    listAdapter = new SupportListAdapter(activity, admins);
                    listView.setAdapter(listAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    void initButtons() {
        btn_friends.setTextColor(activity.getResources().getColor(R.color.gray));
        btn_supports.setTextColor(activity.getResources().getColor(R.color.gray));
        view_supports.setVisibility(View.GONE);
        view_friends.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityPatient) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllAdmins();
        loadAllUsers();
    }
}