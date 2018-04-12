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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.util.Set;

abstract class CameraViewImpl {
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_HEIGHT = 1080;

    static final int FOCUS_AREA_SIZE_DEFAULT = 300;
    static final int FOCUS_METERING_AREA_WEIGHT_DEFAULT = 1000;
    static final int DELAY_MILLIS_BEFORE_RESETTING_FOCUS = 3000;

    final Callback mCallback;

    final PreviewImpl mPreview;

    CameraViewImpl(Callback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    int getFocusAreaSize() {
        return FOCUS_AREA_SIZE_DEFAULT;
    }

    int getFocusMeteringAreaWeight() {
        return FOCUS_METERING_AREA_WEIGHT_DEFAULT;
    }

    void detachFocusTapListener() {
        mPreview.getView().setOnTouchListener(null);
    }

    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    abstract boolean start();

    abstract void stop();

    abstract void stopPreview();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract Set<AspectRatio> getSupportedAspectRatios();

    abstract boolean setAspectRatio(AspectRatio ratio);

    abstract AspectRatio getAspectRatio();

    abstract void setAutoFocus(boolean autoFocus);

    abstract boolean getAutoFocus();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void takePicture();

    abstract void setDisplayOrientation(int displayOrientation);

    void setBarcodesDetectorListner(IBarcodeListener barcodeListener){

    }

    void setBarcode(boolean isEnabled){

    }

    boolean isBarcode(){
        return false;
    }

    Bitmap getPreviewBitmap(int cameraWidth, int cameraHeight) {
        if(mPreview == null)
            return null;
        View preview = mPreview.getView();
        Bitmap screenShotPreviewBmp = null;
        try {
            if(preview instanceof TextureView){
                screenShotPreviewBmp = Bitmap.createBitmap(preview.getWidth(),preview.getHeight(),Bitmap.Config.ARGB_8888);
                TextureView textureView = (TextureView) preview;
                screenShotPreviewBmp = textureView.getBitmap(screenShotPreviewBmp);
            }
            else {
                SurfaceView surfaceView = (SurfaceView) preview;
                screenShotPreviewBmp = Bitmap.createBitmap(preview.getWidth(), preview.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(screenShotPreviewBmp);
                surfaceView.draw(c);
            }
            screenShotPreviewBmp=Bitmap.createBitmap(screenShotPreviewBmp, 0,0,cameraWidth, cameraHeight);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return screenShotPreviewBmp;
    }

    interface Callback {

        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(byte[] data);

        void onPictureTaken(Bitmap bitmap);

        void onError(String errorMessage);
    }
}
