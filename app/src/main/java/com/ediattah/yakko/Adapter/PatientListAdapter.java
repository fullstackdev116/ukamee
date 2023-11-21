package com.ediattah.yakko.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.MainActivityAdmin;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.R;

import java.util.ArrayList;

public class PatientListAdapter extends BaseAdapter {
    MainActivityAdmin context;
    ArrayList<User> arrayList;

    public PatientListAdapter(MainActivityAdmin _context, ArrayList<User> _arrayList) {
        context = _context;
        arrayList = _arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

//    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.cell_patient, null);
        }
        User user = arrayList.get(i);
        TextView txt_address = view.findViewById(R.id.txt_address);
        TextView txt_name = view.findViewById(R.id.txt_name);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        ImageView img_membership = view.findViewById(R.id.img_membership);
        txt_name.setText(user.firstname + " " + user.lastname);
        txt_address.setText(user.address);
        Glide.with(context).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_white).fitCenter()).into(img_photo);
        if (user.membership == 0) {
            img_membership.setImageDrawable(context.getDrawable(R.drawable.ic_membership_free));
        } else if (user.membership == 1) {
            img_membership.setImageDrawable(context.getDrawable(R.drawable.ic_membership_plus));
        } else {
            img_membership.setImageDrawable(context.getDrawable(R.drawable.ic_membership_pro));
        }

        return view;
    }

}
