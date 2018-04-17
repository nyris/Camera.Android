package io.nyris.camera

import android.hardware.Camera
import android.text.TextUtils
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import java.nio.charset.StandardCharsets

/**
 *
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */

@Suppress("DEPRECATION")
internal class Camera1ZBar(callback: CameraViewImpl.Callback?, preview: PreviewImpl) : IBarcodeView, Camera1(callback, preview), Camera.PreviewCallback {
    companion object {
        init {
            try {
                System.loadLibrary("iconv")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    private var scanner: ImageScanner = ImageScanner()
    private val barcodeListeners: MutableList<IBarcodeListener> = mutableListOf()
    private var isEnableBarcode : Boolean = true

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

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        Thread(Runnable {

            if(!isEnableBarcode)
                return@Runnable

            val parameters = camera.parameters
            val size = parameters.previewSize
            val width = size.width
            val height = size.height
            val barcode = Image(width, height, "Y800")
            barcode.data = data

            val result = scanner.scanImage(barcode)
            if(result == 0)
                return@Runnable

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

            if (!isCameraOpened)
                return@Runnable

            camera.setOneShotPreviewCallback(this)
        }).start()
    }

    override fun adjustCameraParameters(){
        super.adjustCameraParameters()
        if (isEnableBarcode) {
            mCamera.setOneShotPreviewCallback(this)
        }

        if (mShowingPreview) {
            mCamera.startPreview()
        }
    }
}