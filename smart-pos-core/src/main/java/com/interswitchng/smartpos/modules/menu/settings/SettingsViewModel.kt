package com.interswitchng.smartpos.modules.menu.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.interswitchng.smartpos.shared.Constants
import com.interswitchng.smartpos.shared.interfaces.library.IsoService
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.services.iso8583.utils.FileUtils
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo
import com.interswitchng.smartpos.shared.services.kimono.models.TerminalInformation
import com.interswitchng.smartpos.shared.viewmodel.RootViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.io.InputStream

internal  class SettingsViewModel : RootViewModel(), KoinComponent {

    private val _keysDownloadSuccess = MutableLiveData<Boolean>()
    val keysDownloadSuccess: LiveData<Boolean> = _keysDownloadSuccess

    private val _configDownloadSuccess = MutableLiveData<Boolean>()
    val configDownloadSuccess: LiveData<Boolean> = _configDownloadSuccess

    private val _terminalConfigResponse = MutableLiveData<AllTerminalInfo>()
    val terminalConfigResponse: LiveData<AllTerminalInfo> = _terminalConfigResponse

    fun downloadTerminalParameters(serialNumber: String, isKimono: Boolean) {
        val isoService: IsoService = get { parametersOf(isKimono) }
        uiScope.launch {
            val response = withContext(ioScope) { isoService.downloadTerminalParametersForKimono(serialNumber) }
            _terminalConfigResponse.value = response
            println("Settings ViewModel : $terminalConfigResponse")
        }
    }

    fun downloadKeys(terminalId: String, ip: String, port: Int, isKimono: Boolean, isNibbsTest: Boolean,isEPMS: Boolean) {
        val isoService: IsoService = get { parametersOf(isKimono) }
        uiScope.launch {
            val isSuccessful = withContext(ioScope) {
                isoService.downloadKey(terminalId, ip, port, isNibbsTest, isEPMS)
            }
            _keysDownloadSuccess.value = isSuccessful
        }
    }

    fun downloadTerminalConfig(terminalId: String, ip: String, port: Int, isKimono: Boolean) {
        val isoService: IsoService = get { parametersOf(isKimono) }
        uiScope.launch {
            val isSuccessful = withContext(ioScope) { isoService.downloadTerminalParameters(terminalId, ip, port) }
            _configDownloadSuccess.value = isSuccessful
            //Logger.with("Settings ViewModel").logErr(isoService.downloadTerminalParameters(terminalId,ip,port).toString())
        println("Settings ViewModel : $isSuccessful")
    }
    }


    fun getTerminalInformation(xmlFile: InputStream): TerminalInformation = FileUtils.readXml(TerminalInformation::class.java, xmlFile)

    fun getTerminalInfoFromResponse(info : AllTerminalInfo): TerminalInfo{
        return TerminalInfo(
                terminalId = info.terminalInfoBySerials?.terminalCode.toString(),
                merchantId = info.terminalInfoBySerials?.merchantId.toString(),
                merchantNameAndLocation = info.terminalInfoBySerials?.cardAcceptorNameLocation.toString().padEnd(40,' '),
                merchantCategoryCode = info.terminalInfoBySerials?.merchantCategoryCode.toString(),
                countryCode = Constants.ISW_COUNTRY_CODE,
                currencyCode = Constants.ISW_CURRENCY_CODE,
                callHomeTimeInMin = Constants.ISW_CALL_HOME_TIME_IN_MIN.toIntOrNull() ?: -1,
                serverTimeoutInSec = Constants.ISW_SERVER_TIMEOUT_IN_SEC.toIntOrNull() ?: -1,
                isKimono = true,
                capabilities = Constants.ISW_TERMINAL_CAPABILITIES,
                serverIp = Constants.ISW_TERMINAL_IP,
                serverUrl = Constants.ISW_URL_SETTINGS,
                serverPort = Constants.ISW_CTMS_PORT.toIntOrNull() ?: -1,
                agentId = info.terminalInfoBySerials?.merchantPhoneNumber.toString(),
                agentEmail = info.terminalInfoBySerials?.merchantEmail.toString(),
                merchantCode = Constants.ISW_MERCHANT_CODE,
                merchantAlias = Constants.ISW_MERCHANT_ALIAS,
                isNibbsTest = false,
                isEPMS = false
        )
    }

}