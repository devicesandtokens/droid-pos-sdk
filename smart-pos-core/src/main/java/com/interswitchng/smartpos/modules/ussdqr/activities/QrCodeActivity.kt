package com.interswitchng.smartpos.modules.ussdqr.activities


import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.interswitchng.smartpos.shared.activities.BaseActivity
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.shared.interfaces.library.HttpService
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.models.core.UserType
import com.interswitchng.smartpos.shared.models.posconfig.PrintObject
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest.Companion.QR_FORMAT_RAW
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest.Companion.TRANSACTION_QR
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.TransactionStatus
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.CodeResponse
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Transaction
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils
import com.interswitchng.smartpos.shared.utilities.DialogUtils
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.utilities.Logger
import com.interswitchng.smartpos.shared.utilities.ThreadUtils
import kotlinx.android.synthetic.main.isw_activity_qr_code.*
import kotlinx.android.synthetic.main.isw_content_amount.*
import org.koin.android.ext.android.inject
import java.util.*


class QrCodeActivity : BaseActivity() {

    private val paymentService: HttpService by inject()
    private val store: KeyValueStore by inject()


    private val dialog by lazy { DialogUtils.getLoadingDialog(this) }
    private val logger by lazy { Logger.with("QR") }
    private val alert by lazy { DialogUtils.getAlertDialog(this) }

    private var qrData: String? = null
    private var qrBitmap: Bitmap? = null
    private val printSlip = mutableListOf<PrintObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.isw_activity_qr_code)
    }

    override fun onStart() {
        super.onStart()
        setupImage()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }


    private fun setupImage() {

        // set the amount
        val amount = DisplayUtils.getAmountString(paymentInfo)
        amountText.text = getString(R.string.isw_amount, amount)
        paymentHint.text = getString(R.string.isw_hint_qr_code)

        dialog.show()

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap)
            dialog.dismiss()
        } else {
            val request = CodeRequest.from(iswPos.config.alias, terminalInfo, paymentInfo, TRANSACTION_QR, QR_FORMAT_RAW)
            // initiate qr payment
            paymentService.initiateQrPayment(request) { response, throwable ->
                runOnUiThread {
                    dialog.dismiss()
                    if (throwable != null) handleError(throwable)
                    else response?.apply { handleResponse(this) }
                }
            }
        }
    }

    private fun showTransactionMocks(response: CodeResponse) {
        mockButtonsContainer.visibility = View.VISIBLE
        initiateButton.isEnabled = true

        initiateButton.setOnClickListener {
            initiateButton.isEnabled = false
            initiateButton.isClickable = false
            // check transaction status
            checkTransactionStatus(TransactionStatus(response.transactionReference!!, iswPos.config.merchantCode))
        }

        printCodeButton.isEnabled = true
        printCodeButton.setOnClickListener { printCode() }

    }

    private fun printCode() {
        // get printer status
        val printStatus = posDevice.printer.canPrint()

        // print based on status
        when(printStatus) {
            is Error -> toast(printStatus.message)
            else -> {
                printCodeButton.isEnabled = false
                printCodeButton.isClickable = false

                val disposable = ThreadUtils.createExecutor {
                    val status = posDevice.printer.printSlip(printSlip, UserType.Customer)

                    runOnUiThread {
                        Toast.makeText(this, status.message, Toast.LENGTH_LONG).show()
                        printCodeButton.isEnabled = true
                        printCodeButton.isClickable = true
                    }
                }

                disposables.add(disposable)
            }
        }
    }

    private fun handleResponse(response: CodeResponse) {
        when (response.responseCode) {
            CodeResponse.OK -> {
                qrData = response.qrCodeData
                qrBitmap = response.getBitmap(this)
                val bitmap = PrintObject.BitMap(qrBitmap!!)
                printSlip.add(bitmap)
                runOnUiThread {
                    qrCodeImage.setImageBitmap(qrBitmap)
                    showTransactionMocks(response)
                    // check transaction status
                    startPolling(TransactionStatus(response.transactionReference!!, iswPos.config.merchantCode))
                }
            }
            else -> {
                runOnUiThread {
                    val errorMessage = "An error occured: ${response.responseDescription}"
                    toast(errorMessage)
                    showAlert()
                }
            }
        }

        runOnUiThread { dialog.dismiss() }
    }

    private fun handleError(throwable: Throwable) {
        // TODO handle error
        toast(throwable.localizedMessage)
        dialog.dismiss()
        showAlert()
    }

    private fun showAlert() {
        alert.setPositiveButton(R.string.isw_title_try_again) { dialog, _ -> dialog.dismiss(); setupImage() }
                .setNegativeButton(R.string.isw_title_cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    override fun getTransactionResult(transaction: Transaction): TransactionResult? {
        val now = Date()
        val responseMsg = IsoUtils.getIsoResult(transaction.responseCode)?.second
                ?: transaction.responseDescription
                ?: "Error"

        return TransactionResult(
                paymentType = PaymentType.QR,
                dateTime = DisplayUtils.getIsoString(now),
                amount = DisplayUtils.getAmountString(paymentInfo),
                type = TransactionType.Purchase,
                authorizationCode = transaction.responseCode,
                responseMessage = responseMsg,
                responseCode = transaction.responseCode,
                cardPan = "", cardExpiry = "", cardType = CardType.None,
                stan = paymentInfo.getStan(), pinStatus = "", AID = "",
                code = qrData!!, telephone = iswPos.config.merchantTelephone
        )
    }


    override fun onCheckStopped() {
        initiateButton.isEnabled = true
        initiateButton.isClickable = true
        super.onCheckStopped()
    }

}
