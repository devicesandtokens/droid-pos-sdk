package com.interswitchng.interswitchpossdk.activities

import android.content.Intent
import com.interswitchng.interswitchpossdk.IswPos
import com.interswitchng.interswitchpossdk.modules.ussdqr.QrCodeActivity
import com.interswitchng.interswitchpossdk.shared.Constants
import com.interswitchng.interswitchpossdk.shared.interfaces.Payable
import com.interswitchng.interswitchpossdk.shared.models.POSConfiguration
import com.interswitchng.interswitchpossdk.shared.models.PaymentInfo
import com.interswitchng.interswitchpossdk.shared.models.response.CodeResponse
import com.interswitchng.interswitchpossdk.shared.utilities.SimpleResponseHandler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast


@RunWith(RobolectricTestRunner::class)
class QrCodeActivityTests {

    private lateinit var activity: QrCodeActivity
    private val parcel = PaymentInfo(2000, "stan")
    private val qrIntent = Intent().apply {
        putExtra(Constants.KEY_PAYMENT_INFO, parcel)
    }

    private lateinit var instance: IswPos

    @Before
    fun setup() {
        val nothing = ""
        val posConfiguration = POSConfiguration.default()
        instance = mock { on(mock.config).thenReturn(posConfiguration) }
    }

    @Test
    fun should_show_show_error_message_when_qr_code_request_fails() {
        // mock dependencies
        val failedResponse: CodeResponse = mock()
        whenever(failedResponse.responseCode).thenReturn(CodeResponse.SERVER_ERROR)

        val service: Payable = mock()
        whenever(service.initiateQrPayment(any(), any())).then{
            val callback: SimpleResponseHandler<CodeResponse?>  = it.getArgument(1)
            callback(failedResponse, null)
        }

        // load mock dependencies
        loadKoinModules(module {
            single { service }
            single { instance }
        })

        activity = Robolectric.buildActivity(QrCodeActivity::class.java, qrIntent).create().start().get()

        // check that the last toast was error
        val lastToastText = ShadowToast.getTextOfLatestToast()
        assertNotNull("Last Toast message was null", lastToastText)
        assertTrue(lastToastText.contains("error"))
    }

}