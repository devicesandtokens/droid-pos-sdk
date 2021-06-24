package com.interswitchng.smartpos.modules.main.transfer.fragments


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.transfer.*
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.core.UserType
import com.interswitchng.smartpos.shared.models.posconfig.PosType
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult

import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.viewmodel.TransactionResultViewModel
import kotlinx.android.synthetic.main.fragment_receipt.*
import kotlinx.android.synthetic.main.isw_fragment_receipt.*
import kotlinx.coroutines.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class ReceiptFragment : BaseFragment(TAG) {
    private val receiptFragmentArgs by navArgs<ReceiptFragmentArgs>()
    private val job = Job()
    val data by lazy { receiptFragmentArgs.transactionResponse.transactionResult }

    private val resultViewModel: TransactionResultViewModel by viewModel()

    override val layoutId: Int
        get() = R.layout.fragment_receipt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        listenToviewModel()
        if (!receiptFragmentArgs.reprint) {
            handleprint()

        }
    }

    private fun handleprint() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                this@ReceiptFragment.requireActivity().runOnUiThread {
                    doPrinting()
                }
            }

        }, 1000)
    }

    private fun doPrinting() {
        if (IswPos.getInstance().device.name  == PosType.PAX.name) {
            if (receiptFragmentArgs.withAgent) {
                val scope = CoroutineScope(Dispatchers.Main + job)
                scope.launch {
                    getScreenBitMap(this@ReceiptFragment.requireActivity(), root_view_for_print_page)?.let { resultViewModel.printSlipNew(it) }
                    delay(3000L)
                    customer_title.text = "*** MERCHANT COPY ***"
                    delay(1000L)
                    getScreenBitMap(this@ReceiptFragment.requireActivity(), root_view_for_print_page)?.let { resultViewModel.printSlipNew(it) }
                }
            } else {
                getScreenBitMap(this@ReceiptFragment.requireActivity(), root_view_for_print_page)?.let { resultViewModel.printSlipNew(it) }
            }
        } else {
            if (receiptFragmentArgs.withAgent){
                val scope = CoroutineScope(Dispatchers.Main + job)
                scope.launch {
                    data?.let { printTelpo(it, false) }
                    delay(2000L)
                    data?.let { printTelpo(it, true) }
                }
            } else {
                data?.let { printTelpo(data!!, false) }
            }
        }


    }
    private fun listenToviewModel() {
        val owner = { lifecycle }
        with(resultViewModel) {
            printerMessage.observe(owner) {
                showSnack(agent_title, it.toString())
            }
        }
    }

    private fun setupUI() {
        agent_value.text = terminalInfo.merchantNameAndLocation
        terminal_id_title.text = "TERMINAL ID: ${terminalInfo.terminalId}"
        tel_title.text = "TEL: ${terminalInfo.agentId}"
        withdraw_title.text = when (data?.type) {
            TransactionType.CashOutPay -> "Cash-Out"

            TransactionType.Transfer -> "Transfer"
            else -> "Cashout"
        }
        channel_title.text = "CHANNEL: ${data?.paymentType?.name}"
        date_title.text = "DATE: ${getDate(data?.dateTime.toString())}"
        time_title.text = "TIME: ${getTime(data?.dateTime.toString())}"
        println("this is amount in receipt: ${data!!.amount}")
        amount_title.text = getHtmlString("AMOUNT: ${data?.amount.let { DisplayUtils.getAmountWithCurrency(it.toString()) }}")

        val cardTypeName = when (data?.cardType) {
            CardType.MASTER -> "Master Card"
            CardType.VISA -> "Visa Card"
            CardType.VERVE -> "Verve Card"
            CardType.AMERICANEXPRESS -> "American Express Card"
            CardType.CHINAUNIONPAY -> "China Union Pay Card "
            CardType.None -> "Unknown Card"
            else -> "Unknown Card"
        }
        card_title.text = "CARD TYPE: ${cardTypeName}"
        pan_title.text = "CARD PAN: ${mask(data?.cardPan.toString())}"
        expiry_date_title.text = "EXPIRY DATE: ${formatExpiryDate(data?.cardExpiry.toString(), "/")}"
        stan_title.text  = "STAN: ${data?.getTransactionInfo()?.stan?.padStart(6, '0')}"
        aid_title.text = "AID: ${data?.AID}"

        if (data?.getTransactionStatus()?.responseCode.toString() == "00") {
            retain_receipt_title.reveal()
            transaction_approved_title.text = "${data?.getTransactionStatus()?.responseMessage}"
        } else {
            transaction_approved_title.text = "${data?.getTransactionStatus()?.responseMessage}"
        }
        ref_title.text = "REF: ${data?.ref}"

        if (receiptFragmentArgs.reprint) {
            customer_title.text = "*** MERCHANT COPY ***"
            reprint_title.reveal()
            reprint_title_.reveal()
            line_after_reprint1.reveal()
            line_before_reprint2.reveal()
            isw_reprint_btn.reveal()
        }

        isw_go_to_landing.setOnClickListener {
            findNavController().popBackStack(R.id.isw_transferlandingfragment, false)
        }

        isw_reprint_btn.setOnClickListener {
            val scope = CoroutineScope(Dispatchers.Main + job)
            scope.launch {
                isw_reprint_btn.hide()
                delay(100L)
                doPrinting()
                delay(100L)
                isw_reprint_btn.reveal()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReceiptFragment()
        val TAG = "receipt fragment"
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun printTelpo(result: TransactionResult, isAgent: Boolean) {
        val printSlip = terminalInfo.let { result?.getSlip(it) }
            // print slip

            printSlip?.let {
                if (isAgent){
                    resultViewModel.printSlip(UserType.Merchant, it)
                    result?.hasPrintedMerchantCopy = 1
                    resultViewModel.updateTransaction(result!!)
                } else {
                    if (result?.hasPrintedCustomerCopy == 0) {
                        resultViewModel.printSlip(UserType.Customer, it)
                        result?.hasPrintedCustomerCopy = 1
                        resultViewModel.updateTransaction(result!!)
                    } else if (result?.hasPrintedMerchantCopy == 1) {
                        resultViewModel.printSlip(UserType.Merchant, it, reprint = true)
                    } else {
                        // if has not printed merchant copy
                        // print merchant copy
                        resultViewModel.printSlip(UserType.Merchant, it)
                        result?.hasPrintedMerchantCopy = 1
                        resultViewModel.updateTransaction(result!!)
                    }
                }

            }

    }

}