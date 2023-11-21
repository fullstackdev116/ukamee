package com.ediattah.yakko;

import static android.graphics.Color.WHITE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.ediattah.yakko.Adapter.ChatListAdapter;
import com.ediattah.yakko.Model.MessageChat;
import com.ediattah.yakko.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class ChatActivity extends BaseActivity {
    String roomId;
    User user;
    ListView listView;
    ChatListAdapter chatListAdapter;
    ArrayList<MessageChat> arrayList = new ArrayList<>();
    private Uri imgUri;
    String user_id;
    TextView txt_title;
    LinearLayout ly_status;
    ImageView img_photo;
    TextView txt_typing;
    boolean flag_typing = false;
    int VOICE_REQUEST_CODE = 900;
    String audioFilePath = "";
    File audioFile;
    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onTakePictureResult);
    private final ActivityResultLauncher<String> chooseGallery =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onChooseGalleryResult);
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), this::onCropImageResult);
    static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    static final String FILE_NAMING_PREFIX = "JPEG_";
    static final String FILE_NAMING_SUFFIX = "_";
    static final String FILE_FORMAT = ".jpg";
    static final String AUTHORITY_SUFFIX = ".cropper.fileprovider";
    private Uri cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        App.hideKeyboard(this);
        txt_title = findViewById(R.id.txt_title);
        txt_typing = findViewById(R.id.txt_typing);
        img_photo = findViewById(R.id.img_photo);
        ly_status = findViewById(R.id.ly_status);
        roomId = getIntent().getStringExtra("roomId");
        user_id = MyUtils.getChatUserId(roomId);
        listView = findViewById(R.id.listView);
        chatListAdapter = new ChatListAdapter(this, arrayList);
        chatListAdapter.roomId = roomId;
        listView.setAdapter(chatListAdapter);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btn_file = findViewById(R.id.btn_file);
        btn_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayChoiceDialog();
            }
        });
        final Button btn_send = findViewById(R.id.btn_send);
        btn_send.setTag("record");
        final EditText edit_message = findViewById(R.id.edit_message);
        flag_typing = false;
        edit_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("isTyping").child(MyUtils.cur_user.uid).setValue(true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("isTyping").child(MyUtils.cur_user.uid).setValue(false);
                    }
                }, 3000);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edit_message.getText().toString().trim().length() > 0) {
                    btn_send.setTag("send");
                    btn_send.setBackground(getResources().getDrawable(R.drawable.ic_send));
                } else {
                    btn_send.setTag("record");
                    btn_send.setBackground(getResources().getDrawable(R.drawable.ic_record));
                }
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_send.getTag().equals("send")) {
                    String msg = edit_message.getText().toString().trim();
                    if (msg.length() == 0) {
                        return;
                    }
                    MessageChat message = new MessageChat("", MyUtils.cur_user.uid, user_id, msg, "", "", System.currentTimeMillis(), false);
                    MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("messages").push().setValue(message);
                    edit_message.setText("");
                    App.sendPushMessage(user.token, "Chat from " + MyUtils.cur_user.firstname + " " + MyUtils.cur_user.lastname, message.message, "", ChatActivity.this, MyUtils.PUSH_CHAT, MyUtils.cur_user.uid, user.type);
                } else if (btn_send.getTag().equals("record")) {
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
                        ArrayList<String> arrPermissionRequests = new ArrayList<>();
                        arrPermissionRequests.add(Manifest.permission.RECORD_AUDIO);
                        arrPermissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        ActivityCompat.requestPermissions(ChatActivity.this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), 201);
                        return;
                    } else {
                        goToVoiceRecordPage();
                    }
                }
            }
        });

        load_user(user_id);
        readMessages();
        watchTypingEvent();
    }
    void goToVoiceRecordPage() {
        Time time = new Time();
        time.setToNow();
//                    return new File(App.MY_AUDIO_PATH + File.separator + time.format("%Y%m%d%H%M%S") + "." + suffix);

        audioFilePath = App.MY_AUDIO_PATH + File.separator + time.format("%Y%m%d%H%M%S") + ".wav";

        int color = getResources().getColor(R.color.primary);
        AndroidAudioRecorder.with(ChatActivity.this)
                // Required
                .setFilePath(audioFilePath)
                .setColor(color)
                .setRequestCode(VOICE_REQUEST_CODE)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.MONO)
                .setSampleRate(AudioSampleRate.HZ_16000)
                .setKeepDisplayOn(true)

                // Start recording
                .record();
    }
    void watchTypingEvent() {
        MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("isTyping").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    boolean isTyping = Boolean.valueOf(dataSnapshot.getValue().toString());
                    if (isTyping) {
                        txt_typing.setVisibility(View.VISIBLE);
                    } else {
                        txt_typing.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void readMessages() {
        arrayList.clear();
        MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.getValue()!=null) {
                    MessageChat message = dataSnapshot.getValue(MessageChat.class);
                    message._id = dataSnapshot.getKey();
                    arrayList.add(message);
                    chatListAdapter.notifyDataSetChanged();
                    listView.setSelection(arrayList.size()-1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue()!=null) {
                    MessageChat message = dataSnapshot.getValue(MessageChat.class);
                    message._id = dataSnapshot.getKey();
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (message._id.equals(arrayList.get(i)._id)) {
                            arrayList.set(i, message);
                            break;
                        }
                    }
//                    arrayList.add(message);
                    chatListAdapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(arrayList.size()-1);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    MessageChat message = dataSnapshot.getValue(MessageChat.class);
                    message._id = dataSnapshot.getKey();
                    for (int i = 0; i < arrayList.size(); i++) {
                        MessageChat message1 = arrayList.get(i);
                        if (message1._id.equals(message._id)) {
                            arrayList.remove(i);
                            chatListAdapter.notifyDataSetChanged();
                            listView.smoothScrollToPosition(i-1);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void load_user(String user_id) {
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    user = dataSnapshot.getValue(User.class);
                    user.uid = dataSnapshot.getKey();
                    txt_title.setText("Chat with " + user.firstname + " " + user.lastname);
                    try {
                        Glide.with(ChatActivity.this).load(user.photo).apply(new RequestOptions()
                                .placeholder(R.drawable.ic_avatar_white).centerCrop().dontAnimate()).into(img_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (user.status == 0) {
                        ly_status.setBackground(getResources().getDrawable(R.drawable.status_offline));
                    } else if (user.status == 2) {
                        ly_status.setBackground(getResources().getDrawable(R.drawable.status_away));
                    } else {
                        ly_status.setBackground(getResources().getDrawable(R.drawable.status_online));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 201: {
                if (grantResults[0] == 0) {
                    goToVoiceRecordPage();
                } else {
                    Toast.makeText(ChatActivity.this, "Permission denied", Toast.LENGTH_SHORT);
                    finish();
                }
                break;
            }
            default:
                Toast.makeText(ChatActivity.this, "Permission denied", Toast.LENGTH_SHORT);
                finish();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = Uri.fromFile(new File(audioFilePath));
                if (uri!=null) {
                    uploadFileToFirebase(uri, "wav");
                }
            }
        }
    }
    void uploadFileToFirebase(Uri uri, final String file_type) {
// Create the file metadata
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading..");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        StorageMetadata metadata = null;
        if (file_type.equals("jpeg")) {
            metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
        } else if (file_type.equals("wav")) {
            metadata = new StorageMetadata.Builder()
                    .setContentType("audio/wav")
                    .build();
        }
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        final StorageReference file_refer = MyUtils.mStorage.child(MyUtils.storage_chat+ts);
        // Listen for state changes, errors, and completion of the upload.
        file_refer.putFile(uri, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                file_refer.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        String downloadUrl = uri.toString();
                        MessageChat message = new MessageChat("", MyUtils.cur_user.uid, user_id, "", downloadUrl, file_type, System.currentTimeMillis(), false);
                        MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("messages").push().setValue(message);
                        App.sendPushMessage(user.token, "Chat from " + MyUtils.cur_user.firstname + " " + MyUtils.cur_user.lastname, "File attached", "",ChatActivity.this, MyUtils.PUSH_CHAT, MyUtils.cur_user.uid, user.type);
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void delete_message(MessageChat message) {
        MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("messages").child(message._id).setValue(null);
    }


    private void displayChoiceDialog() {
        String choiceString[] = new String[] {getResources().getString(R.string.gallery) ,getResources().getString(R.string.camera)};
        AlertDialog.Builder dialog= new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(getResources().getString(R.string.select_image_from));
        dialog.setItems(choiceString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0) {
                    chooseGallery.launch("image/*");
                } else {
                    startTakePicture();
                }

            }
        }).show();
    }

    private void startTakePicture() {
        try {
            String authorities = getPackageName() + AUTHORITY_SUFFIX;
            cameraUri = FileProvider.getUriForFile(this, authorities, createImageFile());
            takePicture.launch(cameraUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onTakePictureResult(boolean success) {
        if (success) { cropPictureWithUri(cameraUri); }
        else { showErrorMessage(getResources().getString(R.string.taking_picture_failed)); }
    }
    public void onChooseGalleryResult(Uri uri) {
        if (uri != null) {
            cropPictureWithUri(uri);
        }
    }

    public void handleCropImageResult(@NotNull String uri) {
        Uri uriContent = null;
        
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            uriContent = Uri.parse(uri);
        } else {
            try {
                uriContent = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), uri, null, null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (uriContent != null) {
            uploadFileToFirebase(uriContent, "jpeg");
        }
    }
    public void onCropImageResult(@NonNull CropImageView.CropResult result) {
        if (result.isSuccessful()) {
            handleCropImageResult(Objects.requireNonNull(result.getUriContent())
                    .toString()
                    .replace("file:", ""));
        } else if (result.equals(CropImage.CancelledResult.INSTANCE)) {
            showErrorMessage(getResources().getString(R.string.cropping_image_was_cancelled_by_the_user));
        } else {
            showErrorMessage(getResources().getString(R.string.cropping_image_failed));
        }
    }
    public void cropPictureWithUri(Uri uri) {
        CropImageContractOptions options = new CropImageContractOptions(uri, new CropImageOptions())
                .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .setMaxZoom(4)
                .setAutoZoomEnabled(true)
                .setMultiTouchEnabled(true)
                .setCenterMoveEnabled(true)
                .setShowCropOverlay(true)
                .setAllowFlipping(true)
                .setSnapRadius(3f)
                .setTouchRadius(48f)
                .setInitialCropWindowPaddingRatio(0.1f)
                .setBorderLineThickness(3f)
                .setBorderLineColor(Color.argb(170, 255, 255, 255))
                .setBorderCornerThickness(2f)
                .setBorderCornerOffset(5f)
                .setBorderCornerLength(14f)
                .setBorderCornerColor(WHITE)
                .setGuidelinesThickness(1f)
                .setGuidelinesColor(R.color.white)
                .setBackgroundColor(Color.argb(119, 0, 0, 0))
                .setMinCropWindowSize(24, 24)
                .setMinCropResultSize(20, 20)
                .setMaxCropResultSize(99999, 99999)
                .setActivityTitle("")
                .setActivityMenuIconColor(0)
                .setOutputUri(null)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(90)
                .setRequestedSize(0, 0)
                .setRequestedSize(0, 0, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
                .setInitialCropWindowRectangle(null)
//                .setInitialRotation(90)
                .setAllowCounterRotation(false)
                .setFlipHorizontally(false)
                .setFlipVertically(false)
                .setCropMenuCropButtonTitle(null)
                .setCropMenuCropButtonIcon(0)
                .setAllowRotation(true)
                .setNoOutputImage(false)
                .setFixAspectRatio(false);
        cropImage.launch(options);
    }
    private File createImageFile() throws IOException {
        SimpleDateFormat timeStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                FILE_NAMING_PREFIX + timeStamp + FILE_NAMING_SUFFIX,
                FILE_FORMAT,
                storageDir
        );
    }
    public void showErrorMessage(@NotNull String message) {
        Log.e("Camera Error:", message);
        Toast.makeText(this, getResources().getString(R.string.cropping_image_failed) + message, Toast.LENGTH_SHORT).show();
    }
}