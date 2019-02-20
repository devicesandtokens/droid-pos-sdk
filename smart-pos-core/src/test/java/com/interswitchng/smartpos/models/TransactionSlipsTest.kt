package com.interswitchng.smartpos.models

import com.interswitchng.smartpos.shared.models.TerminalInfo
import com.interswitchng.smartpos.shared.models.posconfig.PrintObject
import com.interswitchng.smartpos.shared.models.printslips.info.TransactionInfo
import com.interswitchng.smartpos.shared.models.printslips.info.TransactionStatus
import com.interswitchng.smartpos.shared.models.printslips.info.TransactionType
import com.interswitchng.smartpos.shared.models.printslips.slips.CardSlip
import com.interswitchng.smartpos.shared.models.printslips.slips.TransactionSlip
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class TransactionSlipsTest {



    private lateinit var config: TerminalInfo
    private lateinit var status: TransactionStatus
    private lateinit var info: TransactionInfo
    private lateinit var txnSlip: TransactionSlip

    @Before
    fun setup() {

        val formatter = SimpleDateFormat("YYYY-MM-dd hh:mm:ss", Locale.ENGLISH)

        val expiry = "2702"
        val pan = "5060990580000367864"
        val track2Data = "5060990580000367864D2702601018444995"
        val amount = "2100"

        val alias = "000007"
        val terminalId = "2069018M"
        val merchantId = "IBP000000001384"
        val merchantLocation = "AIRTEL NETWORKS LIMITED PH MALL"
        val currencyCode = "566"
        val posGeoCode = "0023400000000056"
        val merchantName = "Interswitch Groups"
        val terminalType = "PAX"
        val uniqueId = "280-820-589"
        val merchantCode = "MX5882"

        val now = Date()
        info = TransactionInfo(
                paymentType = PaymentType.Card,
                stan = "000120",
                dateTime = formatter.format(now),
                amount = amount,
                cardPan = pan,
                cardExpiry = expiry,
                type = TransactionType.Purchase,
                authorizationCode = "00",
                pinStatus = "PIN Verified",
                cardType = "VISA CARD"
        )

        config = TerminalInfo(alias, terminalId, merchantId, terminalType, uniqueId, merchantLocation, 230, 12)

        status = TransactionStatus(responseMessage = "Transaction Approved", responseCode = "00", AID = "A0000000031010", telephone = "08031234273")

        txnSlip = CardSlip(config, status, info)
    }

    @Test
    fun `should format terminal info correctly`() {
        val merchantName = "merchant name: \n${config.merchantNameAndLocation}\n".toUpperCase()
        val terminalID = "terminal id: \n${config.terminalId}\n".toUpperCase()

        val actualValues = txnSlip.getTerminalInfo()
        val actualMerchantName = (actualValues[0] as PrintObject.Data).value
        val actualLocation = (actualValues[1] as PrintObject.Data).value
        val actualTerminalID = (actualValues[2] as PrintObject.Data).value

        assertEquals(merchantName, actualMerchantName)
        assertEquals(terminalID, actualTerminalID)
    }


    @Test
    fun test() {
        val ex = "PURCHASE"
        val ac = TransactionType.Purchase.toString().toUpperCase()

        println(ac)

        assertEquals(ex, ac)
    }
}