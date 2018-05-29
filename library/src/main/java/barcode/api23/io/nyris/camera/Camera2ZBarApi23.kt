package io.nyris.camera

import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.params.StreamConfigurationMap

/**
 * Camera2ZBarApi23.kt - class that use Camera API 2 to detect barcode using ZBAR
 * Target API above level 23
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */

@TargetApi(23)
internal class Camera2ZBarApi23(callback : Callback?, preview : PreviewImpl, context : Context) : Camera2ZBar(callback, preview, context){
    override fun collectPictureSizes(sizes: SizeMap, map: StreamConfigurationMap) {
        // Try to get hi-res output sizes
        val outputSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG)
        if (outputSizes != null) {
            for (size in map.getHighResolutionOutputSizes(ImageFormat.JPEG)) {
                sizes.add(Size(size.width, size.height))
            }
        }
        if (sizes.isEmpty) {
            super.collectPictureSizes(sizes, map)
        }
    }
}