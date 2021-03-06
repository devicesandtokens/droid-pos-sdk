package com.interswitchng.smartpos.modules.main.fragments

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.navArgs
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.card.CardViewModel
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.modules.main.models.TransactionResponseModel
import com.interswitchng.smartpos.modules.main.models.cardModel
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.PaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.AccountType
import kotlinx.android.synthetic.main.isw_fragment_card_details.*
import org.koin.android.viewmodel.ext.android.viewModel

class CardDetailsFragment : BaseFragment(TAG) {

    private val cardDetailsFragmentArgs by navArgs<CardDetailsFragmentArgs>()
    private val paymentModel by lazy { cardDetailsFragmentArgs.PaymentModel }

    private lateinit var transactionResponseModel: TransactionResponseModel
    private var transactionType: TransactionType = TransactionType.CardNotPresent
    private var accountType = AccountType.Default
    private lateinit var transactionResult: TransactionResult
    private var pinOk = false


    private val cardViewModel: CardViewModel by viewModel()
    private var cardType = CardType.None
    private val paymentInfo by lazy {
        PaymentInfo(
                paymentModel.amount,
                IswPos.getNextStan(),
                paymentModel.stan,
                paymentModel.authorizationId
        )
    }

    override val layoutId: Int
        get() = R.layout.isw_fragment_card_details

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isw_card_details_toolbar.setNavigationOnClickListener { navigateUp() }

        isw_amount.text = Editable.Factory.getInstance().newEditable((paymentModel.amount / 100.0).toString())

//        cardViewModel.transactionResponse.observe(this, Observer {
//            processResponse(it)
//        })

        isw_proceed.setOnClickListener {
            //Logger.with("Proceed CardDetails").log("Hi You clicked Me")

            var amountInput = paymentModel.amount
            val cardModel = cardModel {
                cvv = isw_cvv.text.toString()
                cardPan = isw_card_pan.text.toString()
                expiryDate = isw_card_expiry_date.text.toString()
            }

            paymentModel.newPayment {
                amount = amountInput
                card = cardModel
                paymentType = PaymentModel.PaymentType.CARD_NOT_PRESENT
            }

            runWithInternet {
                val direction = CardDetailsFragmentDirections.iswActionIswFragmentCardDetailsToIswProcessingTransaction(
                        paymentModel, transactionType, CardType.None, accountType, paymentInfo
                )
                navigate(direction)
//                cardViewModel.processOnlineCNP(
//                    paymentModel,
//                    accountType,
//                    terminalInfo,
//                    cardModel.expiryDate!!,
//                    cardModel.cardPan!!
//                )
            }
        }
    }

    override fun onResume() {
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onResume()
    }


//    private fun processResponse(transactionResponse: Optional<Pair<TransactionResponse, EmvData>>) {
//        when (transactionResponse) {
//            is None -> logger.log("Unable to complete transaction")
//            is Some -> {
//                // extract info
//                val response = transactionResponse.value.first
//                val emvData = transactionResponse.value.second
//                val txnInfo =
//                    TransactionInfo.fromEmv(emvData, paymentModel, PurchaseType.Card, accountType)
//                val responseMsg = IsoUtils.getIsoResultMsg(response.responseCode) ?: "Unknown Error"
//                Logger.with("transactionrESponseModel").logErr(responseMsg)
//
//                val pinStatus = when {
//                    pinOk || response.responseCode == IsoUtils.OK -> "PIN Verified"
//                    else -> "PIN Unverified"
//                }
//                val now = Date()
//                transactionResult = TransactionResult(
//                    paymentType = PaymentType.Card,
//                    dateTime = DisplayUtils.getIsoString(now),
//                    amount = paymentModel.amount.toString(),
//                    type = transactionType,
//                    authorizationCode = response.authCode,
//                    responseMessage = responseMsg,
//                    responseCode = response.responseCode,
//                    cardPan = txnInfo.cardPAN,
//                    cardExpiry = txnInfo.cardExpiry,
//                    cardType = cardType,
//                    stan = response.stan,
//                    pinStatus = pinStatus,
//                    AID = emvData.AID,
//                    code = "",
//                    telephone = iswPos.config.merchantTelephone,
//                    icc = txnInfo.iccString,
//                    src = txnInfo.src,
//                    csn = txnInfo.csn,
//                    cardPin = txnInfo.cardPIN,
//                    cardTrack2 = txnInfo.cardTrack2,
//                    month = response.month,
//                    time = response.time,
//                    originalTransmissionDateTime = response.transmissionDateTime
//                )
//                transactionResponseModel = TransactionResponseModel(
//                    transactionResult = transactionResult,
//                    transactionType = PaymentModel.TransactionType.CARD_PURCHASE
//                )
//                Logger.with("transactionrESponseModel").logErr(transactionResponseModel.toString())
//
//                val direction = CardDetailsFragmentDirections.iswActionGotoFragmentReceipt(
//                    PaymentModel = paymentModel,
//                    TransactionResponseModel = transactionResponseModel,
//                    IsFromActivityDetail = false
//                )
//                navigate(direction)
//            }
//        }
//    }

    companion object {
        const val TAG = "Card Details Fragment"
    }
}