/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nyris.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Set;

public class BaseCameraView extends FrameLayout {

    /** The camera device faces the opposite direction as the device's screen. */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /** The camera device faces the same direction as the device's screen. */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /** type of the recognition */
    protected int typeRecognition = 0;

    /** Direction the camera faces relative to device screen. */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /** Flash will not be fired. */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /** Flash will always be fired during snapshot. */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /** Constant emission of light during preview, auto-focus and snapshot. */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /** Flash will be fired automatically when required. */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /** Flash will be fired in red-eye reduction mode. */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /** The mode for for the camera device's flash control */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }

    CameraViewImpl mImpl;

    protected boolean mAdjustViewBounds;

    private final DisplayOrientationDetector mDisplayOrientationDetector;

    protected boolean isScreenShot;

    private boolean isTakeScreenshot;

    private boolean isSaveImage;

    private int takenPictureWidth = 512;

    private int takenPictureHeight = 512;

    protected CallbackBridge mCallbacks;

    protected BaseCameraView(Context context) {
        this(context, null);
    }

    protected BaseCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("WrongConstant")
    protected BaseCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()){
            mDisplayOrientationDetector = null;
            return;
        }
        // Display orientation detector
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @SuppressLint("ClickableViewAccessibility")
    void updateFocusMarkerView(final PreviewImpl preview){
        View view = findViewById(R.id.focusMarker);
        if(view != null)
            removeView(view);
        final FocusMarkerLayout focusMarkerLayout = new FocusMarkerLayout(getContext());
        focusMarkerLayout.setId(R.id.focusMarker);
        addView(focusMarkerLayout);
        focusMarkerLayout.setOnTouchListener((v, motionEvent) -> {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if(focusMarkerTouchListener!= null){
                    focusMarkerTouchListener.onTouched(focusMarkerLayout);
                }
                focusMarkerLayout.focus(motionEvent.getX(), motionEvent.getY());
            }

            //preview.getView().dispatchTouchEvent(motionEvent);
            return true;
        });
    }

    FocusMarkerTouchListener focusMarkerTouchListener;
    public void setFocusMarkerTouchListener(FocusMarkerTouchListener focusMarkerTouchListener) {
        this.focusMarkerTouchListener = focusMarkerTouchListener;
    }

    @NonNull
    private PreviewImpl createPreviewImpl(Context context, boolean isFallback) {
        if(isFallback)
            return new SurfaceViewPreview(context, this);
        else return createPreviewImpl(context);
    }

    @NonNull
    protected PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview;
        if (Build.VERSION.SDK_INT < 14) {
            preview = new SurfaceViewPreview(context, this);
        } else {
            preview = new TextureViewPreview(context, this);
        }
        return preview;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Handle android:adjustViewBounds
        if (mAdjustViewBounds) {
            if (!isCameraOpened()) {
                mCallbacks.reserveRequestLayoutOnOpen();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        // Measure the TextureView
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        AspectRatio ratio = getAspectRatio();
        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            ratio = ratio.inverse();
        }
        assert ratio != null;

        if (height < width * ratio.getY() / ratio.getX()) {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(),
                            MeasureSpec.EXACTLY));
        } else {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    public Size getSurfaceMeasurement(){
        return new Size(mImpl.getView().getMeasuredWidth(), mImpl.getView().getMeasuredHeight());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.facing = getFacing();
        state.ratio = getAspectRatio();
        state.autoFocus = getAutoFocus();
        state.flash = getFlash();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setFacing(ss.facing);
        setAspectRatio(ss.ratio);
        setAutoFocus(ss.autoFocus);
        setFlash(ss.flash);
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * {@link Activity#onResume()}.
     */
    public void start() {
        isTakeScreenshot = false;
        try {
            if (!mImpl.start()) {
                Parcelable state=onSaveInstanceState();
                // Camera2 uses legacy hardware layer; fall back to Camera1
                PreviewImpl preview = createPreviewImpl(getContext(), true);
                mImpl = new Camera1(mCallbacks, preview);
                onRestoreInstanceState(state);
                mImpl.start();
                updateFocusMarkerView(preview);
            }
        }
        catch (Exception e){
            mCallbacks.onError(e.getMessage());
        }
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * {@link Activity#onPause()}.
     */
    public void stop() {
        mImpl.stop();
    }

    /**
     * @return {@code true} if the camera is opened.
     */
    public boolean isCameraOpened() {
        return mImpl.isCameraOpened();
    }

    /**
     * Add a new callback.
     *
     * @param callback The {@link Callback} to add.
     * @see #removeCallback(Callback)
     */
    public void addCallback(@NonNull Callback callback) {
        mCallbacks.add(callback);
    }

    /**
     * Remove a callback.
     *
     * @param callback The {@link Callback} to remove.
     * @see #addCallback(Callback)
     */
    public void removeCallback(@NonNull Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param adjustViewBounds {@code true} if you want the CameraView to adjust its bounds to
     *                         preserve the aspect ratio of camera.
     * @see #getAdjustViewBounds()
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mAdjustViewBounds != adjustViewBounds) {
            mAdjustViewBounds = adjustViewBounds;
            requestLayout();
        }
    }

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see #setAdjustViewBounds(boolean)
     */
    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either {@link #FACING_BACK} or
     *               {@link #FACING_FRONT}.
     */
    public void setFacing(@Facing int facing) {
        mImpl.setFacing(facing);
    }


    /**
     * Set save image to true to save image
     * @param saveImage the boolean value
     */
    public void setSaveImage(boolean saveImage) {
        isSaveImage = saveImage;
    }

    /**
     * Set taken picture width
     * @param takenPictureWidth the width value
     */
    public void setTakenPictureWidth(int takenPictureWidth) {
        this.takenPictureWidth = takenPictureWidth;
    }

    /**
     * Set taken picture height
     * @param takenPictureHeight the height value
     */
    public void setTakenPictureHeight(int takenPictureHeight) {
        this.takenPictureHeight = takenPictureHeight;
    }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    @Facing
    public int getFacing() {
        //noinspection WrongConstant
        return mImpl.getFacing();
    }

    /**
     * Get is saving image
     * @return The saving image value
     */
    public boolean isSaveImage() {
        return isSaveImage;
    }

    /**
     * Get taken picture width
     * @return The width of the taken picture
     */
    public int getTakenPictureWidth() {
        return takenPictureWidth;
    }

    /**
     * Get taken picture height
     * @return The height of the taken picture
     */
    public int getTakenPictureHeight() {
        return takenPictureHeight;
    }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    public Set<AspectRatio> getSupportedAspectRatios() {
        return mImpl.getSupportedAspectRatios();
    }

    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The {@link AspectRatio} to be set.
     */
    public void setAspectRatio(@NonNull AspectRatio ratio) {
        if (mImpl.setAspectRatio(ratio)) {
            requestLayout();
        }
    }

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current {@link AspectRatio}. Can be {@code null} if no camera is opened yet.
     */
    @Nullable
    public AspectRatio getAspectRatio() {
        return mImpl.getAspectRatio();
    }

    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus {@code true} to enable continuous auto-focus mode. {@code false} to
     *                  disable it.
     */
    public void setAutoFocus(boolean autoFocus) {
        mImpl.setAutoFocus(autoFocus);
    }

    /**
     * Enable Barcode recognition
     */
    public void enableBarcode(boolean isEnabled) {
        if(!(mImpl instanceof IBarcodeCamera)){
            final PreviewImpl preview = createPreviewImpl(getContext(), isEnabled);
            CameraViewImpl cameraViewImpl = new Camera1ZBar(mCallbacks, preview);
            cameraViewImpl.setFlash(mImpl.getFlash());
            cameraViewImpl.setAutoFocus(mImpl.getAutoFocus());
            cameraViewImpl.setAspectRatio(mImpl.getAspectRatio());
            cameraViewImpl.setFacing(mImpl.getFacing());
            mImpl = cameraViewImpl;
        }
        mImpl.enableBarcode(isEnabled);
    }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return {@code true} if the continuous auto-focus mode is enabled. {@code false} if it is
     * disabled, or if it is not supported by the current camera.
     */
    public boolean getAutoFocus() {
        return mImpl.getAutoFocus();
    }

    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    public void setFlash(@Flash int flash) {
        mImpl.setFlash(flash);
    }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    @Flash
    public int getFlash() {
        //noinspection WrongConstant
        return mImpl.getFlash();
    }

    /**
     * Add barcode listener
     * @param barcodeListener the barcode listener
     */
    public void addBarcodeListener(IBarcodeListener barcodeListener){
        mImpl.addBarcodeListener(barcodeListener);
    }

    /**
     * Take a picture. The result will be returned to
     * {@link Callback#onPictureTaken(BaseCameraView, byte[])}.
     */
    public void takePicture() {
        isScreenShot = false;
        Bitmap bitmap = mImpl.getPreviewBitmap(getWidth(), getHeight());
        if(isTakeScreenshot)
            mImpl.takePicture();

        if(bitmap ==null)
            mImpl.takePicture();
        else {
            Bitmap emptyBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            if (bitmap.sameAs(emptyBitmap)) {
                mImpl.takePicture();
                isTakeScreenshot = true;
            }
            else {
                isScreenShot = true;
                mCallbacks.onPictureTaken(bitmap);
            }
        }
    }

    protected class CallbackBridge implements CameraViewImpl.Callback {
        private final ArrayList<Callback> mCallbacks = new ArrayList<>();
        private boolean mRequestLayoutOnOpen;
        CallbackBridge() {

        }

        public void add(Callback callback) {
            mCallbacks.add(callback);
        }

        public void remove(Callback callback) {
            mCallbacks.remove(callback);
        }

        @Override
        public void onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false;
                requestLayout();
            }
            for (Callback callback : mCallbacks) {
                callback.onCameraOpened(BaseCameraView.this);
            }
        }
        @Override
        public void onCameraClosed() {
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(BaseCameraView.this);
            }
        }

        @Override
        public void onPictureTaken(byte[] image) {
            if (!isScreenShot) {
                image = ImageUtils.Companion.rotateBitmap(image);
                image = ImageUtils.Companion.resize(getContext(), image, getWidth(), getHeight());
            }

            if (isSaveImage) {
                new ImageSavingTask(getContext(), image).execute();
            }

            byte[] transformedData = ImageUtils.Companion.resize(getContext(), image, takenPictureWidth, takenPictureHeight);
            for (Callback callback : mCallbacks) {
                callback.onPictureTakenOriginal(BaseCameraView.this, image);
                callback.onPictureTaken(BaseCameraView.this, transformedData);
            }
        }

        @Override
        public void onPictureTaken(Bitmap bitmap) {
            byte[] data = ImageUtils.Companion.compressAndTransformToBytes(bitmap);
            onPictureTaken(data);
        }

        @Override
        public void onError(String errorMessage) {
            for (Callback callback : mCallbacks) {
                callback.onError(errorMessage);
            }
        }
        public void reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true;
        }
        public ArrayList<Callback> getCallbacks() { return mCallbacks;}
    }

    protected static class SavedState extends BaseSavedState {
        @Facing
        int facing;

        AspectRatio ratio;

        boolean autoFocus;

        @Flash
        int flash;

        @SuppressWarnings("WrongConstant")
        SavedState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            ratio = source.readParcelable(loader);
            autoFocus = source.readByte() != 0;
            flash = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeParcelable(ratio, 0);
            out.writeByte((byte) (autoFocus ? 1 : 0));
            out.writeInt(flash);
        }

        static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        });
    }
}
