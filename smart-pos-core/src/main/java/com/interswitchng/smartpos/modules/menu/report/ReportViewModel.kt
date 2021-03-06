package com.interswitchng.smartpos.modules.menu.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.interfaces.library.TransactionLogService
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.core.UserType
import com.interswitchng.smartpos.shared.models.posconfig.PrintObject
import com.interswitchng.smartpos.shared.models.posconfig.PrintStringConfiguration
import com.interswitchng.smartpos.shared.models.printer.info.PrintStatus
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.TransactionLog
import com.interswitchng.smartpos.shared.services.iso8583.utils.DateUtils
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.viewmodel.RootViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

internal class ReportViewModel(
        private val posDevice: POSDevice,
        private val transactionLogService: TransactionLogService,
        private val store: KeyValueStore

) : RootViewModel() {

    private val terminalInfo: TerminalInfo by lazy { TerminalInfo.get(store)!! }

    private val _printButton = MutableLiveData<Boolean>()
    val printButton: LiveData<Boolean> get() = _printButton

    private val _printerMessage = MutableLiveData<String>()
    val printerMessage: LiveData<String> get() = _printerMessage

    private val _endOfDatTransactions = MutableLiveData<List<TransactionLog>>()
    val endOfDatTransactions: LiveData<List<TransactionLog>> = _endOfDatTransactions


    // setup paged list config
    private val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(10)
            .build()

    fun getReport(day: Date): LiveData<PagedList<TransactionLog>> {
        return transactionLogService.getTransactionFor(day, config)
    }

    fun clearEod(day: Date){
        return transactionLogService.clearEndOFDay(day)
    }

    fun getReport(day: Date, transactionType: TransactionType): LiveData<PagedList<TransactionLog>> {
        return transactionLogService.getTransactionFor(day, transactionType, config)
    }

    fun printEndOfDay(date: Date, transactions: List<TransactionLog>, transactionType: TransactionType) {

        uiScope.launch {
            // get printer status on IO thread
            val printStatus = withContext(ioScope) {
                posDevice.printer.canPrint()
            }

            when (printStatus) {
                is PrintStatus.Error -> {
                    _printerMessage.value = printStatus.message
                }
                else -> {
                    // print transaction for current type
                    printTransactions(transactionType, transactions, date)
                }
            }
        }
    }


    fun printAll(date: Date) {
        // get printer status on IO thread
        uiScope.launch {

            for (transactionType in TransactionType.values()) {

                // get transactions for current type
                val transactions = withContext(ioScope) {
                    transactionLogService.getTransactionListFor(date, transactionType)
                }

                // get print status before printing
                val printStatus = withContext(ioScope) {
                    posDevice.printer.canPrint()
                }
                when (printStatus) {
                    is PrintStatus.Error -> {
                        _printerMessage.value = printStatus.message
                    }
                    else -> {
                        // print transaction for current type
                        printTransactions(transactionType, transactions, date)

                        // delay for 2 secs before printing again
                        delay(2000)
                    }
                }
            }
        }
    }

    private suspend fun printTransactions(transactionType: TransactionType, transactions: List<TransactionLog>, date: Date) {
        if (transactions.isEmpty()) return

        // get name of transaction type
        val typeName = transactionType.name
        // get slip for current date
        val slipItems = transactions.toSlipItems(date, typeName)

        // print code in IO thread
        val status = withContext(ioScope) {
            posDevice.printer.printSlip(
                    slipItems,
                    UserType.Merchant
            )
        }

        // publish print message
        _printerMessage.value = status.message
        val printed = status !is PrintStatus.Error
        // enable print button
        _printButton.value = printed
    }


    fun getEndOfDay(date: Date): LiveData<List<TransactionLog>> {
        // disable print button
        _printButton.value = false

        return transactionLogService.getTransactionFor(date)
    }


    fun getEndOfDay(date: Date, transactionType: TransactionType): LiveData<List<TransactionLog>> {
        // disable print button
        _printButton.value = false

        return transactionLogService.getTransactionFor(date, transactionType)
    }


    private fun List<TransactionLog>.toSlipItems(date: Date, transactionType: String): MutableList<PrintObject> {

        val transactionTypeString = when (transactionType) {
            "CashOutPay" -> "Completion"
            else -> "Inquiry"
        }

        // create the title for printout
        val title = PrintObject.Data(transactionTypeString, PrintStringConfiguration(isTitle = true, displayCenter = true))

        // initialize list with the title and a line under
        val newLine = PrintObject.Data("\n")
        val list = mutableListOf(title, newLine, PrintObject.Line)

        //create the addressTitle for printout
        val addressTitle = PrintObject.Data("\nAddress\n", PrintStringConfiguration(isBold = true))
        //add addressTitle
        list.add(addressTitle)
        //create the address for printout
        val address = PrintObject.Data("${terminalInfo.merchantNameAndLocation}\n")
        // add address
        list.add(address)


        //add line
        list.add(PrintObject.Line)


        //add terminalIdTitle
        val terminalIdTitle = PrintObject.Data("TerminalID\n", PrintStringConfiguration(isBold = true))
        //add terminalIdTitle
        list.add(terminalIdTitle)
        //create the terminalId for printout
        val terminalId = PrintObject.Data("${terminalInfo.terminalId}\n")
        // add terminalId
        list.add(terminalId)


        //add line
        list.add(PrintObject.Line)

        // add date
        val dateString = DateUtils.shortDateFormat.format(date)
        val dateTitle = PrintObject.Data("Date: $dateString\n", PrintStringConfiguration(isBold = true))
        //add dateTitle
        list.add(dateTitle)


        // add line
        list.add(PrintObject.Line)


        // table title
        val amountTitle = "Amt".padEnd(15,'-')
        val tableTitle = PrintObject.Data("Time---$amountTitle Card--Status\n")
        list.add(tableTitle)
        list.add(PrintObject.Line)

        var transactionApproved = 0
        var transactionApprovedAmount = 0.0
        var transactionFailedAmount = 0.0

        // add each item into the end of day list
        this.forEach {
            val slipItem = it.toSlipItem()
            list.add(slipItem)

            //add successful transactions
            if (it.responseCode == IsoUtils.OK) {
                transactionApproved += 1
                transactionApprovedAmount += DisplayUtils.getAmountString(it.amount)
            } else {
                transactionFailedAmount += DisplayUtils.getAmountString(it.amount)
            }
        }
        // add line
        list.add(PrintObject.Line)

        //create summary title
        val summary = PrintObject.Data("Summary\n", PrintStringConfiguration(isBold = true))
        //add summaryTitle
        list.add(summary)

        // total number of transactions
        val transactionSum = this.size


        list.add(PrintObject.Data("Total Transactions: $transactionSum\n", PrintStringConfiguration(isBold = true)))
        val transactionApprovedAmountTo2dp = String.format("%.2f", transactionApprovedAmount)
        val transactionFailedAmountTo2dp = String.format("%.2f", transactionFailedAmount)

        val transactionFailed = transactionSum - transactionApproved
        list.add(PrintObject.Data("Total Passed Transaction: $transactionApproved\n", PrintStringConfiguration(isBold = true)))
        list.add(PrintObject.Data("Total Failed Transaction: $transactionFailed\n", PrintStringConfiguration(isBold = true)))
        list.add(PrintObject.Data("Total Approved Amount: $transactionApprovedAmountTo2dp\n", PrintStringConfiguration(isBold = true)))
        list.add(PrintObject.Data("Total Failed Amount: $transactionFailedAmountTo2dp\n", PrintStringConfiguration(isBold = true)))
        list.add(PrintObject.Line)

        return list
    }

    private fun TransactionLog.toSlipItem(): PrintObject {
        val date = Date(this.time)
        val dateStr = DateUtils.hourMinuteFormat.format(date).padEnd(7,'-')
        val amount = DisplayUtils.getAmountString(this.amount.toInt()).padEnd(15,'-')
        val code = this.responseCode
        val card = this.cardPan.takeLast(4).padEnd(6,'-')
        val status = if (code == IsoUtils.OK) "PASS" else "FAIL"


        return PrintObject.Data("$dateStr $amount $card $status\n")
    }

    private fun formatAmount(amount: String): String {
        val spaceCount = 13 - amount.length
        val padding = " ".repeat(spaceCount)
        return amount + padding
    }
}