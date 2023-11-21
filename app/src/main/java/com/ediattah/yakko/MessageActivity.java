package com.ediattah.yakko;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ediattah.yakko.Adapter.MessageListAdapter;
import com.ediattah.yakko.Model.ChatRoom;
import com.ediattah.yakko.Model.MessageChat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessageActivity extends BaseActivity {
    ListView listView;
    MessageListAdapter adapter;
    ArrayList<ChatRoom> array_message = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Message");
        progressDialog = new ProgressDialog(this);
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("roomId", array_message.get(i)._id);
                startActivity(intent);
            }
        });
        loadMessages();
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
    void loadMessages() {
//        App.setPreferenceInt(App.NewMessage, 0);
        array_message.clear();
        adapter = new MessageListAdapter(MessageActivity.this, array_message);
        listView.setAdapter(adapter);
        showProgress();
        MyUtils.mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(MyUtils.tbl_chat)) {
                    addMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    void addMessages() {
        MyUtils.mDatabase.child(MyUtils.tbl_chat).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if (flag) {
                        ChatRoom chatRoom = new ChatRoom();
                        chatRoom._id = dataSnapshot.getKey();
                        int my_index = chatRoom._id.indexOf(MyUtils.cur_user.uid);
                        String other_uid;
                        if (my_index == 0) {
                            other_uid = chatRoom._id.substring(MyUtils.cur_user.uid.length());
                        } else {
                            other_uid = chatRoom._id.substring(0, chatRoom._id.length()-MyUtils.cur_user.uid.length());
                        }
                        chatRoom.isTyping = dataSnapshot.child("isTyping").child(other_uid).getValue(boolean.class);
                        for (DataSnapshot datas:dataSnapshot.child("messages").getChildren()) {
                            MessageChat message = datas.getValue(MessageChat.class);
                            chatRoom.messages.add(message);
                        }
                        array_message.add(chatRoom);

                    }
                }
                adapter.notifyDataSetChanged();
                dismissProgress();
//                if (array_message.size() == 0) {
//                    String[] listItems = {"No message"};
//                    ArrayAdapter simpleAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listItems) {
//                        @Override
//                        public View getView(int position, View convertView, ViewGroup parent) {
//                            TextView textView = (TextView) super.getView(position, convertView, parent);
//                            textView.setTextColor(getResources().getColor(R.color.gray));
//                            return textView;
//                        }
//                    };
//                    listView.setAdapter(simpleAdapter);
//                } else {
//
//                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if ( flag) {
                        ChatRoom chatRoom = new ChatRoom();
                        chatRoom._id = dataSnapshot.getKey();
                        int my_index = chatRoom._id.indexOf(MyUtils.cur_user.uid);
                        String other_uid;
                        if (my_index == 0) {
                            other_uid = chatRoom._id.substring(MyUtils.cur_user.uid.length());
                        } else {
                            other_uid = chatRoom._id.substring(0, chatRoom._id.length()-MyUtils.cur_user.uid.length());
                        }
                        chatRoom.isTyping = dataSnapshot.child("isTyping").child(other_uid).getValue(boolean.class);
                        for (DataSnapshot datas:dataSnapshot.child("messages").getChildren()) {
                            MessageChat message = datas.getValue(MessageChat.class);
                            chatRoom.messages.add(message);
                        }
                        int index_update = 0;
                        for (int i = 0; i < array_message.size(); i++) {
                            ChatRoom room = array_message.get(i);
                            if (room._id.equals(chatRoom._id)) {
                                array_message.remove(i);
                                index_update = i;
                                break;
                            }
                        }
                        array_message.add(index_update, chatRoom);
                    }
                }
                adapter.notifyDataSetChanged();
                dismissProgress();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    boolean flag = dataSnapshot.getKey().contains(MyUtils.cur_user.uid);
                    if (flag) {
                        ChatRoom chatRoom = new ChatRoom();
                        chatRoom._id = dataSnapshot.getKey();
                        int my_index = chatRoom._id.indexOf(MyUtils.cur_user.uid);
                        String other_uid;
                        if (my_index == 0) {
                            other_uid = chatRoom._id.substring(MyUtils.cur_user.uid.length());
                        } else {
                            other_uid = chatRoom._id.substring(0, chatRoom._id.length()-MyUtils.cur_user.uid.length());
                        }
                        chatRoom.isTyping = dataSnapshot.child("isTyping").child(other_uid).getValue(boolean.class);
                        for (DataSnapshot datas:dataSnapshot.child("messages").getChildren()) {
                            MessageChat message = datas.getValue(MessageChat.class);
                            chatRoom.messages.add(message);
                        }
                        for (int i = 0; i < array_message.size(); i++) {
                            ChatRoom room = array_message.get(i);
                            if (room._id.equals(chatRoom._id)) {
                                array_message.remove(i);
                                break;
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                dismissProgress();
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
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}