package com.ediattah.yakko.FragmentAdmin;

import static android.graphics.Color.WHITE;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.App;
import com.ediattah.yakko.LoginActivity;
import com.ediattah.yakko.MainActivityAdmin;
import com.ediattah.yakko.Model.Feedback;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.Service.PermissionCallback;
import com.ediattah.yakko.Service.SmsListener;
import com.ediattah.yakko.Service.SmsReceiver;
import com.ediattah.yakko.SupportDetailActivity;
import com.ediattah.yakko.idcamera.utils.PermissionUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jkb.vcedittext.VerificationAction;
import com.jkb.vcedittext.VerificationCodeEditText;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsAdminFragment extends Fragment {
    MainActivityAdmin activity;
    ImageView img_photo;
    EditText edit_phone, edit_firstname, edit_lastname, edit_address, edit_ID;
    TextView txt_key, txt_rate;
    RatingBar rate;
    private Uri cameraUri;
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
    Uri user_photo;
    int MY_PERMISSION_CAMERA = 101;
    String firstname, lastname, address, ID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings_admin, container, false);
        activity.setTitle("Settings");
        activity.setLogoutVisible(true);

        img_photo = v.findViewById(R.id.img_photo);
        edit_phone = v.findViewById(R.id.edit_phone);
        edit_firstname = v.findViewById(R.id.edit_firstname);
        edit_lastname = v.findViewById(R.id.edit_lastname);
        edit_address = v.findViewById(R.id.edit_address);
        edit_ID = v.findViewById(R.id.edit_ID);
        txt_key = v.findViewById(R.id.txt_key);
        txt_rate = v.findViewById(R.id.txt_rate);
        rate = v.findViewById(R.id.rate);
        v.findViewById(R.id.img_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(activity, MY_PERMISSION_CAMERA,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                if (!checkPermissionFirst) {
                    DesignToast.makeText(activity, activity.getResources().getString(R.string.enable_permissions), DesignToast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                } else {
                    displayChoiceDialog();
                }
            }
        });
        v.findViewById(R.id.ly_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedbackDialog();
            }
        });
        activity.permissionCallback = new PermissionCallback() {
            @Override
            public void permissionGranted(int code) {
                if (code == activity.MY_PERMISSION_CAMERA) {
                    displayChoiceDialog();
                }
            }
        };
        v.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstname = edit_firstname.getText().toString().trim();
                lastname = edit_lastname.getText().toString().trim();
                address = edit_address.getText().toString().trim();
                ID = edit_ID.getText().toString().trim();
                if (firstname.length()*lastname.length()*address.length()*ID.length() == 0) {
                    MyUtils.showAlert(activity, getResources().getString(R.string.warning), getResources().getString(R.string.please_fill_in_blank_field));
                    return;
                }
                App.hideKeyboard(activity);
                if (user_photo == null) {
                    updateUserInfo();
                } else {
                    uploadUserPhotoToFirebase();
                }
            }
        });
        loadProfile();
        return v;
    }

    public void openFeedbackDialog() {
        final Dialog dlg = new Dialog(activity);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_feedbacks, null);
        LinearLayout ly_cover = view.findViewById(R.id.ly_cover);
        ly_cover.removeAllViews();
        MyUtils.mDatabase.child(MyUtils.tbl_feedback).orderByChild("admin_id").equalTo(MyUtils.cur_user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        Feedback feedback = datas.getValue(Feedback.class);
                        feedback._id = datas.getKey();
                        LayoutInflater inflater = LayoutInflater.from(activity);
                        View view = inflater.inflate(R.layout.cell_feedback, null);
                        RatingBar ratingBar = view.findViewById(R.id.rate);
                        TextView txt_name = view.findViewById(R.id.txt_name);
                        TextView txt_feedback = view.findViewById(R.id.txt_feedback);
                        TextView txt_date = view.findViewById(R.id.txt_date);
                        ImageView img_photo = view.findViewById(R.id.img_photo);
                        txt_feedback.setText(feedback.feedback);
                        txt_date.setText(feedback.date);
                        ratingBar.setRating(feedback.rate);
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(feedback.user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user1 = snapshot.getValue(User.class);
                                    txt_name.setText(user1.firstname + " " + user1.lastname);
                                    Glide.with(activity).load(user1.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ly_cover.addView(view);
                    }
                } else {
                    TextView txt = new TextView(activity);
                    txt.setText("No feedback");
                    txt.setTextSize(16);
                    ly_cover.addView(txt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(view);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dlg.show();

    }
    void updateUserInfo() {
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("firstname").setValue(firstname);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("lastname").setValue(lastname);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("address").setValue(address);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("ID").setValue(ID);
        MyUtils.cur_user.firstname = firstname;
        MyUtils.cur_user.lastname = lastname;
        MyUtils.cur_user.address = address;
        MyUtils.cur_user.ID = ID;
        loadProfile();
        DesignToast.makeText(activity, activity.getResources().getString(R.string.user_updated_successfully), DesignToast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
    }
    void loadProfile() {
        edit_phone.setText(MyUtils.cur_user.phone);
        edit_firstname.setText(MyUtils.cur_user.firstname);
        edit_lastname.setText(MyUtils.cur_user.lastname);
        edit_address.setText(MyUtils.cur_user.address);
        txt_key.setText("KEY: " + MyUtils.cur_user.key);
        txt_rate.setText(String.valueOf(MyUtils.cur_user.rate));
        rate.setRating(MyUtils.cur_user.rate);
        edit_ID.setText(MyUtils.cur_user.ID);
        Glide.with(activity).load(MyUtils.cur_user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar).fitCenter()).into(img_photo);
    }
    public void uploadUserPhotoToFirebase() {
        activity.showProgress();
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        final StorageReference file_refer = MyUtils.mStorage.child(MyUtils.storage_user+ts);
        file_refer.putFile(user_photo, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file_refer.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        activity.dismissProgress();
                        String downloadUrl = uri.toString();
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("photo").setValue(downloadUrl);
                        MyUtils.cur_user.photo = downloadUrl;
                        updateUserInfo();
                    }
                });
            }

        });
    }
    private void displayChoiceDialog() {
        String choiceString[] = new String[] {getResources().getString(R.string.gallery) ,getResources().getString(R.string.camera)};
        AlertDialog.Builder dialog= new AlertDialog.Builder(activity);
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
            Context ctx = requireContext();
            String authorities = ctx.getPackageName() + AUTHORITY_SUFFIX;
            cameraUri = FileProvider.getUriForFile(ctx, authorities, createImageFile());
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
        Glide.with(activity).load(uri).apply(new RequestOptions().placeholder(R.drawable.ic_avatar).centerCrop()).into(img_photo);
        Uri uriContent = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            uriContent = Uri.parse(uri);
        } else {
            try {
                uriContent = Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), uri, null, null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (uriContent != null) {
            user_photo = uriContent;
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
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                FILE_NAMING_PREFIX + timeStamp + FILE_NAMING_SUFFIX,
                FILE_FORMAT,
                storageDir
        );
    }
    public void showErrorMessage(@NotNull String message) {
        Log.e("Camera Error:", message);
        Toast.makeText(activity, getResources().getString(R.string.cropping_image_failed) + " " + message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] _permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, _permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayChoiceDialog();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                PermissionUtils.checkPermissionFirst(activity, MY_PERMISSION_CAMERA,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
            }
        }
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityAdmin) context;
    }
}