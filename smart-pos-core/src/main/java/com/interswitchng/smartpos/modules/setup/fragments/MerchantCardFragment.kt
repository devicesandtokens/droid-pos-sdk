package com.interswitchng.smartpos.modules.setup.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.card.CardViewModel
import com.interswitchng.smartpos.modules.setup.SetupFragmentViewModel
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.EmvMessage
import kotlinx.android.synthetic.main.isw_fragment_merchant_card_setup.*
import kotlinx.android.synthetic.main.isw_layout_supervisors_card_pin.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MerchantCardFragment : BaseFragment(TAG) {

    private val cardViewModel by viewModel<CardViewModel>()
    private val setupViewModel by viewModel<SetupFragmentViewModel>()

    private val store by inject<KeyValueStore>()

    override val layoutId: Int
        get() = R.layout.isw_fragment_merchant_card_setup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (store.getBoolean("SETUP")) {
            proceedToMainActivity()
        }
        cardViewModel.emvMessage.observe(this, Observer {
            it?.let(::processMessage)
        })
        cardViewModel.setupTransaction(0, terminalInfo)

        if (!IswPos.hasFingerprint()) {
            isw_skip_fingerprint.text = resources.getString(R.string.isw_finish)
            isw_link_fingerprint.visibility = View.GONE
        }

        isw_skip_fingerprint.setOnClickListener {
            val cardPAN = cardViewModel.getCardPAN()!!
            setupViewModel.saveMerchantPAN(cardPAN)
            store.saveBoolean("SETUP", true)
            val direction = MerchantCardFragmentDirections.iswActionGotoFragmentSetupComplete()
            navigate(direction)
        }

        isw_link_fingerprint.setOnClickListener {
            val cardPAN = cardViewModel.getCardPAN()!!
            logger.logErr(cardPAN)
            setupViewModel.saveMerchantPAN(cardPAN)
            val direction = MerchantCardFragmentDirections.iswActionGotoFragmentPhoneNumber()
            navigate(direction)
        }
    }

    private fun proceedToMainActivity() {
        IswPos.showMainActivity()
        requireActivity().finish()
    }

    private fun processMessage(message: EmvMessage) {

        // assigns value to ensure the when expression is exhausted
        val ignore = when (message) {

            // when card is detected
            is EmvMessage.CardDetected -> {
                isw_insert_card_layout.visibility = View.GONE
                isw_card_detected_layout.visibility = View.VISIBLE
                isw_enter_pin_layout.visibility = View.GONE
            }

            is EmvMessage.EmptyPin -> {}

            is EmvMessage.CardDetails -> {}

            // when card should be inserted
            is EmvMessage.InsertCard -> {
                isw_insert_card_layout.visibility = View.VISIBLE
                isw_card_detected_layout.visibility = View.GONE
                isw_enter_pin_layout.visibility = View.GONE
            }

            // when card has been read
            is EmvMessage.CardRead -> {
                runBlocking { delay(1000) }

                cardViewModel.startTransaction(requireContext())
            }

            // when card gets removed
            is EmvMessage.CardRemoved -> {

            }

            // when user should enter pin
            is EmvMessage.EnterPin -> {

            }

            // when user types in pin
            is EmvMessage.PinText -> {

            }

            // when pin has been validated
            is EmvMessage.PinOk -> {
                isw_insert_card_layout.visibility = View.GONE
                isw_card_detected_layout.visibility = View.GONE
                isw_enter_pin_layout.visibility = View.VISIBLE
                isw_card_pan.text = cardViewModel.getCardPAN()
                toast("Pin OK")
            }

            // when the user enters an incomplete pin
            is EmvMessage.IncompletePin -> {

            }

            // when pin is incorrect
            is EmvMessage.PinError -> {

            }

            // when user cancels transaction
            is EmvMessage.TransactionCancelled -> {

            }

            // when transaction is processing
            is EmvMessage.ProcessingTransaction -> {

            }
        }
    }

    companion object {
        const val TAG = "Merchant Card Fragment"
    }
}