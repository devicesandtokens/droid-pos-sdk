package com.interswitchng.smartpos.modules.ussdqr.viewModels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.gojuno.koptional.Optional
import com.interswitchng.smartpos.shared.interfaces.library.HttpService
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.request.CodeRequest
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Bank
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.CodeResponse
import kotlinx.coroutines.*

internal class UssdViewModel(private val paymentService: HttpService) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val _allBanks = MutableLiveData<Optional<List<Bank>>>()
    val allBanks: LiveData<Optional<List<Bank>>> get() = _allBanks

    private val _bankCode = MutableLiveData<Optional<CodeResponse>>()
    val bankCode: LiveData<Optional<CodeResponse>>  get() = _bankCode


    fun loadBanks() {
        uiScope.launch {
            // fetch banks on IO thread
            val banks = uiScope.async(Dispatchers.IO) { paymentService.getBanks() }
            // assign banks on main thread
            _allBanks.value = banks.await()
        }
    }

    fun getBankCode(codeRequest: CodeRequest) {
        uiScope.launch {
            // retrieve ussd code on IO thread
            val response = uiScope.async(Dispatchers.IO) { paymentService.initiateUssdPayment(codeRequest) }
            _bankCode.value = response.await()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}