package io.nyris.camera

/**
 *
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
/**
 * Callback for monitoring events about [CameraView].
 */
interface Callback {
    /**
     * Called when camera is opened.
     *
     * @param cameraView The associated [CameraView].
     */
    fun onCameraOpened(cameraView: CameraView)

    /**
     * Called when camera is closed.
     *
     * @param cameraView The associated [CameraView].
     */
    fun onCameraClosed(cameraView: CameraView)

    /**
     * Called when a picture is taken.
     *
     * @param cameraView The associated [CameraView].
     * @param resizedImage JPEG data.
     */
    fun onPictureTaken(cameraView: CameraView, resizedImage: ByteArray)

    /**
     * Called when a picture is taken.
     *
     * @param cameraView The associated [CameraView].
     * @param original JPEG data.
     */
    fun onPictureTakenOriginal(cameraView: CameraView, original: ByteArray)

    /**
     * Called when a picture is taken.
     *
     * @param errorMessage    Error message.
     */
    fun onError(errorMessage: String)
}
