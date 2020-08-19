package com.interswitchng.smartpos.shared.utilities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import java.io.ByteArrayOutputStream


fun Context.toast(message: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, length).show()
}

fun View.hideMe() {
    if (visibility == View.VISIBLE && alpha == 1f) {
        animate()
            .alpha(0f)
            .withEndAction { visibility = View.GONE }
    }
}

fun View.showMe() {
    if (visibility == View.GONE && alpha == 0f) {
        animate()
            .alpha(1f)
            .withEndAction { visibility = View.VISIBLE }
    }
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE
fun View.isNotVisible(): Boolean = this.visibility != View.VISIBLE




fun getBitmapFromView(view: View): Bitmap? {
    //Define a bitmap with the same size as the view
    val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    //Bind a canvas to it
    val canvas = Canvas(returnedBitmap)
    //Get the view's background
    val bgDrawable = view.background
    if (bgDrawable != null) //has background drawable, then draw it on the canvas
        bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
        canvas.drawColor(Color.WHITE)
    // draw the view on the canvas
    view.draw(canvas)
    //return the bitmap
    return returnedBitmap
}

fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
    return Uri.parse(path)
}