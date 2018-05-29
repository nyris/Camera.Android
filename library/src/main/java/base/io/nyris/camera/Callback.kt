package io.nyris.camera

/**
 *
 * Callback for monitoring events about [BaseCameraView].
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
interface Callback {
    /**
     * Called when camera is opened.
     *
     * @param cameraView The associated [BaseCameraView].
     */
    fun onCameraOpened(cameraView: BaseCameraView)

    /**
     * Called when camera is closed.
     *
     * @param cameraView The associated [BaseCameraView].
     */
    fun onCameraClosed(cameraView: BaseCameraView)

    /**
     * Called when a picture is taken.
     *
     * @param cameraView The associated [BaseCameraView].
     * @param resizedImage JPEG data.
     */
    fun onPictureTaken(cameraView: BaseCameraView, resizedImage: ByteArray)

    /**
     * Called when a picture is taken.
     *
     * @param cameraView The associated [BaseCameraView].
     * @param original JPEG data.
     */
    fun onPictureTakenOriginal(cameraView: BaseCameraView, original: ByteArray)

    /**
     * Called when a picture is taken.
     *
     * @param errorMessage    Error message.
     */
    fun onError(errorMessage: String)
}
