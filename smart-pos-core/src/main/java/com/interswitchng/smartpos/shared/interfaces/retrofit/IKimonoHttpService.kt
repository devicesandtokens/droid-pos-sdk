package com.interswitchng.smartpos.shared.interfaces.retrofit

import com.igweze.ebi.simplecalladapter.Simple
import com.interswitchng.smartpos.shared.Constants
import com.interswitchng.smartpos.shared.services.kimono.models.BillPaymentResponse
import com.interswitchng.smartpos.shared.services.kimono.models.CallHomeRequest
import com.interswitchng.smartpos.shared.services.kimono.models.KimonoKeyRequest
import com.interswitchng.smartpos.shared.services.kimono.models.PurchaseResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

internal interface IKimonoHttpService {


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun callHome(@Url url: String, @Body data: CallHomeRequest): Simple<ResponseBody>


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun completion(@Url url: String, @Body data: RequestBody): Simple<PurchaseResponse>


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun reversePurchase(@Url url: String, @Body reverseRequest: RequestBody): Simple<PurchaseResponse>
//    fun reversePurchase(@Body reverseRequest: ReversalRequest): Simple<PurchaseResponse>


    @POST
    fun reservation(@Url url: String, @Body data: RequestBody): Simple<PurchaseResponse>
//    fun reservation(@Body data: ReservationRequest): Simple<ReservationResponse>


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun makePurchase(@Url url: String, @Body purchaseRequest: RequestBody): Simple<PurchaseResponse>
//    fun makePurchase(@Body purchaseRequest: PurchaseRequest): Simple<ResponseBody>

    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun makeBillPayment(@Url url: String, @Body purchaseRequest: RequestBody): Simple<BillPaymentResponse>


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST
    fun refund(@Url url: String, @Body refundRequest: RequestBody): Simple<PurchaseResponse>


    @Headers("Content-Type: text/xml", "Accept: application/xml", "Accept-Charset: utf-8")
    @POST("")
    fun getPinKey(@Url endpoint: String,
                  @Body request: KimonoKeyRequest): Simple<ResponseBody>


    @Headers("Content-Type: application/xml")
    @GET(Constants.KIMONO_KEY_END_POINT)
    fun getKimonoKey(@Url url: String,
                     @Query("cmd") cmd: String,
                     @Query("terminal_id") terminalId: String,
                     @Query("pkmod") pkmod: String,
                     @Query("pkex") pkex: String,
                     @Query("pkv") pkv: String,
                     @Query("keyset_id") keyset_id: String,
                     @Query("der_en") der_en: String): Simple<ResponseBody>


}