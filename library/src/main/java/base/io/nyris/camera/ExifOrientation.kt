package io.nyris.camera

import android.support.annotation.IntDef
import android.support.media.ExifInterface
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 *
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */

@IntDef(ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ExifOrientation