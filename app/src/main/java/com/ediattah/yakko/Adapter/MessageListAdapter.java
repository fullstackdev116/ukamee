package com.ediattah.yakko.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.MessageActivity;
import com.ediattah.yakko.Model.ChatRoom;
import com.ediattah.yakko.Model.MessageChat;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class MessageListAdapter extends BaseAdapter {
    ArrayList<ChatRoom> array_private;
    MessageActivity activity;

    public MessageListAdapter(MessageActivity _activity, ArrayList<ChatRoom> _array_private) {
        activity = _activity;
        this.array_private = _array_private;
    }
    @Override
    public int getCount() {
        return array_private.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(activity);

        view = inflater.inflate(R.layout.cell_message, null);
        final ChatRoom chatRoom = array_private.get(i);
        MessageChat message = chatRoom.messages.get(chatRoom.messages.size()-1);
        String user_id = MyUtils.getChatUserId(chatRoom._id);

        final LinearLayout ly_status = view.findViewById(R.id.ly_status);
        final TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_message = view.findViewById(R.id.txt_message);
        TextView txt_typing = view.findViewById(R.id.txt_typing);
        final ImageView img_photo = view.findViewById(R.id.img_photo);
        txt_message.setText(message.message);
        if (message.message.length() == 0) {
            txt_message.setText("[File Attached]");
            if (message.file.length() == 0) {
                txt_message.setText("[Chat Open]");
            }
        }
        txt_message.setTextColor(activity.getResources().getColor(R.color.dark));

        if (message.receiver_id.equals(MyUtils.cur_user.uid) && !message.seen) {
            txt_message.setTextColor(activity.getResources().getColor(R.color.teal_200));
        }
        if (chatRoom.isTyping) {
            txt_typing.setVisibility(View.VISIBLE);
        } else  {
            txt_typing.setVisibility(View.GONE);
        }
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    User user = dataSnapshot.getValue(User.class);
                    user.uid = dataSnapshot.getKey();
                    txt_name.setText(user.firstname + " " + user.lastname);
                    try {
                        Glide.with(activity).load(user.photo).apply(new RequestOptions()
                                .placeholder(R.drawable.ic_avatar_white).centerCrop().dontAnimate()).into(img_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (user.status == 0) {
                        ly_status.setBackground(activity.getResources().getDrawable(R.drawable.status_offline));
                    } else if (user.status == 2) {
                        ly_status.setBackground(activity.getResources().getDrawable(R.drawable.status_away));
                    } else {
                        ly_status.setBackground(activity.getResources().getDrawable(R.drawable.status_online));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
