package com.interswitchng.smartpos.modules.sendmoney


import android.os.Bundle
import android.view.View
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.shared.activities.BaseFragment
import kotlinx.android.synthetic.main.isw_fragment_account_transfer.*


class AccountTransferFragment : BaseFragment(TAG) {

    override val layoutId: Int
        get() = R.layout.isw_fragment_account_transfer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
    }

    private fun initializeViews() {

        backImg.setOnClickListener {
            navigateUp()
        }


        isw_proceed.setOnClickListener {
                proceedToPayment()
        }


    }

    private fun proceedToPayment() {
        val payment = PaymentModel()
        payment.type = PaymentModel.TransactionType.CARD_PURCHASE
        val direction =
            AccountTransferFragmentDirections.iswActionIswAccounttransferfragmentToIswFragmentAmount(payment)
        navigate(direction)
    }

    companion object {

        const val TAG = "ECASH FRAGMENT"
    }

}
