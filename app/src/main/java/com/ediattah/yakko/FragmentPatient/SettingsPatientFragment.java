package com.ediattah.yakko.FragmentPatient;

import static android.graphics.Color.WHITE;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.Model.Transaction;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.Service.PermissionCallback;
import com.ediattah.yakko.httpsModule.RestClient;
import com.ediattah.yakko.idcamera.utils.PermissionUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SettingsPatientFragment extends Fragment {
    MainActivityPatient activity;
    ImageView img_photo;
    EditText edit_phone, edit_firstname, edit_lastname, edit_address;
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
    String firstname, lastname, address;
    Button btn_profile, btn_membership;
    View view_profile, view_membership;
    LinearLayout ly_profile, ly_membership;
    ImageView img_membership, img_free, img_plus, img_pro;
    ImageView img_checked_free, img_checked_plus, img_checked_pro;
    TextView txt_upgrade_free, txt_upgrade_plus, txt_upgrade_pro;
    RadioGroup ediapay_method;
    String pay_method = "";
    Dialog dlg_ediaPay;
    String uniqueid = "";
    String transactionid = "";
    Button btn_ediapayDlg;
    TextView txt_ediapay_status;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings_patient, container, false);
        activity.setTitle("Settings");
        activity.setLogoutVisible(true);

        img_photo = v.findViewById(R.id.img_photo);
        edit_phone = v.findViewById(R.id.edit_phone);
        edit_firstname = v.findViewById(R.id.edit_firstname);
        edit_lastname = v.findViewById(R.id.edit_lastname);
        edit_address = v.findViewById(R.id.edit_address);
        img_membership = v.findViewById(R.id.img_membership);
        img_free = v.findViewById(R.id.img_free);
        img_plus = v.findViewById(R.id.img_plus);
        img_pro = v.findViewById(R.id.img_pro);
        img_checked_free = v.findViewById(R.id.img_checked_free);
        img_checked_plus = v.findViewById(R.id.img_checked_plus);
        img_checked_pro = v.findViewById(R.id.img_checked_pro);
        txt_upgrade_free = v.findViewById(R.id.txt_upgrade_free);
        txt_upgrade_plus = v.findViewById(R.id.txt_upgrade_plus);
        txt_upgrade_pro = v.findViewById(R.id.txt_upgrade_pro);

        v.findViewById(R.id.ly_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(activity, activity.MY_PERMISSION_CAMERA,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                if (!checkPermissionFirst) {
                    DesignToast.makeText(activity, activity.getResources().getString(R.string.enable_permissions), DesignToast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                } else {
                    displayChoiceDialog();
                }
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
                if (firstname.length()*lastname.length()*address.length() == 0) {
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
        ly_profile = v.findViewById(R.id.ly_profile);
        ly_membership = v.findViewById(R.id.ly_membership);
        btn_profile = v.findViewById(R.id.btn_profile);
        btn_membership = v.findViewById(R.id.btn_membership);
        view_profile = v.findViewById(R.id.view_profile);
        view_membership = v.findViewById(R.id.view_membership);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initButtons();
                btn_profile.setTextColor(activity.getResources().getColor(R.color.primary));
                view_profile.setVisibility(View.VISIBLE);
                ly_profile.setVisibility(View.VISIBLE);
            }
        });
        btn_membership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initButtons();
                btn_membership.setTextColor(activity.getResources().getColor(R.color.primary));
                view_membership.setVisibility(View.VISIBLE);
                ly_membership.setVisibility(View.VISIBLE);
            }
        });
        txt_upgrade_free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure to downgrade your membership?");
                builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("membership").setValue(0);
                        Date date = new Date();
                        String expiry = MyUtils.getDateString(MyUtils.getNextMonth(date));
                        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("expiry").setValue(expiry);
                        MyUtils.cur_user.membership = 0;
                        loadMembership();
                        loadProfile();
                        DesignToast.makeText(activity, "Your membership has been downgraded successfully.", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                    }
                });
                builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        txt_upgrade_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEdiaPayDialog((int)(0.16*598.30f), 1);
            }
        });
        txt_upgrade_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEdiaPayDialog((int)(0.16*598.30f), 2);
            }
        });
        loadProfile();
        loadMembership();
        return v;
    }
    void loadMembership() {
        img_checked_free.setVisibility(View.GONE);
        img_checked_plus.setVisibility(View.GONE);
        img_checked_pro.setVisibility(View.GONE);
        txt_upgrade_free.setVisibility(View.VISIBLE);
//        txt_upgrade_plus.setVisibility(View.GONE);
//        txt_upgrade_pro.setVisibility(View.GONE);
        txt_upgrade_free.setBackground(activity.getResources().getDrawable(R.drawable.round_frame_primary));
        txt_upgrade_plus.setBackground(activity.getResources().getDrawable(R.drawable.round_frame_primary));
        txt_upgrade_pro.setBackground(activity.getResources().getDrawable(R.drawable.round_frame_primary));
        txt_upgrade_free.setTextColor(activity.getColor(R.color.white));
        txt_upgrade_plus.setTextColor(activity.getColor(R.color.white));
        txt_upgrade_pro.setTextColor(activity.getColor(R.color.white));
        txt_upgrade_free.setPadding(20,10,20,10);
        txt_upgrade_plus.setPadding(20,10,20,10);
        txt_upgrade_pro.setPadding(20,10,20,10);
        txt_upgrade_plus.setTypeface(Typeface.DEFAULT);
        txt_upgrade_pro.setTypeface(Typeface.DEFAULT);

        switch (MyUtils.cur_user.membership) {
            case 0:
                img_membership.setImageDrawable(activity.getDrawable(R.drawable.ic_membership_free));
                img_membership.setColorFilter(activity.getResources().getColor(R.color.free));
                img_checked_free.setVisibility(View.VISIBLE);
                txt_upgrade_free.setVisibility(View.GONE);

                txt_upgrade_plus.setText("Upgrade");
                txt_upgrade_pro.setText("Upgrade");
                break;
            case 1:
                img_membership.setImageDrawable(activity.getDrawable(R.drawable.ic_membership_plus));
                img_membership.setColorFilter(activity.getResources().getColor(R.color.plus));
                img_checked_plus.setVisibility(View.VISIBLE);
                txt_upgrade_free.setText("Downgrade");
                txt_upgrade_pro.setText("Upgrade");
                txt_upgrade_plus.setText("Expired on " + MyUtils.cur_user.expiry);
                txt_upgrade_plus.setBackgroundColor(activity.getResources().getColor(R.color.transparent));
                txt_upgrade_plus.setTextColor(activity.getColor(R.color.red));
                txt_upgrade_plus.setTypeface(txt_upgrade_plus.getTypeface(), Typeface.ITALIC);
                break;
            case 2:
                img_membership.setImageDrawable(activity.getDrawable(R.drawable.ic_membership_pro));
                img_membership.setColorFilter(activity.getResources().getColor(R.color.pro));
                img_checked_pro.setVisibility(View.VISIBLE);
                txt_upgrade_free.setText("Downgrade");
                txt_upgrade_plus.setText("Downgrade");
                txt_upgrade_pro.setText("Expired on " + MyUtils.cur_user.expiry);
                txt_upgrade_pro.setBackgroundColor(activity.getResources().getColor(R.color.transparent));
                txt_upgrade_pro.setTextColor(activity.getColor(R.color.red));
                txt_upgrade_pro.setTypeface(txt_upgrade_plus.getTypeface(), Typeface.ITALIC);
                break;
        }
    }
    void initButtons() {
        btn_profile.setTextColor(activity.getResources().getColor(R.color.gray));
        btn_membership.setTextColor(activity.getResources().getColor(R.color.gray));
        view_profile.setVisibility(View.GONE);
        view_membership.setVisibility(View.GONE);
        ly_profile.setVisibility(View.GONE);
        ly_membership.setVisibility(View.GONE);
    }
    void updateUserInfo() {
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("firstname").setValue(firstname);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("lastname").setValue(lastname);
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("address").setValue(address);
        MyUtils.cur_user.firstname = firstname;
        MyUtils.cur_user.lastname = lastname;
        MyUtils.cur_user.address = address;
        loadProfile();
        DesignToast.makeText(activity, activity.getResources().getString(R.string.user_updated_successfully), DesignToast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
    }
    void loadProfile() {
        edit_phone.setText(MyUtils.cur_user.phone);
        edit_firstname.setText(MyUtils.cur_user.firstname);
        edit_lastname.setText(MyUtils.cur_user.lastname);
        edit_address.setText(MyUtils.cur_user.address);
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
    public void openEdiaPayDialog(int pay_balance, int membership) {
        final String[] gatewayid = {""};
        dlg_ediaPay = new Dialog(activity);
        Window window = dlg_ediaPay.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_ediapay, null);
        TextView txt_price = view.findViewById(R.id.txt_price);
        txt_price.setText(String.valueOf(pay_balance)+" XOF");
        txt_ediapay_status = view.findViewById(R.id.txt_status);
        ediapay_method = view.findViewById(R.id.radioGroup);
        ediapay_method.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                gatewayid[0] = view.findViewById(checkedId).getTag().toString();
                RadioButton radioButton = (RadioButton)view.findViewById(checkedId);
                pay_method = radioButton.getText().toString();
            }
        });
        btn_ediapayDlg = view.findViewById(R.id.btn_pay);
        if (transactionid.length() > 0) {
            btn_ediapayDlg.setText("Confirm");
            for(int i = 0; i < ediapay_method.getChildCount(); i++){
                ((RadioButton)ediapay_method.getChildAt(i)).setEnabled(false);
            }
            txt_ediapay_status.setVisibility(View.VISIBLE);
        } else {
            btn_ediapayDlg.setText("Pay");
            for(int i = 0; i < ediapay_method.getChildCount(); i++){
                ((RadioButton)ediapay_method.getChildAt(i)).setEnabled(true);
            }
            txt_ediapay_status.setVisibility(View.GONE);
        }
        btn_ediapayDlg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_ediapayDlg.getText().toString().equals("Pay")) {
                    if (gatewayid[0].length() == 0) {
                        MyUtils.showAlert(activity, activity.getResources().getString(R.string.warning), "Please choose a method");
                        return;
                    }
                    merchant_pay_ediaRequest(gatewayid[0], pay_balance);
                } else if (btn_ediapayDlg.getText().toString().equals("Confirm")) {
                    payment_status_ediaRequest(pay_balance, membership);
                }
            }
        });
        ImageButton btn_close = view.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg_ediaPay.dismiss();
            }
        });

        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels*0.70);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels*0.4);
        view.setMinimumWidth(width);
        view.setMinimumHeight(height);
        dlg_ediaPay.setCancelable(true);
        dlg_ediaPay.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg_ediaPay.setContentView(view);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dlg_ediaPay.show();
    }

    private void merchant_pay_ediaRequest(String gatewayid, int pay_balance) {
        activity.showProgress();
        uniqueid = MyUtils.getRandomStringUUID();
        final JSONObject object = new JSONObject();
        try {
            object.put("command", "create payment request");
            object.put("gatewayid", gatewayid);
            object.put("merchantid", App.ediaMerchantId);
            object.put("service", "UKAMEE membership payment");
            object.put("amount", pay_balance);
            object.put("currency", "XOF");
            object.put("uniqueid", uniqueid);
            object.put("sandbox", 0);
            object.put("mobile", MyUtils.cur_user.phone);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final RestClient restClient = RestClient.getInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = restClient.postRequest(App.ediapayUrl+"api_payment", object);
                JSONObject jsonObject = null;
                String _error = null;
                int pay_code = 0;
                try {
                    jsonObject = new JSONObject(response);
                    pay_code = jsonObject.getInt("code");

                    switch (pay_code) {
                        case 200:
                            _error = "Payment is under queue";
                            transactionid = jsonObject.getString("transactionid_out");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_ediapayDlg.setText("Confirm");
                                    for(int i = 0; i < ediapay_method.getChildCount(); i++){
                                        ((RadioButton)ediapay_method.getChildAt(i)).setEnabled(false);
                                    }
                                    txt_ediapay_status.setVisibility(View.VISIBLE);
                                }
                            });

                            break;

                        default:
                            _error = jsonObject.getString("error");
                            final String final_error = _error;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, final_error, Toast.LENGTH_LONG).show();
                                }
                            });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.dismissProgress();
            }
        }).start();
    }
    private void openCompleteDialog(final String pay_link) {
        final Dialog dlg = new Dialog(activity);
        Window window = dlg.getWindow();
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        final View view = getLayoutInflater().inflate(R.layout.dialog_ediapay_link, null);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(view);
        window.setGravity(Gravity.CENTER);
        Button btn_complete = (Button)view.findViewById(R.id.btn_complete);
        btn_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.openUrl(pay_link, activity);
                dlg.dismiss();
            }
        });
        dlg.show();
        dlg.getWindow().setLayout((int)(width*0.95f), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void payment_status_ediaRequest(int pay_balance, int membership) {
        activity.showProgress();
        final JSONObject object = new JSONObject();
        try {
            object.put("command", "get payment request status");
            object.put("merchantid", App.ediaMerchantId);
            object.put("uniqueid", uniqueid);
            object.put("transactionid", transactionid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final RestClient restClient = RestClient.getInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = restClient.postRequest(App.ediapayUrl+"api_payment", object);
                JSONObject jsonObject = null;
                String _error = null;
                int pay_code = 0;
                try {
                    jsonObject = new JSONObject(response);
                    pay_code = jsonObject.getInt("code");

                    switch (pay_code) {

                        case 100:
                            _error = "Payment has been completed successfully";
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DesignToast.makeText(activity, "Payment has been completed successfully", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                }
                            });

                            Transaction transaction = new Transaction("", MyUtils.cur_user.uid, String.valueOf(pay_balance), pay_method, transactionid, App.ediaMerchantId, uniqueid, "", String.valueOf(pay_code), MyUtils.getDateString(new Date()));
                            MyUtils.mDatabase.child(MyUtils.tbl_transaction).push().setValue(transaction);
                            MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("membership").setValue(membership);
                            Date date = new Date();
                            String expiry = MyUtils.getDateString(MyUtils.getNextMonth(date));
                            MyUtils.mDatabase.child(MyUtils.tbl_user).child(MyUtils.cur_user.uid).child("expiry").setValue(expiry);
                            MyUtils.cur_user.membership = membership;
                            dlg_ediaPay.dismiss();
                            loadMembership();
                            loadProfile();
                            break;
                        default:
                            _error = jsonObject.getString("error");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.dismissProgress();
            }
        }).start();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] _permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, _permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayChoiceDialog();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                PermissionUtils.checkPermissionFirst(activity, activity.MY_PERMISSION_CAMERA,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
            }
        }
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivityPatient) context;
    }
}