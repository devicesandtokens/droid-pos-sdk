package com.interswitch.smartpos.emv.telpo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.interswitch.smartpos.emv.telpo.emv.TelpoEmvCardReaderImpl
import com.interswitchng.smartpos.shared.interfaces.device.DevicePrinter
import com.interswitchng.smartpos.shared.interfaces.device.EmvCardReader
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice
import com.telpo.tps550.api.reader.SmartCardReader

class TelpoPOSDeviceImpl constructor(
    override val printer: DevicePrinter,
    private val factory: () -> EmvCardReader
): POSDevice {

    override fun getEmvCardReader(): EmvCardReader = factory()

    companion object {

        internal lateinit var companyLogo: Bitmap private set

        @JvmStatic
        internal fun create(context: Context, printer: DevicePrinter, factory: () -> EmvCardReader): TelpoPOSDeviceImpl {
            // setupDevice pos device
            setupDevice(context)

            // setup logo
//            val drawable = ContextCompat.getDrawable(context, R.drawable.isw_pax_app_logo)!!
//            companyLogo = run {
//                return@run when(drawable) {
//                    is BitmapDrawable -> drawable.bitmap
//                    else -> Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
//                        val canvas = Canvas(bitmap)
//                        drawable.setBounds(0, 0, canvas.width, canvas.height)
//                        drawable.draw(canvas)
//                    }
//                }
//            }

            return TelpoPOSDeviceImpl(printer, factory)
        }


        @JvmStatic
        fun create(context: Context): TelpoPOSDeviceImpl {
            val printer: DevicePrinter = TelpoDevicePrinterImpl(context)
            val factory = { TelpoEmvCardReaderImpl(context) }
            return create(context, printer, factory)
        }

//        @JvmStatic
//        lateinit var dal: IDAL
//            private set

        private var isSetup = false

        private fun setupDevice(context: Context) {
//            if (!isSetup) {
//                val neptunes = NeptuneLiteUser.getInstance()
//                dal = neptunes.getDal(context)
//                // set IDevice implementation
//                DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance())
//                isSetup = true
//            }
        }
    }
}