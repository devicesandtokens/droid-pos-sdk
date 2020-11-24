package com.interswitchng.smartpos.shared.interfaces.retrofit

import com.igweze.ebi.simplecalladapter.Simple
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface IKeyService {


    @GET
    fun getKimonoKey(@Url url: String,
                     @Query("cmd") cmd: String,
                     @Query("terminal_id") terminalId: String,
                     @Query("pkmod") pkmod: String,
                     @Query("pkex") pkex: String,
                     @Query("pkv") pkv: String,
                     @Query("keyset_id") keyset_id: String,
                     @Query("der_en") der_en: String): Call<ResponseBody>
    @GET
    fun downloadTerminalParameters(@Url url: String): Call<AllTerminalInfo>
}