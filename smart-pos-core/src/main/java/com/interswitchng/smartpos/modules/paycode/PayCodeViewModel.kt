package com.interswitchng.smartpos.modules.paycode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.shared.interfaces.library.IsoService
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.PaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.AccountType
import com.interswitchng.smartpos.shared.services.iso8583.utils.DateUtils
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.viewmodel.RootViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

internal class PayCodeViewModel(private val isoService: IsoService, private val iswPos: IswPos) : RootViewModel() {


    private val _transactionResult = MutableLiveData<Optional<TransactionResult>>()
    val transactionResult: LiveData<Optional<TransactionResult>> get() = _transactionResult


    fun processOnline(terminalInfo: TerminalInfo, code: String, paymentInfo: PaymentModel) {
        uiScope.launch {

            val result = withContext(ioScope) {
                val response = isoService.initiatePaycodePurchase(terminalInfo, code, paymentInfo)

                return@withContext when (response) {
                    null -> None
                    else -> {

                        val pan = IsoUtils.generatePan(code)
                        // generate expiry date using current date
                        val expiryDate = Calendar.getInstance().let {
                            it.time = response.date
                            val currentYear = it.get(Calendar.YEAR)
                            it.set(Calendar.YEAR, currentYear + 1)
                            it.time
                        }

                        // format track 2
                        val expiry = DateUtils.yearAndMonthFormatter.format(expiryDate)
                        val track2 = "${pan}D$expiry"

                        val now = Date()
                        val responseMsg = IsoUtils.getIsoResultMsg(response.responseCode)
                                ?: "Unknown Error"

                        // extract result
                        Some(TransactionResult(
                                paymentType = PaymentType.PayCode,
                                dateTime = DisplayUtils.getIsoString(now),
                                amount = paymentInfo.amount.toString(),
                                type = TransactionType.PayCode,
                                accountType = AccountType.Default,
                                authorizationCode = response.authCode,
                                responseMessage = responseMsg,
                                responseCode = response.responseCode,
                                stan = response.stan, pinStatus = "", AID = "", code = code,
                                cardPan = pan, cardExpiry = expiry, cardType = CardType.None,
                                telephone = iswPos.config.merchantTelephone, cardTrack2 = track2,
                                cardPin = "", icc = "", csn = "", src = "", time = Date().time,currencyType = paymentInfo.currencyType!!))
                    }
                }
            }


            _transactionResult.value = result
        }
    }

}