package com.interswitchng.smartpos.modules.ussdqr.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.interswitchng.smartpos.shared.interfaces.library.HttpService
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.CodeResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class QrViewModel(paymentService: HttpService): BaseViewModel(paymentService) {


    private val _qrCode = MutableLiveData<Optional<CodeResponse>>()
    val qrCode: LiveData<Optional<CodeResponse>> get() = _qrCode

    fun getQrCode(request: CodeRequest, context: Context) {
        // retrieve the qr code
        uiScope.launch {
            // get qrrequest code in IO thread
            val response = withContext(ioScope) {
                // get the code
                val code = paymentService.initiateQrPayment()

                // if code was returned, generate the image in IO thread
                if (code is Some) code.value.setBitmap(context)

                // return value
                code
            }

            // set result in main
            _qrCode.value = response
        }
    }
}