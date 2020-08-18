package com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import org.simpleframework.xml.Element

open class IccData(
        @field:Element(name = "AmountAuthorized")
        var TRANSACTION_AMOUNT: String = "",
        @field:Element(name = "AmountOther")
        var ANOTHER_AMOUNT: String = "000000000000",
        @field:Element(name = "ApplicationInterchangeProfile")
        var APPLICATION_INTERCHANGE_PROFILE: String = "",
        @field:Element(name = "atc")
        var APPLICATION_TRANSACTION_COUNTER: String = "",
        @field:Element(name = "Cryptogram")
        var AUTHORIZATION_REQUEST: String = "",
        @field:Element(name = "CryptogramInformationData")
        var CRYPTOGRAM_INFO_DATA: String = "",
        @field:Element(name = "CvmResults")
        var CARD_HOLDER_VERIFICATION_RESULT: String = "",
        @field:Element(name = "iad")
        var ISSUER_APP_DATA: String = "",
        @field:Element(name = "TransactionCurrencyCode")
        var TRANSACTION_CURRENCY_CODE: String = "",
        @field:Element(name = "TerminalVerificationResult")
        var TERMINAL_VERIFICATION_RESULT: String = "",
        @field:Element(name = "TerminalCountryCode")
        var TERMINAL_COUNTRY_CODE: String = "",
        @field:Element(name = "TerminalType")
        var TERMINAL_TYPE: String = "",
        @field:Element(name = "TerminalCapabilities")
        var TERMINAL_CAPABILITIES: String = "",
        @field:Element(name = "TransactionDate")
        var TRANSACTION_DATE: String = "",
        @field:Element(name = "TransactionType")
        var TRANSACTION_TYPE: String = "",
        @field:Element(name = "UnpredictableNumber")
        var UNPREDICTABLE_NUMBER: String = "",
        @field:Element(name = "DedicatedFileName")
        var DEDICATED_FILE_NAME: String = "") : RealmObject(), Parcelable{


        var INTERFACE_DEVICE_SERIAL_NUMBER: String = ""
        var APP_VERSION_NUMBER: String = ""
        var APP_PAN_SEQUENCE_NUMBER: String = ""

        var iccAsString: String = ""

        constructor(parcel: Parcel) : this(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!) {


                INTERFACE_DEVICE_SERIAL_NUMBER = parcel.readString()!!
                APP_VERSION_NUMBER = parcel.readString()!!
                APP_PAN_SEQUENCE_NUMBER = parcel.readString()!!
                iccAsString = parcel.readString()!!
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(TRANSACTION_AMOUNT)
                parcel.writeString(ANOTHER_AMOUNT)
                parcel.writeString(APPLICATION_INTERCHANGE_PROFILE)
                parcel.writeString(APPLICATION_TRANSACTION_COUNTER)
                parcel.writeString(AUTHORIZATION_REQUEST)
                parcel.writeString(CRYPTOGRAM_INFO_DATA)
                parcel.writeString(CARD_HOLDER_VERIFICATION_RESULT)
                parcel.writeString(ISSUER_APP_DATA)
                parcel.writeString(TRANSACTION_CURRENCY_CODE)
                parcel.writeString(TERMINAL_VERIFICATION_RESULT)
                parcel.writeString(TERMINAL_COUNTRY_CODE)
                parcel.writeString(TERMINAL_TYPE)
                parcel.writeString(TERMINAL_CAPABILITIES)
                parcel.writeString(TRANSACTION_DATE)
                parcel.writeString(TRANSACTION_TYPE)
                parcel.writeString(UNPREDICTABLE_NUMBER)
                parcel.writeString(DEDICATED_FILE_NAME)
        }

        override fun describeContents(): Int {
                return  0
        }

        companion object CREATOR : Parcelable.Creator<IccData> {
                override fun createFromParcel(parcel: Parcel): IccData {
                        return IccData(parcel)
                }

                override fun newArray(size: Int): Array<IccData?> {
                        return arrayOfNulls(size)
                }
        }
}

