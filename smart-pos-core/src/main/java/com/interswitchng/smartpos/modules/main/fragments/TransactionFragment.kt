package com.interswitchng.smartpos.modules.main.fragments

import android.os.Bundle
import android.view.View
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.dialogs.MakePaymentDialog
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.modules.main.models.payment
import com.interswitchng.smartpos.shared.activities.BaseFragment
import kotlinx.android.synthetic.main.isw_fragment_transaction.*
import java.text.DateFormat
import java.util.*

class TransactionFragment: BaseFragment(TAG) {

    override val layoutId: Int
        get() = R.layout.isw_fragment_transaction

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isw_date.text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(System.currentTimeMillis()))
        isw_merchant_name.text = "SmartWare Solutions"
        isw_make_payment_card.setOnClickListener {
            val sheet = MakePaymentDialog {
                //Handle click listener here
                logger.logErr("Item number clicked ===== $it")
                when (it) {
                    0 -> {
                        val payment = payment {
                            type = PaymentModel.Type.MAKE_PAYMENT
                        }
                        navigate(TransactionFragmentDirections.iswActionGotoFragmentAmount(payment))
                    }
                }
            }
            sheet.show(childFragmentManager, TAG)
        }
    }

    companion object {
        const val TAG = "TRANSACTION FRAGMENT"
    }
}