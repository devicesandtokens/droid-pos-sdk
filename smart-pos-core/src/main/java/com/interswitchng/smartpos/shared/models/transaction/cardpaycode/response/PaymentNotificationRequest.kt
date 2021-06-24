package com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response

import android.os.Parcelable
import io.realm.RealmObject
import kotlinx.android.parcel.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import retrofit2.http.Field
import java.lang.ref.PhantomReference

data class PaymentNotificationRequest (
        var terminalId: String? = "",
        var merchantId: String? = "",
        var  referenceNumber: String? ="",
        var stan: String? = "",
        var authId : String? = "",
        var transactionDate : String? = "",
        var description: String? = "",
        var responseCode: String? = "00",
        var amount: String? = ""
)

@Root(name = "PaymentNotificationResponse", strict = false)
data class PaymentNotificationResponse (
       @field:Element(name = "transactionDate", required = false)
       var  transactionDate: String? = null,
       @field:Element(name = "PaymentLogId", required = false)
       var  PaymentLogId: String? = "",
       @field:Element(name = "notificationMessage", required = false)
       var  notificationMessage: String? = "",
       @field:Element(name = "notificationStatus", required = false)
       var  notificationStatus: String? = "",
       @field:Element(name = "Status", required = false)
       var Status: Int? = 0
)

@Root(name = "Payments", strict = false)
data class Payments (
        @field:Element(name = "Payment", required = false)
        var  payment: Payment? = null
)

@Root(name = "Payment", strict = false)
data class Payment (
        @field:Element(name = "PaymentLogId", required = false)
        var  PaymentLogId: String? = "",
        @field:Element(name = "Status", required = false)
        var Status: Int? = 0
)

@Parcelize
open class PaymentNotificationResponseRealm (
        var amount: String? = "",
        var  transactionDate: Long? = null,
        var  PaymentLogId: String? = "",
        var  notificationMessage: String? = "",
        var  notificationStatus: String? = "",
        var Status: Int? = 0
) : RealmObject(), Parcelable

