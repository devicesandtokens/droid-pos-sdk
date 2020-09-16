package com.interswitchng.smartpos.modules.main.models

import android.os.Parcelable
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransactionResponseModel (
    var transactionResult: TransactionResult? =  null,
    var transactionType: TransactionType = TransactionType.Purchase
): Parcelable
