package io.nyris.camera

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import java.util.ArrayList

/**
 * CameraView.kt - Class view that extend BaseCameraView for targeting recognition based styleable parameters
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
class CameraView : BaseCameraView {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (isInEditMode) {
            mCallbacks = null
            return
        }

        mCallbacks = CallbackBridge()

        // Internal setup
        val preview = createPreviewImpl(context)
        mImpl = when {
            Build.VERSION.SDK_INT < 21 -> cameraBelow21(typeRecognition, preview)
            Build.VERSION.SDK_INT < 23 -> cameraAbove21Low23(typeRecognition, preview, context)
            else -> cameraAbove23(typeRecognition, preview, context)
        }

        // Attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr,
                R.style.Widget_CameraView)
        typeRecognition = a.getInt(R.styleable.CameraView_recognition, 0)

        mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false)
        facing = a.getInt(R.styleable.CameraView_facing, FACING_BACK)
        val aspectRatio = a.getString(R.styleable.CameraView_aspectRatio)
        if (aspectRatio != null) {
            setAspectRatio(AspectRatio.parse(aspectRatio))
        } else {
            setAspectRatio(Constants.DEFAULT_ASPECT_RATIO)
        }
        autoFocus = a.getBoolean(R.styleable.CameraView_autoFocus, true)
        flash = a.getInt(R.styleable.CameraView_flash, Constants.FLASH_AUTO)

        isSaveImage = a.getBoolean(R.styleable.CameraView_saveImage, false)
        takenPictureWidth = a.getInt(R.styleable.CameraView_imageWidth, 512)
        takenPictureHeight = a.getInt(R.styleable.CameraView_imageHeight, 512)
        a.recycle()

        updateFocusMarkerView(preview)
    }


    private fun cameraBelow21(type: Int, preview: PreviewImpl): CameraViewImpl {
        return when (type) {
            0 //none
            -> Camera1(mCallbacks, preview)
            1 //barcode
            -> Camera1ZBar(mCallbacks, preview)
            else -> Camera1(mCallbacks, preview)
        }
    }

    private fun cameraAbove21Low23(type: Int, preview: PreviewImpl, context: Context): CameraViewImpl {
        return when (type) {
            0 //none
            -> Camera2(mCallbacks, preview, context)
            1 //barcode
            -> Camera2ZBar(mCallbacks, preview, context)
            else -> Camera2(mCallbacks, preview, context)
        }
    }

    private fun cameraAbove23(type: Int, preview: PreviewImpl, context: Context): CameraViewImpl {
        return when (type) {
            0 //none
            -> Camera2Api23(mCallbacks, preview, context)
            1 //barcode
            -> Camera2ZBarApi23(mCallbacks, preview, context)
            else -> Camera2Api23(mCallbacks, preview, context)
        }
    }
}