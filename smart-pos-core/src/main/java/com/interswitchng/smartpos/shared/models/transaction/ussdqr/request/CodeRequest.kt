package com.interswitchng.smartpos.shared.models.transaction.ussdqr.request

import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.shared.models.transaction.PaymentInfo
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import java.util.*


/**
 * This class represents information required for
 * issuing out QR and USSD code purchase request
 */
internal data class CodeRequest(
        val alias: String,
        val amount: String,
        val bankCode: String?,
        val date: String,
        val stan: String,
        val terminalId: String,
        val transactionType: String,
        val qrFormat: String?,
        val additionalInformation: TransactionInfo
) {

    companion object {
        // transaction types
        internal const val TRANSACTION_QR = "QR"
        internal const val TRANSACTION_USSD = "USSD"

        // qr request formats
        internal const val QR_FORMAT_BITMAP = "BITMAP"
        internal const val QR_FORMAT_RAW = "RAW"
        internal const val QR_FORMAT_FULL = "FULL"

        internal fun from(alias: String, terminalInfo: TerminalInfo, paymentInfo: PaymentInfo, paymentModel: PaymentModel, transactionType: String, qrFormat: String? = null) : CodeRequest {

            return CodeRequest (
                    alias = alias,
                    amount = "${paymentInfo.amount}",
                    bankCode = paymentInfo.bankCode,
                    date = DisplayUtils.getIsoString(Date()),
                    stan = paymentInfo.getStan(),
                    terminalId = terminalInfo.terminalId,
                    transactionType = transactionType,
                    qrFormat = qrFormat,
                    additionalInformation = TransactionInfo.from(terminalInfo, paymentInfo, paymentModel)
            )
        }
    }
}