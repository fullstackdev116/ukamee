package com.ediattah.yakko.FragmentAdmin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ediattah.yakko.Adapter.PatientListAdapter;
import com.ediattah.yakko.MainActivityAdmin;
import com.ediattah.yakko.Model.History;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.QAHistoryActivity;
import com.ediattah.yakko.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatbotAdminFragment extends Fragment {
    MainActivityAdmin activity;
    PatientListAdapter listAdapter;
    ListView listView;
    ArrayList<User> arrayList = new ArrayList<>();
    ArrayList<String> userIDs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chatbot_admin, container, false);
        activity.setTitle("Q & A History");
        activity.setLogoutVisible(false);

        listView = v.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, QAHistoryActivity.class);
                intent.putExtra("user", arrayList.get(position));
                startActivity(intent);
            }
        });
        listAdapter = new PatientListAdapter(activity, arrayList);
        listView.setAdapter(listAdapter);
        loadUsers();
        return v;
    }
    void loadUsers() {
        MyUtils.mDatabase.child(MyUtils.tbl_history).orderByChild("admin_id").equalTo(MyUtils.cur_user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear(); userIDs.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot datas:snapshot.getChildren()) {
                        History history = datas.getValue(History.class);
                        history._id = datas.getKey();
                        if (!userIDs.contains(history.patient_id)) {
                            userIDs.add(history.patient_id);
                            MyUtils.mDatabase.child(MyUtils.tbl_user).child(history.patient_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        User user = snapshot.getValue(User.class);
                                        user.uid = snapshot.getKey();
                                        arrayList.add(user);
                                    }
                                    listAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
                if (userIDs.size() == 0) {
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
                    listAdapter = new PatientListAdapter(activity, arrayList);
                    listView.setAdapter(listAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityAdmin) context;
    }
}