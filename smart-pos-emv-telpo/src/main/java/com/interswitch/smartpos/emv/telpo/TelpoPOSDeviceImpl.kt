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
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import com.telpo.tps550.api.util.StringUtil

class TelpoPOSDeviceImpl constructor(
        override val printer: DevicePrinter,
        private val factory: () -> EmvCardReader,
        private val context: Context
): POSDevice {

    override fun getEmvCardReader(): EmvCardReader = factory()

    override val name: String
        get() = DEVICE_NAME

    override val hasFingerPrintReader: Boolean get() = true

    override fun loadInitialKey(initialKey: String, ksn: String) {
        EmvService.Open(this.context)
        EmvService.Device_Open(this.context)
        val keyValue = StringUtil.toBytes(IPEK_STRING)
        val ksnValue = StringUtil.toBytes(KSN_STRING)
//        PinpadService.TP_PinpadWriteDukptIPEK(keyValue)
        PinpadService.Open(this.context)
        PinpadService.TP_PinpadWriteDukptIPEK(keyValue, ksnValue, 0, 0, 0)
    }

    override fun loadMasterKey(masterKey: String) {

        EmvService.Open(this.context)
        EmvService.deviceOpen()
        PinpadService.Open(this.context)

        val key = StringUtil.toBytes(masterKey)
        val result = PinpadService.TP_WriteMasterKey(INDEX_TMK, key, PinpadService.KEY_WRITE_DIRECT)
        print(result)


        // PinpadService.Close()
        // EmvService.Device_Close()

    }

    override fun loadPinKey(pinKey: String) {

        /*  EmvService.Open(this.context)
          EmvService.deviceOpen()
          PinpadService.Open(this.context)*/

        val key = StringUtil.toBytes(pinKey)
        val result = PinpadService.TP_WritePinKey(INDEX_TPK, key, PinpadService.KEY_WRITE_DIRECT, INDEX_TMK)
        print(result)

        PinpadService.Close()
        EmvService.Device_Close()

    }

    fun setCompanyLogo(bitmap: Bitmap) {
        companyLogo = bitmap


    }

    companion object {

        internal const val INDEX_TIK = 0x01
        internal const val INDEX_TMK = 0x01
        internal const val INDEX_TPK = 0x03
        internal const val DEVICE_NAME: String = "TELPO"
        internal const val KSN_STRING: String = "FFFF000002DDDDE00000"
        internal const val IPEK_STRING: String = "00000000000000003F2216D8297BCE9C"

        internal lateinit var companyLogo: Bitmap private set

        @JvmStatic
        internal fun create(context: Context, printer: DevicePrinter, factory: () -> EmvCardReader): TelpoPOSDeviceImpl {
            // setupDevice pos device
            setupDevice()

            // setup logo
            val drawable = ContextCompat.getDrawable(context, R.drawable.isw_telpo_app_logo)!!
            companyLogo = run {
                return@run when(drawable) {
                    is BitmapDrawable -> drawable.bitmap
                    else -> Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                    }
                }
            }

            return TelpoPOSDeviceImpl(printer, factory, context)
        }


        @JvmStatic
        fun create(context: Context): TelpoPOSDeviceImpl {
            val printer: DevicePrinter = TelpoDevicePrinterImpl(context)
            val factory = { TelpoEmvCardReaderImpl(context) }
            return create(context, printer, factory)
        }

        private var isSetup = false

        private fun setupDevice() {
            isSetup = true
        }
    }
}