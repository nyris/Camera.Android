package io.nyris.camera

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import java.io.File
import java.io.FileOutputStream

/**
 * ImageSavingTask.kt - class for saving image in the background.
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
internal class SavingImageTask(context : Context, var mImage: ByteArray) : AsyncTask<Void, Void, Void>() {
    private var mSavedImageUri: Uri? = ImageUtils.getPhotoFileUri(context, "photo.jpg")

    override fun doInBackground(vararg voids: Void): Void? {
        try {
            val savedImageFile = File(mSavedImageUri?.path)
            savedImageFile.createNewFile()
            val fos = FileOutputStream(savedImageFile)
            fos.write(mImage)
            fos.close()
        } catch (ignored: Exception) {
        }
        return null
    }
}