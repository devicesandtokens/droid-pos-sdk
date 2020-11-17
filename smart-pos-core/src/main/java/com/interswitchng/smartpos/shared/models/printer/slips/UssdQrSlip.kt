package com.interswitchng.smartpos.shared.models.printer.slips

import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.posconfig.PrintObject
import com.interswitchng.smartpos.shared.models.posconfig.PrintStringConfiguration
import com.interswitchng.smartpos.shared.models.printer.info.TransactionInfo
import com.interswitchng.smartpos.shared.models.printer.info.TransactionStatus
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import java.text.NumberFormat


/**
 * This class is responsible for generating a print
 * slip for USSD and QR transactions
 *
 * @param info the information concerning the current transaction
 */
internal class UssdQrSlip(terminal: TerminalInfo, status: TransactionStatus, private val info: TransactionInfo): TransactionSlip(terminal, status) {


    /**
     * @inherit
     */
    override fun getTransactionInfo(reprint: Boolean): List<PrintObject> {

        val numberFormat = NumberFormat.getInstance()
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2


        val paymentType = pairString("channel", info.paymentType.toString())
        val stan = pairString("stan", info.stan)
        val date = pairString("date", info.dateTime)
        val amount = pairString("amount", DisplayUtils.getAmountWithCurrency(info.amount))

        val typeConfig = PrintStringConfiguration(isTitle = true, isBold = true, displayCenter = true)
        val txnType = pairString("", info.type.toString(), stringConfig = typeConfig)

        val reprintConfig = PrintStringConfiguration(displayCenter = true, isBold = true, isTitle = true)
        val rePrintFlag = pairString("","*** Re-Print ***",stringConfig = reprintConfig )

        val list = mutableListOf(txnType, paymentType, stan, date, line,rePrintFlag ,amount,rePrintFlag, line)
        //remove reprint tag if not a reprint
        if(!reprint) {
            list.removeAll(listOf(rePrintFlag))
        }

        // return transaction info of slip
        return list
    }

}
