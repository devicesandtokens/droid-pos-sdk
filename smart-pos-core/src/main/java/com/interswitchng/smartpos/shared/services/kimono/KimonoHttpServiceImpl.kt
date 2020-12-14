package com.interswitchng.smartpos.shared.services.kimono

import android.content.Context
import android.util.Base64
import com.igweze.ebi.simplecalladapter.Simple
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.shared.Constants
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice
import com.interswitchng.smartpos.shared.interfaces.library.IsoService
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.interfaces.retrofit.IKeyService
import com.interswitchng.smartpos.shared.interfaces.retrofit.IKimonoHttpService
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.transaction.PaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.AccountType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.IccData
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.PurchaseType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.TransactionResponse
import com.interswitchng.smartpos.shared.services.iso8583.utils.DateUtils
import com.interswitchng.smartpos.shared.services.iso8583.utils.DateUtils.universalDateFormat
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo
import com.interswitchng.smartpos.shared.services.kimono.models.CallHomeRequest
import com.interswitchng.smartpos.shared.services.kimono.models.PurchaseRequest
import com.interswitchng.smartpos.shared.utilities.Logger
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class KimonoHttpServiceImpl(private val context: Context,
                                     private val store: KeyValueStore,
                                     private val device: POSDevice,
                                     private val keyService: IKeyService,
                                     private val httpService: IKimonoHttpService) : IsoService{
    val terminalInfo = TerminalInfo.get(store)
    private val kimonoServiceUrl = terminalInfo?.serverUrl ?: Constants.ISW_KIMONO_BASE_URL



//
//    override fun downloadTerminalParameters(terminalId: String, ip: String, port: Int): Boolean {
//
//
//
//        try {
//
//
//            val terminalData = TerminalInfoParser.parseKimono("2ISW0001",
//                    "2ISW1234567TEST","566","566",100,10,"MX1065","1648C Oko-Awo Street, Victoria Island",
//                    "")?.also {
//
//                it.persist(store) }
//            logger.log("Terminal Data => $terminalData")
//
//            return true
//        } catch (e: Exception) {
//            logger.log(e.localizedMessage)
//            e.printStackTrace()
//        }
//
//        return false
//    }


    val logger by lazy { Logger.with(this.javaClass.name) }

//
//    override fun downloadKey(terminalId: String): Boolean {
//
//        // load test keys
////        val tik = Constants.ISW_DUKPT_IPEK
////        val ksn = Constants.ISW_DUKPT_KSN
//
//        // load keys
////        device.loadInitialKey(tik, ksn)
//        return true
//    }


    override fun downloadKey(terminalId: String, ip: String, port: Int, isNibbsTest: Boolean, isEPMS: Boolean): Boolean {


        try {
            val responseBody = keyService.getKimonoKey(url = Constants.ISW_KIMONO_KEY_URL, cmd = "key", terminalId = terminalId, pkmod = Constants.PKMOD, pkex = Constants.PKEX, pkv = "1", keyset_id = "000002", der_en = "1").execute()


            return if (!responseBody.isSuccessful) {
                Logger.with("KeyResponseCode").log(responseBody.errorBody().toString())
                false
            } else {
                //store.saveString(Constants.KIMONO_KEY, responseBody.body().toString())

                // load test keys
                val tik = Constants.ISW_DUKPT_IPEK
                val ksn = Constants.ISW_DUKPT_KSN

                // load keys
                device.loadInitialKey(tik, ksn)

                Logger.with("KeyResponseMessage").log(responseBody.body()?.string()
                        ?: "no response")
                true
            }
        } catch (e: Exception) {
            logger.log(e.localizedMessage)
            e.printStackTrace()
        }
        return false


    }

    override fun downloadTerminalParametersForKimono(serialNumber: String): AllTerminalInfo? {
        try {
            val url = Constants.ISW_TERMINAL_CONFIG_URL+serialNumber
            val responseBody = keyService.downloadTerminalParameters(url).execute()
            if (responseBody.isSuccessful){
                return responseBody.body()
            } else{
                AllTerminalInfo()
            }
        } catch (e: Exception){
            logger.log(e.localizedMessage)
            e.printStackTrace()
        }

        return AllTerminalInfo()

    }


    override suspend fun callHome(terminalInfo: TerminalInfo): Boolean {

//terminalInfo.merchantId

// terminalInfo.terminalId

        val uid = ""
        val request = CallHomeRequest.create(device.name, terminalInfo, uid)


        val response = httpService.callHome(kimonoServiceUrl, request).run()
        val data = response.body()
        return true
    }


    override  fun initiateCardPurchase(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {
        val now = Date()
        val transmissionDate = universalDateFormat.format(now)
      val requestBody: String = PurchaseRequest.toCardPurchaseString(device,terminalInfo,transaction)
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)

        try {
            val responseBody = httpService.makePurchase(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode =purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode =  purchaseResponse.authCode,// data.authCode,
                        scripts =  purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        transmissionDateTime = transmissionDate,
                        rrn = purchaseResponse.referenceNumber
                )
            }

        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
          //  initiateReversal(request, request.stan) // TODO change stan to authId
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }

    }

    override fun initiateCNPPurchase(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {
        val requestBody: String = PurchaseRequest.toCNPPurchaseString(device,terminalInfo,transaction)
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)

        try {
            val responseBody = httpService.makePurchase(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode = purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode = purchaseResponse.authCode,// data.authCode,
                        scripts = purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        rrn = purchaseResponse.referenceNumber

                )
            }

        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
            //  initiateReversal(request, request.stan) // TODO change stan to authId
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }
    }

    override fun initiateBillPayment(terminalInfo: TerminalInfo, txnInfo: TransactionInfo): TransactionResponse? {
        val requestBody: String = PurchaseRequest.toBillPaymentString(device, terminalInfo, txnInfo)
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)

        try {
            val responseBody = httpService.makeBillPayment(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode = purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode = purchaseResponse.authId,//purchaseResponse.authCode,// data.authCode,
                        scripts = purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        name = purchaseResponse.customerDescription,
                        ref = purchaseResponse.transactionRef,
                        rrn = purchaseResponse.retrievalRefNumber

                )
            }

        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
            //  initiateReversal(request, request.stan) // TODO change stan to authId
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }
    }


    override fun initiateRefund(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {
        // generate purchase request


        val requestBody: String = PurchaseRequest.toRefund(device,terminalInfo,transaction)
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)

//        val request = RefundRequest.create(device.name, terminalInfo, transaction,"")
//        request.pinData?.apply {
//            // remove first 2 bytes to make 8 bytes
//            ksn = ksn.substring(4)
//        }
        var authCode=""
        try {


            val responseBody = httpService.refund(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode =purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode =  purchaseResponse.authCode,// data.authCode,
                        scripts =  purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        rrn = purchaseResponse.referenceNumber
                )
            }



        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
             //initiateReversal(terminalInfo, transaction) // TODO change stan to authId
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = authCode, stan = "", scripts = "")
        }
    }





    override fun initiatePaycodePurchase(terminalInfo: TerminalInfo, code: String, paymentInfo: PaymentModel): TransactionResponse? {

        val now = Date()
        val pan = IsoUtils.generatePan(code)
       /// val amount = String.format(Locale.getDefault(), "%012d", paymentInfo.amount)
        val stan = paymentInfo.getTransactionStan()
//        val now = Date()
       // val date = DateUtils.dateFormatter.format(now)
        val src = "501"

        // generate expiry date using current date
        val expiryDate = Calendar.getInstance().let {
            it.time = now
            val currentYear = it.get(Calendar.YEAR)
            it.set(Calendar.YEAR, currentYear + 1)
            it.time
        }

        // format track 2
        val expiry = DateUtils.yearAndMonthFormatter.format(expiryDate)
        val track2 = "${pan}D$expiry"

        // create transaction info
        val transaction = TransactionInfo(
                cardExpiry = expiry,
                cardPIN = "",
                cardPAN = pan,
                cardTrack2 = track2,
                iccString = "",
                iccData = IccData(),
                src = src,
                csn = "",
                amount = paymentInfo.amount,
                stan = stan,
                purchaseType = PurchaseType.PayCode,
                accountType = AccountType.Default,
                pinKsn = "",
                currencyType = paymentInfo.currencyType!!
        )

        logger.log("i: ${paymentInfo.amount} in kimonoHttpServiceImpl")
        logger.log("i: ${transaction.amount} in kimonoHttpServiceImpl")

        val requestBody: String = PurchaseRequest.toPaycodeString(device,terminalInfo,transaction)
        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)



        try {
            val responseBody = httpService.makePurchase(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message(),
                        date = now
                )
            } else {
                TransactionResponse(
                        responseCode =purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode =  purchaseResponse.authCode,// data.authCode,
                        scripts =  purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        rrn = purchaseResponse.referenceNumber,
                        date = now
                )
            }


        } catch (e: Exception) {
            //logger.logErr(e.localizedMessage)
            e.printStackTrace()
            //initiateReversal(terminalInfo, transaction)
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "",date = now)
        }
    }




    override fun initiateCompletion(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {
        try {

//            request.pinData?.apply {
//                // remove first 2 bytes to make 8 bytes
//                ksn = ksn.substring(4)
//            }

            val requestBody: String = PurchaseRequest.toCompletion(device,terminalInfo,transaction)
            val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)


            val responseBody = httpService.completion(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode =purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode =  purchaseResponse.authCode,// data.authCode,
                        scripts =  purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        rrn = purchaseResponse.referenceNumber
                )
            }



        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }

    }


    override fun initiatePreAuthorization(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {
        try {
            val requestBody: String = PurchaseRequest.toReservation(device,terminalInfo,transaction)
            val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)


            val responseBody = httpService.reservation(kimonoServiceUrl, body).run()
            var purchaseResponse = responseBody.body()

                return if (!responseBody.isSuccessful || purchaseResponse == null) {
                    TransactionResponse(
                            responseCode = IsoUtils.TIMEOUT_CODE,
                            authCode = "",
                            stan = "",
                            scripts = "",
                            responseDescription = responseBody.message()
                    )
                } else {
                    TransactionResponse(
                            responseCode =purchaseResponse.responseCode,//data.responseCode,
                            stan = purchaseResponse.stan,
                            authCode =  purchaseResponse.authCode,// data.authCode,
                            scripts =  purchaseResponse.stan,
                            responseDescription = purchaseResponse.description,//data.description
                            rrn = purchaseResponse.referenceNumber
                    )
                }




        } catch (e: Exception) {
            //logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }

    }


    override fun initiateReversal(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse? {

        val requestBody: String = PurchaseRequest.toReversal(device,terminalInfo,transaction)


        val body = RequestBody.create(MediaType.parse("text/xml"), requestBody)

 try {
     val responseBody = httpService.reversePurchase(kimonoServiceUrl, body).run()
     var purchaseResponse = responseBody.body()

            return if (!responseBody.isSuccessful || purchaseResponse == null) {
                TransactionResponse(
                        responseCode = IsoUtils.TIMEOUT_CODE,
                        authCode = "",
                        stan = "",
                        scripts = "",
                        responseDescription = responseBody.message()
                )
            } else {
                TransactionResponse(
                        responseCode =purchaseResponse.responseCode,//data.responseCode,
                        stan = purchaseResponse.stan,
                        authCode =  purchaseResponse.authCode,// data.authCode,
                        scripts =  purchaseResponse.stan,
                        responseDescription = purchaseResponse.description,//data.description
                        rrn = purchaseResponse.referenceNumber
                )
            }



        } catch (e: Exception) {
     //logger.logErr(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(IsoUtils.TIMEOUT_CODE, authCode = "", stan = "", scripts = "")
        }
    }


    private fun ByteArray.base64encode(): String {
        return String(Base64.encode(this, Base64.NO_WRAP))
    }

    private fun generatePan(code: String): String {
        val bin = "506179"
        var binAndCode = "$bin$code"
        val remainder = 12 - code.length
        // pad if less than 12
        if (remainder > 0)
            binAndCode = "$binAndCode${"0".repeat(remainder)}"

        var nSum = 0
        var alternate = true
        for (i in binAndCode.length - 1 downTo 0) {

            var d = binAndCode[i] - '0'

            if (alternate)
                d *= 2

            // We add two digits to handle
            // cases that make two digits after
            // doubling
            nSum += d / 10
            nSum += d % 10

            alternate = !alternate
        }

        val unitDigit = nSum % 10
        val checkDigit = 10 - unitDigit

        return "$binAndCode$checkDigit"
    }

    private suspend fun <T> Simple<T>.await(): Pair<T?, String?> {
        return suspendCoroutine { continuation ->
            process { response, t ->
                val message =  t?.message ?: t?.localizedMessage

                // log errors
                if (message != null) logger.log(message)
                // pair result and error
                val result = Pair(response, message)
                // return response
                continuation.resume(result)
            }
        }


//
//
//        private fun parseXmlToJsonObject(xml: String) : String {
//            var jsonObj: JSONObject? = null
//            try {
//                jsonObj = Xml.toJSONObject(xml)
//            } catch (e: JSONException) {
//                Log.e("JSON exception", e.message)
//                e.printStackTrace()
//            }
//
//            return jsonObj.toString()
//        }
//
//        fun<T> parseResponse(xml: String, clazz: Class<T>) : T {
//            try {
//                return initializeMoshi().adapter(clazz).fromJson(parseXmlToJsonObject(xml))!!
//            }catch (e: IOException){
//                throw IllegalArgumentException("Could not deserialize: $xml into class: $clazz")
//            }
//        }
//
//        private fun initializeMoshi(): Moshi {
//            return Moshi.Builder()
//                    .add(KotlinJsonAdapterFactory())
//                    .build()
//        }
    }


}
