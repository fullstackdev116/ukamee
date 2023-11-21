package com.ediattah.yakko.idcamera.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ediattah.yakko.R;


/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/24
 * Desc	        ${裁剪布局}
 */
public class IDCropImageView extends FrameLayout {

    private ImageView       mImageView;
    private IDCropOverlayView mIDCropOverlayView;

    public IDCropImageView(@NonNull Context context) {
        super(context);
    }

    public IDCropImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.id_crop_image_view, this, true);
        mImageView = (ImageView) v.findViewById(R.id.img_crop);
        mIDCropOverlayView = (IDCropOverlayView) v.findViewById(R.id.overlay_crop);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        mIDCropOverlayView.setBitmap(bitmap);
    }

    public void crop(IDCropListener listener, boolean needStretch) {
        if (listener == null)
            return;
        mIDCropOverlayView.crop(listener, needStretch);
    }

}
