package com.group29.localtreasury

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object Util {
    // Checks if all the permissions are given
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23 || activity == null) return
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri, fromCamera: Boolean): Bitmap? {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        if(fromCamera){
            matrix.setRotate(90f)
            var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            return ret
        }else {
            val imagePath = getRealPathFromURI(context, imgUri)
            if (imagePath != null) {
                val exif = ExifInterface(imagePath)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }
            }
            val ret = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
            return ret
        }
    }

    // Used to get the path for gallery image to find the rotation
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor = context.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    //Save the bitmap to file
    fun saveBitmapToFile(bitmap: Bitmap?, file: File) {
        val out = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
    }

    //Returns the bitmap that was saved
    fun getBitmapFromFile(context: Context, file: File): Bitmap? {
        val inputStream = FileInputStream(file)
        return BitmapFactory.decodeStream(inputStream)
    }
}