package com.interswitchng.smartpos.shared.models.printer.info

enum class TransactionType {
    // NOTE: Do not change the order of these values, because of DB queries

    Purchase, PreAuth, Completion, Refund, Reversal, CardNotPresent, BillPayment, CashOut, PayCode;


    companion object {
        private val values = values();
        fun getByValue(value: Int) = values.firstOrNull { it.ordinal == value }
    }
}


