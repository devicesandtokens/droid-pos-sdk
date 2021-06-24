package com.interswitchng.smartpos.modules.main.transfer.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.modules.main.models.TransactionResponseModel
import com.interswitchng.smartpos.modules.main.transfer.adapters.NotificationHistoryAdaptar
import com.interswitchng.smartpos.modules.main.transfer.adapters.TransactionHistoryAdaptar
import com.interswitchng.smartpos.modules.main.transfer.customdailog
import com.interswitchng.smartpos.modules.main.transfer.hide
import com.interswitchng.smartpos.modules.main.transfer.reveal
import com.interswitchng.smartpos.modules.main.transfer.viewmodels.TransactionHistoryViewmodel
import com.interswitchng.smartpos.modules.menu.report.ReportViewModel
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.models.printer.info.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.TransactionLog
import com.interswitchng.smartpos.shared.models.transaction.TransactionResult
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.PaymentNotificationResponse
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.PaymentNotificationResponseRealm
import com.interswitchng.smartpos.shared.services.iso8583.utils.DateUtils
import com.interswitchng.smartpos.shared.utilities.DialogUtils
import kotlinx.android.synthetic.main.isw_activity_report.*
import kotlinx.android.synthetic.main.isw_fragment_transaction_history.*
import kotlinx.android.synthetic.main.isw_fragment_transaction_history.rvTransactions
import kotlinx.android.synthetic.main.isw_fragment_transaction_history.tvDate
import kotlinx.android.synthetic.main.isw_fragment_transaction_history.tvResultHint
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class PaymentNotificationHistoryFragment{

    private lateinit var adapter: NotificationHistoryAdaptar
    private var selectedDate = Date()
}