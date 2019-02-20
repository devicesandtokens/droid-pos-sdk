package com.interswitchng.smartpos.mockservices

import android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.interswitchng.smartpos.shared.interfaces.library.Payable
import com.interswitchng.smartpos.shared.interfaces.network.TransactionRequeryCallback
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.TransactionStatus
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Bank
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.CodeResponse
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Transaction
import com.interswitchng.smartpos.shared.utilities.SimpleResponseHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class MockPayableService private constructor(
        private val qr: ((SimpleResponseHandler<CodeResponse?>) -> Unit)?,
        private val ussd: ((SimpleResponseHandler<CodeResponse?>) -> Unit)?,
        private val banks: ((SimpleResponseHandler<List<Bank>?>) -> Unit)?,
        private val paymentStatus: ((TransactionRequeryCallback) -> Unit)?) : Payable {



    private val defaultTime = 2500L
    private val code = "00"
    private val desc = "Successful"
    private val date = "2018-11-23T16:21:22"
    private val ref = "46524"
    private val bankShortCode = "*737*6*46524#"
    private val defaultShortCode = "*322*419*051691201*46524#"
    private val terminalId = "2069018M"
    private val transactionReference = "TRSuiKz3mpqoXmDsW7bqiiZsZ"
    private val qrCodeData = "000201021340771312156775204599953035665406100.005802NG5911Refund Test6005Lagos62290525ptu3UzobDrKJ9IE0kgcvxHITl6304432C"
    private val response = CodeResponse(code, desc, date, ref, bankShortCode, defaultShortCode, terminalId, transactionReference, qrCodeData)

    private val banksResponse = listOf(Bank(107, "ACCESS BANK PLC", "ABP"),
            Bank(108, "ECOBANK NIGERIA", "ECO"),
            Bank(103, "GUARANTY TRUST BANK", "GTB"),
            Bank(114, "Skye Bank", "SKYE"))

    override fun initiateUssdPayment(request: CodeRequest, callback: SimpleResponseHandler<CodeResponse?>) {

        Thread(Runnable {

            Thread.sleep(defaultTime)

            runOnUiThread {
                if (ussd != null) ussd.invoke(callback)
                else callback(response, null)
            }

        }).start()
    }

    override fun initiateQrPayment(request: CodeRequest, callback: SimpleResponseHandler<CodeResponse?>) {

        Thread(Runnable {

            Thread.sleep(defaultTime)

            runOnUiThread {
                if (qr != null) qr.invoke(callback)
                else callback(response, null)
            }

        }).start()
    }

    override fun getBanks(callback: SimpleResponseHandler<List<Bank>?>) {
        Thread(Runnable {
            Thread.sleep(defaultTime)

            runOnUiThread {
                if (banks != null) banks.invoke(callback)
                else callback(banksResponse, null)
            }
        }).start()
    }

    override fun checkPayment(type: PaymentType, status: TransactionStatus, timeout: Long, callback: TransactionRequeryCallback): ExecutorService {
        val executor = Executors.newSingleThreadScheduledExecutor()

        executor.execute {
            Thread.sleep(defaultTime)
            val response = Transaction(3380867, 5000, "XeAAoeCX8dklyjbBMDg5wysiA", "00", "566", false, "011", 50, null)

            runOnUiThread {
                if (paymentStatus != null) paymentStatus.invoke(callback)
                else callback.onTransactionCompleted(response)
            }
        }

        return executor
    }

    class Builder {
        private var qr: ((SimpleResponseHandler<CodeResponse?>) -> Unit)? = null
        private var ussd: ((SimpleResponseHandler<CodeResponse?>) -> Unit)? = null
        private var banks: ((SimpleResponseHandler<List<Bank>?>) -> Unit)? = null
        private var paymentStatus: ((TransactionRequeryCallback) -> Unit)? = null

        fun setQrCall(call: (SimpleResponseHandler<CodeResponse?>) -> Unit): Builder {
            qr = call
            return this
        }

        fun setUssdCall(call: (SimpleResponseHandler<CodeResponse?>) -> Unit): Builder {
            ussd = call
            return this
        }

        fun setBanksCall(call: (SimpleResponseHandler<List<Bank>?>) -> Unit): Builder {
            banks = call
            return this
        }

        fun setPaymentStatusCall(call: (TransactionRequeryCallback) -> Unit): Builder {
            paymentStatus = call
            return this
        }

        fun build() = MockPayableService(qr, ussd, banks, paymentStatus)

    }
}