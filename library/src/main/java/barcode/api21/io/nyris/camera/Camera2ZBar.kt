package io.nyris.camera

import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.ImageScanner
import java.nio.charset.StandardCharsets
import java.util.*


/**
 *
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
@TargetApi(21)
internal open class Camera2ZBar(callback : Callback, preview : PreviewImpl, context : Context) : Camera2(callback, preview, context){
    private var scanner: ImageScanner = ImageScanner()
    private val barcodeListeners: MutableList<IBarcodeListener> = mutableListOf()
    private var isEnableBarcode : Boolean = true

    private var listenerThread: HandlerThread? = HandlerThread("listenerThread")
    private var listenerHandler: Handler? = null

    private lateinit var frameSize: Size

    init {
        scanner.setConfig(0, Config.X_DENSITY, 3)
        scanner.setConfig(0, Config.Y_DENSITY, 3)
    }

    override fun addBarcodeListener(barcodeListener: IBarcodeListener) {
        barcodeListeners.add(barcodeListener)
    }

    override fun enableBarcode(isEnabled: Boolean) {
        isEnableBarcode = isEnabled
    }

    private val mBarcodeImageListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener

        val planes = image.planes
        if (!planes.isNotEmpty()) {
            image.close()
            return@OnImageAvailableListener
        }

        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val barcode = net.sourceforge.zbar.Image(frameSize.width, frameSize.height, "Y800")
        barcode.data = bytes
        val result = scanner.scanImage(barcode)
        if (result == 0) {
            image.close()
            return@OnImageAvailableListener
        }

        val syms = scanner.results
        val barcodeInstance = Barcode()
        for (sym in syms) {
            var symData: String
            symData = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                String(sym.dataBytes, StandardCharsets.UTF_8)
            } else {
                sym.data
            }
            if (!TextUtils.isEmpty(symData)) {
                symData = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    String(sym.dataBytes, StandardCharsets.UTF_8)
                } else {
                    sym.data
                }
                if (!TextUtils.isEmpty(symData)) {
                    barcodeInstance.contents = symData
                    barcodeInstance.format = BarcodeFormat.getFormatById(sym.type)
                }
            }
        }

        for (barcodeListener in barcodeListeners) {
            barcodeListener.onBarcode(barcodeInstance)
        }

        image.close()
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                partialResult: CaptureResult) {
        }

        override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult) {
        }
    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            if (mCamera == null) {
                return
            }
            mCaptureSession = session
            updateAutoFocus()
            updateFlash()
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                        mCaptureCallback, listenerHandler)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(this.toString(), "Failed to configure capture session.")
        }

        override fun onClosed(session: CameraCaptureSession) {
            if (mCaptureSession != null && mCaptureSession == session) {
                mCaptureSession = null
            }
        }
    }

    override fun start(): Boolean {
        listenerThread?.start()
        listenerHandler = Handler(listenerThread?.looper)
        return super.start()
    }

    override fun stop() {
        super.stop()
        stopBarcodeThread()
    }

    private fun stopBarcodeThread() {
        listenerThread?.quitSafely()
        try {
            listenerThread?.join()
            listenerThread = null
            listenerHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun prepareImageReader() {
    }

    override fun startCaptureSession() {
        if (!isCameraOpened || !mPreview.isReady) {
            return
        }
        frameSize = chooseOptimalSize()
        mPreview.setBufferSize(frameSize.width, frameSize.height)
        val surface = mPreview.surface
        try {
            mPreviewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder.addTarget(surface)
            mImageReader = ImageReader.newInstance(frameSize.width, frameSize.height,
                    ImageFormat.YUV_420_888, /* maxImages */ 3)
            mImageReader.setOnImageAvailableListener(mBarcodeImageListener, listenerHandler)
            mPreviewRequestBuilder.addTarget(mImageReader.surface)
            mCamera.createCaptureSession(Arrays.asList(surface, mImageReader.surface),
                    mSessionCallback, null)
        } catch (e: CameraAccessException) {
            mCallback.onError("Failed to start camera session")
            return
        }
    }
}