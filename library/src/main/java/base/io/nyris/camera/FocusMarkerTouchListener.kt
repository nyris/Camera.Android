package io.nyris.camera

import android.view.View

/**
 * FocusMarkerTouchListener.kt - Focus listener triggered when user tap one the CameraView
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
interface FocusMarkerTouchListener{
    fun onTouched(v: View)
}
