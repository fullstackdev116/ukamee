package com.ediattah.yakko.Adapter;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.App;
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.Model.Friend;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.Service.PermissionCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PatientGalleryAdapter extends BaseAdapter implements View.OnClickListener{
    private ArrayList<User> users;
    private MainActivityPatient activity;

    public PatientGalleryAdapter(ArrayList<User> users, MainActivityPatient activity) {
        this.users = users;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return users.size();
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=layoutInflater.inflate(R.layout.cell_gallery_patient,null);
        ImageView img_photo=view.findViewById(R.id.img_photo);
        ImageView img_membership = view.findViewById(R.id.img_membership);
        ImageView img_option=(ImageView)view.findViewById(R.id.img_option);
        TextView txt_connected=(TextView)view.findViewById(R.id.txt_connected);
        TextView txt_name=(TextView)view.findViewById(R.id.txt_name);
        TextView txt_phone=(TextView)view.findViewById(R.id.txt_phone);
        User user=users.get(i);
        txt_name.setText(user.firstname + " " + user.lastname);
        txt_phone.setText("+" + user.phone);
        switch (user.membership) {
            case 0:
                img_membership.setVisibility(View.GONE);
                break;
            case 1:
                img_membership.setVisibility(View.VISIBLE);
                img_membership.setImageDrawable(activity.getDrawable(R.drawable.ic_membership_plus));
                img_membership.setColorFilter(activity.getColor(R.color.plus));
                break;
            case 2:
                img_membership.setVisibility(View.VISIBLE);
                img_membership.setImageDrawable(activity.getDrawable(R.drawable.ic_membership_pro));
                img_membership.setColorFilter(activity.getColor(R.color.pro));
                break;
        }
        Glide.with(activity).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);
        img_option.setOnClickListener(this);
        img_option.setTag(R.string.key0, "disconnected");
        img_option.setTag(R.string.key1, user);
        img_option.setTag(R.string.key2, new Friend());

        MyUtils.mDatabase.child(MyUtils.tbl_friend).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot datas:dataSnapshot.getChildren()) {
                        Friend friend = datas.getValue(Friend.class);
                        friend._id = datas.getKey();

                        if (friend.sender_id.equals(MyUtils.cur_user.uid) && friend.receiver_id.equals(user.uid)) {
                            txt_connected.setVisibility(View.VISIBLE);
                            if (friend.state == 0) {
                                img_option.setTag(R.string.key0, "requested_going");
                                txt_connected.setBackgroundColor(activity.getResources().getColor(R.color.primary));
                                txt_connected.setText("Requested");
                            } else {
                                img_option.setTag(R.string.key0, "connected");
                                txt_connected.setText("Connected");
                                txt_connected.setBackgroundColor(activity.getResources().getColor(R.color.green));
                            }
                            img_option.setTag(R.string.key2, friend);
                        } else if (friend.receiver_id.equals(MyUtils.cur_user.uid) && friend.sender_id.equals(user.uid)) {
                            txt_connected.setVisibility(View.VISIBLE);
                            if (friend.state == 0) {
                                img_option.setTag(R.string.key0, "requested_coming");
                                txt_connected.setBackgroundColor(activity.getResources().getColor(R.color.teal_700));
                                txt_connected.setText("Waiting");
                            } else {
                                img_option.setTag(R.string.key0, "connected");
                                txt_connected.setText("Connected");
                                txt_connected.setBackgroundColor(activity.getResources().getColor(R.color.green));
                            }
                            img_option.setTag(R.string.key2, friend);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        showOptionMenu(view);
    }

    private void showOptionMenu(View view) {
        PopupMenu popupMenu=new PopupMenu(activity,view);
        String conn = (String)view.getTag(R.string.key0);
        User user = (User) view.getTag(R.string.key1);
        Friend friend = (Friend) view.getTag(R.string.key2);
        if (conn.equals("connected")) {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_connected_menu,popupMenu.getMenu());
        } else if (conn.equals("requested_going")) {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_requested_going_menu,popupMenu.getMenu());
        } else if (conn.equals("requested_coming")) {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_requested_coming_menu,popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_disconnected_menu,popupMenu.getMenu());
        }
        popupMenu.show();
        String finalUser_id = user.uid;
        String finalFriend_id = friend._id;

        activity.permissionCallback = new PermissionCallback() {
            @Override
            public void permissionGranted(int code) {
                if (code == activity.MY_PERMISSION_STORAGE) {
                    activity.createDirectory();
                    App.goToChatPage(activity, finalUser_id);
                }
            }
        };
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.chat:
                        if (ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ArrayList<String> arrPermissionRequests = new ArrayList<>();
                            arrPermissionRequests.add(WRITE_EXTERNAL_STORAGE);
                            arrPermissionRequests.add(READ_EXTERNAL_STORAGE);
                            ActivityCompat.requestPermissions(activity, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), activity.MY_PERMISSION_STORAGE);
                            return false;
                        } else {
                            // call
                            activity.createDirectory();
                            App.goToChatPage(activity, finalUser_id);
                        }
                        return true;
                    case R.id.call:
                        if (ContextCompat.checkSelfPermission(activity, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ArrayList<String> arrPermissionRequests = new ArrayList<>();
                            arrPermissionRequests.add(CALL_PHONE);
                            ActivityCompat.requestPermissions(activity, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), activity.MY_PERMISSION_CALL);
                            return false;
                        } else {
                            // call
                            App.dialNumber(user.phone, activity);
                        }
                        return true;
                    case R.id.disconnect:
                        MyUtils.mDatabase.child(MyUtils.tbl_friend).child(finalFriend_id).setValue(null);
                        DesignToast.makeText(activity, "Successfully disconnected", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                        notifyDataSetChanged();
                        return true;
                    case R.id.cancel:
                        MyUtils.mDatabase.child(MyUtils.tbl_friend).child(finalFriend_id).setValue(null);
                        DesignToast.makeText(activity, "Request has been canceled successfully", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                        notifyDataSetChanged();
                        return true;
                    case R.id.connect:
                        Friend friend = new Friend("", MyUtils.cur_user.uid, finalUser_id, 0);
                        MyUtils.mDatabase.child(MyUtils.tbl_friend).push().setValue(friend);
                        notifyDataSetChanged();
                        DesignToast.makeText(activity, "Connect request has been sent successfully", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                        return true;
                    case R.id.accept:
                        MyUtils.mDatabase.child(MyUtils.tbl_friend).child(finalFriend_id).child("state").setValue(1);
                        notifyDataSetChanged();
                        DesignToast.makeText(activity, "Successfully connected", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                        return true;
                    case R.id.reject:
                        MyUtils.mDatabase.child(MyUtils.tbl_friend).child(finalFriend_id).setValue(null);
                        DesignToast.makeText(activity, "Successfully rejected", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                        notifyDataSetChanged();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
}
