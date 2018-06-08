package io.nyris.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * ImageUtils.kt - class that containing image processing helpers.
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
open class ImageUtils {
    companion object {
        init {
            try {
                System.loadLibrary("ImageUtils")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }

        fun rotateBitmap(image: ByteArray): ByteArray {
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            val matrix = Matrix()
            matrix.setRotate(Exif.getOrientation(image))
            val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            return stream.toByteArray()
        }

        fun resize(context: Context, data: ByteArray, width: Int, height: Int): ByteArray {
            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            // Create and rotate the bitmap by rotationDegrees
            val matrix = Matrix()
            try {
                val orientation = Exif.getOrientation(data)
                matrix.postRotate(orientation)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // calculate the new image size preserving the aspect ratio
            val proportionalScaleSize = calculateImageSizePreservingAspectRatio(context, Size(width, height), bitmap.width, bitmap.height)
            val scaledImage = Bitmap.createScaledBitmap(bitmap, proportionalScaleSize.width, proportionalScaleSize.height, true)

            return compressAndTransformToBytes(scaledImage)
        }

        fun resize(context: Context, bitmap: Bitmap, width: Int, height: Int): Bitmap {
            val originalHeight = bitmap.height
            val originalWidth = bitmap.width
            val proportionalScaleSize = calculateImageSizePreservingAspectRatio(context, Size(width, height), originalWidth, originalHeight)
            return Bitmap.createScaledBitmap(bitmap, proportionalScaleSize.width, proportionalScaleSize.height, true)
        }

        fun compressAndTransformToBytes(scaledDownImage: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            scaledDownImage.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            return stream.toByteArray()
        }

        fun getPhotoFileUri(context: Context, fileName: String): Uri? {
            if (isExternalStorageAvailable()) {
                val mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                return Uri.fromFile(File(mediaStorageDir!!.path + File.separator + fileName))
            }
            return null
        }

        /*** Private **/
        private fun calculateImageSizePreservingAspectRatio(context: Context, size: Size, originalWidth: Int, originalHeight: Int): Size {
            val percentWidth = size.width / originalWidth.toFloat()
            val percentHeight = size.height / originalHeight.toFloat()

            val percent = if (percentHeight < percentWidth) percentHeight else percentWidth
            val newWidth = (originalWidth * percent).toInt()
            val newHeight = (originalHeight * percent).toInt()
            saveParam(context, "current_ratio", percent.toString() + "")
            return Size(newWidth, newHeight)
        }

        private fun saveParam(context: Context, key: String, value: String) {
            val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = mPrefs.edit()
            editor.putString(key, value)
            editor.apply()
        }

        private fun getParam(context: Context, key: String): String {
            val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return mPrefs.getString(key, "")
        }

        private fun isExternalStorageAvailable(): Boolean {
            val state = Environment.getExternalStorageState()
            return state == Environment.MEDIA_MOUNTED
        }

        fun rotateBitmap(bitmap: Bitmap, rotation: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(rotation)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
            return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        }

        fun rotateImageFromUri(context: Context, imageUri: Uri): Bitmap? {
            val exif = ExifInterface(context.contentResolver.openInputStream(imageUri))
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            var captureBmp: Bitmap? = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            captureBmp = rotateBitmap(captureBmp, orientation)
            return captureBmp
        }

        fun rotateBitmap(bitmap: Bitmap?, @ExifOrientation orientation: Int): Bitmap? {
            if (bitmap == null)
                return null
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> return bitmap
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate(180f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
                else -> return bitmap
            }

            return try {
                val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                bmRotated
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Converts YUV420 semi-planar data to ARGB 8888 data using the supplied width
         * and height. The input and output must already be allocated and non-null.
         * For efficiency, no error checking is performed.
         *
         * @param y
         * @param u
         * @param v
         * @param uvPixelStride
         * @param width The width of the input image.
         * @param height The height of the input image.
         * @param halfSize If true, downsample to 50% in each dimension, otherwise not.
         * @param output A pre-allocated array for the ARGB 8:8:8:8 output data.
         */
        @JvmStatic
        external fun stringFromJNI(): String

        @JvmStatic
        external fun convertYUV420ToARGB8888(
                y: ByteArray?,
                u: ByteArray?,
                v: ByteArray?,
                output: IntArray?,
                width: Int,
                height: Int,
                yRowStride: Int,
                uvRowStride: Int,
                uvPixelStride: Int,
                halfSize: Boolean)

        /**
         * Returns a transformation matrix from one reference frame into another.
         * Handles cropping (if maintaining aspect ratio is desired) and rotation.
         *
         * @param srcWidth Width of source frame.
         * @param srcHeight Height of source frame.
         * @param dstWidth Width of destination frame.
         * @param dstHeight Height of destination frame.
         * @param applyRotation Amount of rotation to apply from one frame to another.
         * Must be a multiple of 90.
         * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
         * cropping the image if necessary.
         * @return The transformation fulfilling the desired requirements.
         */
        @JvmStatic
        fun getTransformationMatrix(
                srcWidth: Int,
                srcHeight: Int,
                dstWidth: Int,
                dstHeight: Int,
                applyRotation: Int,
                maintainAspectRatio: Boolean): Matrix {
            val matrix = Matrix()

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())

            // Account for the already applied rotation, if any, and then determine how
            // much scaling is needed for each axis.
            val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

            val inWidth = if (transpose) srcHeight else srcWidth
            val inHeight = if (transpose) srcWidth else srcHeight

            // Apply scaling if necessary.
            if (inWidth != dstWidth || inHeight != dstHeight) {
                val scaleFactorX = dstWidth / inWidth.toFloat()
                val scaleFactorY = dstHeight / inHeight.toFloat()

                if (maintainAspectRatio) {
                    // Scale by minimum factor so that dst is filled completely while
                    // maintaining the aspect ratio. Some image may fall off the edge.
                    val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                    matrix.postScale(scaleFactor, scaleFactor)
                } else {
                    // Scale exactly to fill dst from src.
                    matrix.postScale(scaleFactorX, scaleFactorY)
                }
            }


            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)

            return matrix
        }
    }
}