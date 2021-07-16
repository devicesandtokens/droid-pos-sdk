package com.interswitchng.smartpos.modules.main.dialogs

import android.os.Bundle
import android.view.View
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.shared.activities.BaseBottomSheetDialog
import com.interswitchng.smartpos.shared.utilities.SingleArgsClickListener
import kotlinx.android.synthetic.main.isw_sheet_layout_currency_options.*

class CurrencyDialog constructor(
            private val optionClickListener: SingleArgsClickListener<Int>
    ) : BaseBottomSheetDialog() {

        override val layoutId: Int
            get() = R.layout.isw_sheet_layout_currency_options

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            isw_naira.setOnClickListener {
                optionClickListener.invoke(0)
                dismiss()
            }
            isw_dollar.setOnClickListener {
                optionClickListener.invoke(1)
                dismiss()
            }
            /*isw_completion.setOnClickListener {
                optionClickListener.invoke(3)
                dismiss()
            }
            isw_bill_payment.setOnClickListener {
                optionClickListener.invoke(4)
                dismiss()
            }
            isw_refund.setOnClickListener {
                optionClickListener.invoke(5)
                dismiss()
            }*/

        }

        companion object {
            const val TAG = "Currency Dialog"
        }
}