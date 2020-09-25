package com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request


/**
 * This class captures the EMV data
 * necessary for a purchase request
 */
data class EmvData(
        var cardExpiry: String,
        val cardPIN: String,
        var cardPAN: String,
        val cardTrack2: String,
        val src: String, // service restriction code
        val csn: String, // card sequence number
        val AID: String,
        val icc: IccData,
        val pinKsn: String,
        var BIC: String //bank identifier code
)
