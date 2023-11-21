package com.ediattah.yakko.Adapter;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.App;
import com.ediattah.yakko.BotActivity;
import com.ediattah.yakko.Model.MessageBot;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.QAActivity;
import com.ediattah.yakko.R;
import com.ediattah.yakko.SupportDetailActivity;

import java.util.ArrayList;

public class BotRecyclerAdapter extends RecyclerView.Adapter<BotRecyclerAdapter.MessageViewHolder> {
    private ArrayList<MessageBot> messages;
    private BotActivity context;

    public BotRecyclerAdapter(BotActivity context, ArrayList<MessageBot> messages){
        this.context = context;
        this.messages = messages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sentLayout;
        private LinearLayout receivedLayout;
        private TextView sentText;
        private TextView receivedText;
        private ImageView img_photo;
        private LinearLayout ly_tool;
        private ImageButton btn_chat, btn_call, btn_vcall;

        public MessageViewHolder(final View itemView) {
            super(itemView);
            sentLayout = itemView.findViewById(R.id.sentLayout);
            receivedLayout = itemView.findViewById(R.id.receivedLayout);
            sentText = itemView.findViewById(R.id.sentTextView);
            receivedText= itemView.findViewById(R.id.receivedTextView);
            img_photo = itemView.findViewById(R.id.img_photo);
            ly_tool = itemView.findViewById(R.id.ly_tool);
            btn_chat = itemView.findViewById(R.id.btn_chat);
            btn_call = itemView.findViewById(R.id.btn_call);
            btn_vcall = itemView.findViewById(R.id.btn_vcall);
        }
    }

    @NonNull
    @Override
    public BotRecyclerAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_bot, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BotRecyclerAdapter.MessageViewHolder holder, int position) {
        String message = messages.get(position).getMessage();
        User doctor = messages.get(position).getDoctor();
        boolean type = messages.get(position).getType();

        if(type){
            //If a message is sent
            holder.sentLayout.setVisibility(LinearLayout.VISIBLE);
            holder.sentText.setText(message);
            // Set visibility as GONE to remove the space taken up
            holder.receivedLayout.setVisibility(LinearLayout.GONE);
        }
        else{
            //Message is received
            holder.receivedLayout.setVisibility(LinearLayout.VISIBLE);
            holder.receivedText.setText(message);
            // Set visibility as GONE to remove the space taken up
            holder.sentLayout.setVisibility(LinearLayout.GONE);
            holder.img_photo.setVisibility(View.GONE);
            holder.ly_tool.setVisibility(View.GONE);
            if (doctor != null) {
                holder.img_photo.setVisibility(View.VISIBLE);
                Glide.with(context).load(doctor.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_white).fitCenter()).into(holder.img_photo);
                holder.ly_tool.setVisibility(View.VISIBLE);

            }
        }
        holder.btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(WRITE_EXTERNAL_STORAGE);
                    arrPermissionRequests.add(READ_EXTERNAL_STORAGE);
                    ActivityCompat.requestPermissions(context, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), context.MY_PERMISSION_STORAGE);
                    return;
                } else {
                    // call
                    context.createDirectory();
                    App.goToChatPage(context, doctor.uid);
                }
            }
        });
        holder.btn_vcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.goToStartVideoCallPage(doctor, context);
            }
        });
        holder.btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(CALL_PHONE);
                    ActivityCompat.requestPermissions(context, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), context.MY_PERMISSION_CALL);
                } else {
                    // call
                    App.dialNumber(doctor.phone, context);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
