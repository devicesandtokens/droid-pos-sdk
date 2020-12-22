package com.interswitchng.smartpos.modules.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.navArgs
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.dialogs.FingerprintBottomDialog
import com.interswitchng.smartpos.modules.main.dialogs.MerchantCardDialog
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.models.core.UserType
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.printer.slips.TransactionSlip
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.TransactionResponse
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils
import com.interswitchng.smartpos.shared.utilities.*
import com.interswitchng.smartpos.shared.utilities.DialogUtils
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.viewmodel.TransactionResultViewModel
import kotlinx.android.synthetic.main.isw_fragment_receipt.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class ReceiptFragment : BaseFragment(TAG) {

    private val receiptFragmentArgs by navArgs<ReceiptFragmentArgs>()
    private val transactionResponseModel by lazy { receiptFragmentArgs.TransactionResponseModel }
    private val result by lazy { transactionResponseModel.transactionResult }
    private val isFromActivityDetail by lazy { receiptFragmentArgs.IsFromActivityDetail }
    private val type by lazy { receiptFragmentArgs.PaymentModel.type }
    private val paymentModel by lazy { receiptFragmentArgs.PaymentModel }

    private val resultViewModel: TransactionResultViewModel by viewModel()

    private var printSlip: TransactionSlip? = null
    private var hasPrintedMerchantCopy = false
    private var hasPrintedCustomerCopy = false
    private var hasClickedReversal = false

    private lateinit var reversalResult: TransactionResult


    override val layoutId: Int
        get() = R.layout.isw_fragment_receipt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUI()
    }

    private fun displayTransactionResultIconAndMessage() {
        println("Called responseCode ----> ${result?.responseCode}")
        Logger.with("Reciept Fragment").logErr(result?.responseCode.toString())
        when (result?.responseCode) {
            IsoUtils.TIMEOUT_CODE -> {
                if (result?.type == TransactionType.Purchase && result?.paymentType == PaymentType.Card) {
                    if (!isFromActivityDetail) {
                        initiateReversal()
                    }
                }
                transactionResponseIcon.setImageResource(R.drawable.isw_failure)
                isw_receipt_text.text = "Failed!"
                isw_transaction_msg.text = "Your transaction was unsuccessful"
            }

            IsoUtils.OK -> {
                transactionResponseIcon.setImageResource(R.drawable.isw_round_done_padded)
                isw_transaction_msg.text = "Your transaction was successful"
                when (transactionResponseModel.transactionType) {
                    TransactionType.Purchase-> isw_receipt_text.text =
                            getString(R.string.isw_purchase_completed)
                    TransactionType.PreAuth-> isw_receipt_text.text =
                            getString(R.string.isw_pre_authorization_completed)
                    TransactionType.CardNotPresent-> isw_receipt_text.text =
                            getString(R.string.isw_card_not_present_completed)
                    TransactionType.Completion-> isw_receipt_text.text =
                            getString(R.string.isw_completion_completed)
                    TransactionType.Refund-> isw_receipt_text.text =
                            getString(R.string.isw_refund_completed)
                    TransactionType.Reversal -> isw_receipt_text.text =
                            getString(R.string.isw_reversal_completed)
                    TransactionType.CashOut -> isw_receipt_text.text =
                            getString(R.string.isw_cash_out_completed)
                    TransactionType.PayCode -> isw_receipt_text.text =
                            getString(R.string.isw_pay_code_completed)
                }
            }

            else -> {
                transactionResponseIcon.setImageResource(R.drawable.isw_failure)
                isw_receipt_text.text = "Failed!"
                isw_transaction_msg.text =
                        result?.responseMessage//"Your transaction was unsuccessful"
            }
        }
    }

    private fun displayTransactionDetails() {
        isw_date_text.text = getString(R.string.isw_receipt_date, result?.dateTime)
        val amountWithCurrency = result?.amount.let { DisplayUtils.getAmountWithCurrency(it.toString()) }
        Logger.with("Reciept fragment").logErr(amountWithCurrency)
        //Logger.with("Recipet fragment amount").logErr(result!!.amount)
        isw_amount_paid.text = getString(R.string.isw_receipt_amount, amountWithCurrency)

        isw_stan.text = result?.stan?.padStart(6, '0')
        isw_aid.text = result?.AID
        isw_terminal_id.text = terminalInfo.terminalId
        val expiryYear = result?.cardExpiry?.take(2).toString()
        val expiryMonth = result?.cardExpiry?.takeLast(2).toString()
        var expiryDate = "${expiryMonth}/${expiryYear}"
        if(type == PaymentModel.TransactionType.CARD_NOT_PRESENT){
           expiryDate = "${expiryYear}/${expiryMonth}"
        }
        isw_expiry_date.text = expiryDate
        isw_card_pan.text = result!!.cardPan.run {
            val length = result!!.cardPan.length
            if (length < 10) return@run ""
            val firstFour = result!!.cardPan.substring(0..3)
            val middle = "*".repeat(length - 8)
            val lastFour = result!!.cardPan.substring(length - 4 until length)
            return@run "$firstFour$middle$lastFour"
        }
        isw_rrn.text = result?.rrn
        isw_ref.text = result?.ref
        isw_auth_id.text = result?.authorizationCode
        isw_date_time_text.text = result?.originalTransmissionDateTime
        var paymentType = result?.paymentType.toString()
        if(type == PaymentModel.TransactionType.CARD_NOT_PRESENT){
            paymentType = "CardNotPresent"
        }
        isw_payment_type_text.text = paymentType

        hideEmptyViews()

        val cardTypeName = when (result?.cardType) {
            CardType.MASTER -> "Master Card"
            CardType.VISA -> "Visa Card"
            CardType.VERVE -> "Verve Card"
            CardType.AMERICANEXPRESS -> "American Express Card"
            CardType.CHINAUNIONPAY -> "China Union Pay Card "
            CardType.None -> "Unknown Card"
            else -> "Unknown Card"
        }

        isw_card_type.text = getString(R.string.isw_receipt_payment_type, cardTypeName)

        if(result?.paymentType != PaymentType.Card || type == PaymentModel.TransactionType.CARD_NOT_PRESENT){
            isw_card_type.visibility = View.GONE
            isw_card_type_label.visibility = View.GONE
        }
    }

    private fun hideEmptyViews() {
        if (isw_rrn.text.isEmpty()) {
            isw_rrn.visibility = View.GONE
            isw_rrn_label.visibility = View.GONE
        }
        if(isw_card_pan.text.isEmpty()){
            isw_card_pan.visibility = View.GONE
            isw_card_pan_label.visibility = View.GONE
        }
        if(isw_expiry_date.text == "/"){
            isw_expiry_date.visibility = View.GONE
            isw_expiry_date_label.visibility = View.GONE
        }
        if(isw_ref.text.isEmpty()){
            isw_ref_label.visibility = View.GONE
            isw_ref.visibility = View.GONE
        }
        if(isw_aid.text.isEmpty()){
            isw_aid.visibility = View.GONE
            isw_aid_label.visibility = View.GONE
        }
        if(isw_auth_id.text.isEmpty()){
            isw_auth_id.visibility = View.GONE
            isw_auth_id_label.visibility = View.GONE
        }
        if(isw_date_time_text.text.isEmpty()){
            isw_date_time_text.visibility = View.GONE
            isw_date_time_label.visibility = View.GONE
        }
    }

    private fun logTransaction() {
        result?.let {
            if (isFromActivityDetail.not()) resultViewModel.logTransaction(it)
        }
    }

    private fun handleClicks() {
        try {
//            isw_done.setOnClickListener {
//                val direction = ReceiptFragmentDirections.iswActionIswReceiptfragmentToIswTransaction()
//                val navOptions = NavOptions.Builder()
//                    .setPopUpTo(R.id.isw_transaction, true)
//                    .setLaunchSingleTop(true)
//                    .build()
//                navigate(direction,navOptions)
//            }
        } catch (Ex: Exception) {

        }
        try {
            transactionResponseIcon.setOnClickListener {
                val direction =
                        ReceiptFragmentDirections.iswActionGotoFragmentTransaction()
                val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.isw_transaction, true)
                        .setLaunchSingleTop(true)
                        .build()
                navigate(direction, navOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isw_share_receipt.setOnClickListener {
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, getImageUri(requireContext(), getBitmapFromView(isw_receipt_layout)!!))
                type = "image/*"
            }
            startActivity(Intent.createChooser(shareIntent, "Select Application"))
        }

        isw_reversal.setOnClickListener { view ->

            authorizeAndPerformAction {

                if (hasClickedReversal.not()) {
                    initiateReversal()
                    hasClickedReversal = true
                    view.isClickable = false

                }
            }
        }

        isw_refund.setOnClickListener {

            authorizeAndPerformAction {

                paymentModel.type = PaymentModel.TransactionType.REFUND
                val direction = ReceiptFragmentDirections.iswActionGotoFragmentAmount(paymentModel)
                navigate(direction)

            }

        }
    }

    private fun initiateReversal() {
        val now = Date()
        val txnInfo = TransactionInfo.fromTxnResult(result!!)
        resultViewModel.initiateReversal(terminalInfo, txnInfo)
        resultViewModel.transactionResponse.observe(viewLifecycleOwner, Observer<TransactionResponse> {
            if (it.responseCode == IsoUtils.OK) {
                //if reversal is successful hide button
                isw_reversal.visibility = View.GONE
                result?.reversed = 1
                resultViewModel.updateTransaction(result!!)
            }
            reversalResult = TransactionResult(
                    paymentType = PaymentType.Card,
                    dateTime = DisplayUtils.getIsoString(now),
                    amount = result!!.amount,
                    type = TransactionType.Reversal,
                    accountType = txnInfo.accountType,
                    authorizationCode = it.authCode,
                    responseMessage = IsoUtils.getIsoResultMsg(it.responseCode)!!,
                    responseCode = it.responseCode,
                    cardPan = txnInfo.cardPAN,
                    cardExpiry = txnInfo.cardExpiry,
                    cardType = result!!.cardType,
                    stan = it.stan,
                    pinStatus = result!!.pinStatus,
                    AID = result!!.AID,
                    code = "",
                    telephone = iswPos.config.merchantTelephone,
                    icc = txnInfo.iccString,
                    src = txnInfo.src,
                    csn = txnInfo.csn,
                    cardPin = txnInfo.cardPIN,
                    cardTrack2 = txnInfo.cardTrack2,
                    month = it.month,
                    time = now.time,
                    originalTransmissionDateTime = it.transmissionDateTime,
                    currencyType = txnInfo.currencyType
            )
            resultViewModel.logTransaction(reversalResult)
            Toast.makeText(context, reversalResult.responseMessage, Toast.LENGTH_SHORT).show()
        })
    }


    private lateinit var dialog: MerchantCardDialog

    //authorizeAndPerformAction { it.findNavController().navigate(R.id.isw_goto_account_fragment_action) }
    private fun authorizeAndPerformAction(action: () -> Unit) {
        val fingerprintDialog = FingerprintBottomDialog(isAuthorization = true) { isValidated ->
            if (isValidated) {
                action.invoke()
            } else {
                toast("Unauthorized Access!!")
            }
        }

        val alert by lazy {
            DialogUtils.getAlertDialog(requireContext())
                    .setTitle("Invalid Configuration")
                    .setMessage("The configuration contains invalid parameters, please fix the errors and try saving again")
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->

                        dialog.dismiss()
                    }
        }
        dialog = MerchantCardDialog (isUseCard =  true){
            when (it) {
                MerchantCardDialog.AUTHORIZED -> action.invoke()
                MerchantCardDialog.FAILED -> toast("Unauthorized Access!!")
//                MerchantCardDialog.NOT_ENROLLED -> {
//                    alert.setTitle("Supervisor's card not enrolled")
//                    alert.setMessage("You have not yet enrolled a supervisor's card. Please enroll a supervisor's card on the settings page after downloading terminal configuration.")
//                    alert.show()
//
//                }


                MerchantCardDialog.USE_FINGERPRINT -> fingerprintDialog.show(requireFragmentManager(), FingerprintBottomDialog.TAG)
            }
        }
        dialog.show(requireFragmentManager(), MerchantCardDialog.TAG)
    }


    private fun setUpUI() {
        displayTransactionResultIconAndMessage()
        displayTransactionDetails()
        logTransaction()
        displayButtons()
        handleClicks()
        handlePrint()
    }

    /* private fun resetPinBlockAndKsnData(){

     }*/

    private fun displayButtons() {
        if (isFromActivityDetail) {
            when (type) {
                PaymentModel.TransactionType.CARD_PURCHASE -> {
                    //if transaction type is purchase and it is also successful
                    if (result?.responseCode == IsoUtils.OK || result?.responseCode == IsoUtils.TIMEOUT_CODE || result?.reversed == 1) {
                        isw_reversal.visibility = View.GONE
                        isw_refund.visibility = View.GONE
                    } else {
                        isw_reversal.visibility = View.VISIBLE
                        isw_refund.visibility = View.GONE
                    }
                }
                else -> {
                }
            }

            isw_print_receipt.apply {
                visibility = View.VISIBLE
                text = getString(R.string.isw_title_re_print_receipt)
            }

            isw_share_receipt.visibility = View.VISIBLE

        } else {
            isw_print_receipt.visibility = View.VISIBLE
        }

    }

    private fun handlePrint() {
        printSlip = terminalInfo.let { result?.getSlip(it) }

        isw_print_receipt.setOnClickListener {
            // print slip
            printSlip?.let {
                if (result?.hasPrintedCustomerCopy == 0) {
                    if(isFromActivityDetail){
                        resultViewModel.printSlip(UserType.Customer, it, reprint = true)
                        result?.hasPrintedCustomerCopy = 1
                        resultViewModel.updateTransaction(result!!)
                    } else {
                    resultViewModel.printSlip(UserType.Customer, it)
                    result?.hasPrintedCustomerCopy = 1
                    resultViewModel.updateTransaction(result!!)
                    }
                } else if (result?.hasPrintedMerchantCopy == 1) {
                    resultViewModel.printSlip(UserType.Merchant, it, reprint = true)
                } else {
                    // if has not printed merchant copy
                    // print merchant copy
                    resultViewModel.printSlip(UserType.Merchant, it)
                    // change print text to re-print
                    isw_print_receipt.text = getString(R.string.isw_title_re_print_receipt)
                    result?.hasPrintedMerchantCopy = 1
                    resultViewModel.updateTransaction(result!!)
                }
            }
        }
    }

    companion object {
        const val TAG = "Receipt Fragment"
    }
}
