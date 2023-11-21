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
import com.ediattah.yakko.FragmentAdmin.FriendsAdminFragment;
import com.ediattah.yakko.MainActivityAdmin;
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.Model.Friend;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.Service.PermissionCallback;
import com.ediattah.yakko.SupportDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminGalleryAdapter extends BaseAdapter implements View.OnClickListener{
    private ArrayList<User> users;
    private MainActivityAdmin activity;
    private FriendsAdminFragment fragment;

    public AdminGalleryAdapter(ArrayList<User> users, MainActivityAdmin activity, FriendsAdminFragment fragment) {
        this.users = users;
        this.activity = activity;
        this.fragment = fragment;
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
        img_option.setTag(R.string.key1, user);

        return view;
    }

    @Override
    public void onClick(View view) {
        showOptionMenu(view);
    }

    private void showOptionMenu(View view) {
        PopupMenu popupMenu=new PopupMenu(activity,view);
        User user = (User) view.getTag(R.string.key1);
        fragment.sel_user = user;
        if (user.membership == 2) {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_pro_menu,popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.gallery_free_plus_menu,popupMenu.getMenu());
        }
        popupMenu.show();
        String finalUser_id = user.uid;

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
                    case R.id.vcall:
                        App.goToStartVideoCallPage(user, activity);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
}
