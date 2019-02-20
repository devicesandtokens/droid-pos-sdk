package com.interswitchng.smartpos.shared.models.transaction.ussdqr.request

import com.interswitchng.smartpos.shared.models.transaction.PaymentInfo
import com.interswitchng.smartpos.shared.models.TerminalInfo
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import java.util.*

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

        internal fun from(terminalInfo: TerminalInfo, paymentInfo: PaymentInfo, transactionType: String, qrFormat: String? = null) = CodeRequest (
                alias = "000007", // TODO replace with -> terminalInfo.merchantId,
                amount = "${paymentInfo.amount * 100}",
                bankCode = paymentInfo.bankCode,
                date = DisplayUtils.getIsoString(Date()),
                stan = paymentInfo.stan,
                terminalId = terminalInfo.terminalId,
                transactionType = transactionType,
                qrFormat = qrFormat,
                additionalInformation = TransactionInfo.from(terminalInfo, paymentInfo)
        )
    }
}