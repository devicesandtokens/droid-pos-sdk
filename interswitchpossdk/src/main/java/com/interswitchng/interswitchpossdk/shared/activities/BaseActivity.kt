package com.interswitchng.interswitchpossdk.shared.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.interswitchng.interswitchpossdk.IswPos
import com.interswitchng.interswitchpossdk.R
import com.interswitchng.interswitchpossdk.modules.card.CardActivity
import com.interswitchng.interswitchpossdk.modules.paycode.PayCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.QrCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.UssdActivity
import com.interswitchng.interswitchpossdk.shared.Constants
import com.interswitchng.interswitchpossdk.shared.interfaces.device.POSDevice
import com.interswitchng.interswitchpossdk.shared.interfaces.library.Payable
import com.interswitchng.interswitchpossdk.shared.interfaces.TransactionRequeryCallback
import com.interswitchng.interswitchpossdk.shared.models.PaymentInfo
import com.interswitchng.interswitchpossdk.shared.models.transaction.PaymentType
import com.interswitchng.interswitchpossdk.shared.models.transaction.TransactionResult
import com.interswitchng.interswitchpossdk.shared.models.transaction.ussdqr.request.TransactionStatus
import com.interswitchng.interswitchpossdk.shared.models.transaction.ussdqr.response.Transaction
import com.interswitchng.interswitchpossdk.shared.views.BottomSheetOptionsDialog
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.isw_content_toolbar.*
import org.koin.android.ext.android.inject
import java.util.concurrent.ExecutorService

abstract class BaseActivity : AppCompatActivity() {

    internal val posDevice: POSDevice by inject()
    private val payableService: Payable by inject()
    protected val instance: IswPos by inject()
    private lateinit var transactionResponse: Transaction
    private var pollingExecutor: ExecutorService? = null

    // get payment info
    internal lateinit var paymentInfo: PaymentInfo


    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentInfo = intent.getParcelableExtra(Constants.KEY_PAYMENT_INFO)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.isw_menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.cancelPayment -> {
                // TODO show confirmation dialog
                finish()
                true
            }
            R.id.changePaymentMethod -> {
                val exclude = when(this) {
                    is QrCodeActivity -> BottomSheetOptionsDialog.QR
                    is PayCodeActivity -> BottomSheetOptionsDialog.PAYCODE
                    is CardActivity -> BottomSheetOptionsDialog.CARD
                    is UssdActivity -> BottomSheetOptionsDialog.USSD
                    else -> BottomSheetOptionsDialog.NONE
                }

                return showPaymentOptions(exclude)
            }
            else -> false
        }
    }

    private fun showPaymentOptions(exclude: String): Boolean {
        val info: PaymentInfo = intent.getParcelableExtra(Constants.KEY_PAYMENT_INFO)
        val optionsDialog: BottomSheetOptionsDialog = BottomSheetOptionsDialog.newInstance(exclude, info)
        optionsDialog.show(supportFragmentManager, optionsDialog.tag)
        return true
    }

    override fun onResume() {
        super.onResume()
        setSupportActionBar(toolbar)
    }


    // for Qr and USSD only
    fun checkTransactionStatus(status: TransactionStatus) {
        // show progress alert
        showProgressAlert()
        // check payment status and timeout after 5 minutes
        val seconds = resources.getInteger(R.integer.poolingTime)
        val paymentType = when (this) {
            is QrCodeActivity -> PaymentType.QR
            else -> PaymentType.USSD
        }
        pollingExecutor = payableService.checkPayment(paymentType, status, seconds.toLong(), TransactionStatusCallback())
    }

    protected fun showProgressAlert() {
        Alerter.create(this)
                .setTitle(getString(R.string.isw_title_transaction_in_progress))
                .setText(getString(R.string.isw_title_checking_transaction_status))
                .enableProgress(true)
                .setDismissable(false)
                .enableInfiniteDuration(true)
                .setBackgroundColorRes(android.R.color.darker_gray)
                .setProgressColorRes(android.R.color.white)
                .show()
    }

    private fun completePayment() {
        Alerter.clearCurrent(this)
        showTransactionResult(transactionResponse)
    }

    protected fun stopPolling() {
        Alerter.clearCurrent(this)
        pollingExecutor?.shutdownNow()
    }

    internal fun showTransactionResult(transaction: Transaction) {
        val result = getTransactionResult(transaction)
        // only show result activity if result is non-null
        result?.let {
            val resultIntent = Intent(this, TransactionResultActivity::class.java).apply {
                putExtra(Constants.KEY_PAYMENT_INFO, paymentInfo)
                putExtra(TransactionResultActivity.KEY_TRANSACTION_RESULT, it)
            }

            startActivity(resultIntent)
        }
    }



    protected fun toast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    internal abstract fun getTransactionResult(transaction: Transaction): TransactionResult?

    override fun onBackPressed() {
        // do nothing
    }


    // class that provides implementation for transaction status callbacks
    private inner class TransactionStatusCallback: TransactionRequeryCallback {


        override fun onTransactionCompleted(transaction: Transaction) = runOnUiThread {
            // set and complete payment
            transactionResponse = transaction
            completePayment()
        }

        override fun onTransactionStillPending(transaction: Transaction) {
            // extend the time for the notification
        }

        override fun onTransactionError(transaction: Transaction?, throwable: Throwable?) = runOnUiThread {
            // get error message
            val message = throwable?.message
                    ?: transaction?.responseDescription
                    ?: "An error occurred, please try again"


            // clear current notification
            Alerter.clearCurrent(this@BaseActivity)

            // change notification to error notification
            Alerter.create(this@BaseActivity)
                    .setTitle(getString(R.string.isw_title_transaction_error))
                    .setText(message)
                    .setIcon(R.drawable.ic_error)
                    .setDismissable(false)
                    .setBackgroundColorRes(android.R.color.holo_red_dark)
                    .setDuration(15 * 1000)
                    .show()
        }

        override fun onTransactionTimeOut() = runOnUiThread {
            // change notification to error notification

            // clear current notification
            Alerter.clearCurrent(this@BaseActivity)

            // change notification to error notification
            Alerter.create(this@BaseActivity)
                    .setTitle(getString(R.string.isw_title_transaction_timeout))
                    .setText(getString(R.string.isw_content_transaction_in_progress_time_out))
                    .setIcon(R.drawable.ic_warning)
                    .setDismissable(false)
                    .setBackgroundColorRes(android.R.color.holo_orange_dark)
                    .setDuration(15 * 1000)
                    .show()
        }

    }

}