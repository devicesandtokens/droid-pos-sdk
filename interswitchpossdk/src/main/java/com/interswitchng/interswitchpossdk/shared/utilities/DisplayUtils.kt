package com.interswitchng.interswitchpossdk.shared.utilities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.interswitchng.interswitchpossdk.R
import com.interswitchng.interswitchpossdk.modules.card.CardActivity
import com.interswitchng.interswitchpossdk.modules.paycode.PayCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.QrCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.UssdActivity
import com.interswitchng.interswitchpossdk.shared.Constants
import com.interswitchng.interswitchpossdk.shared.models.PaymentInfo
import com.interswitchng.interswitchpossdk.shared.views.TweakableOutlineProvider
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


internal object DisplayUtils {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getIsoString(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }


    /**
     * This method converts interger amount notation to string (e.g. 1000 -> 1,000.00)
     *
     * @param amount A value in integer representing the transaction amount
     * @return A string representation of the decimal notation for the amount
     */
    fun getAmountString(amount: Int): String {

        val numberFormat = NumberFormat.getInstance()
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2

        return numberFormat.format(amount)
    }
}