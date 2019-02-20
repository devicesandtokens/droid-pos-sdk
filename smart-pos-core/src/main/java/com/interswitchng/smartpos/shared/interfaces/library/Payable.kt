package com.interswitchng.smartpos.shared.interfaces.library

import com.interswitchng.smartpos.shared.interfaces.network.TransactionRequeryCallback
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.TransactionStatus
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Bank
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.CodeResponse
import com.interswitchng.smartpos.shared.utilities.SimpleResponseHandler
import java.util.concurrent.ExecutorService

internal interface Payable {

    fun initiateQrPayment(request: CodeRequest, callback: SimpleResponseHandler<CodeResponse?>)

    fun initiateUssdPayment(request: CodeRequest, callback: SimpleResponseHandler<CodeResponse?>)

    fun getBanks(callback: SimpleResponseHandler<List<Bank>?>)

    fun checkPayment(type: PaymentType, status: TransactionStatus, timeout: Long, callback: TransactionRequeryCallback): ExecutorService
    // other two
}