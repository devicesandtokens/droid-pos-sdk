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
import com.interswitchng.smartpos.shared.utilities.SecurityUtils
import com.interswitchng.smartpos.shared.utilities.toast
import kotlinx.android.synthetic.main.isw_fragment_merchant_card_setup.*
import kotlinx.android.synthetic.main.isw_layout_enroll_pin.*
import kotlinx.android.synthetic.main.isw_layout_supervisors_card_pin.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MerchantCardFragment : BaseFragment(TAG) {

    private val cardViewModel by viewModel<CardViewModel>()
    private val setupViewModel by viewModel<SetupFragmentViewModel>()

    private val store by inject<KeyValueStore>()
    private val deviceName = IswPos.getInstance().device.name

    override val layoutId: Int
        get() = R.layout.isw_fragment_merchant_card_setup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*if (store.getBoolean("SETUP")) {
            proceedToMainActivity()
        }*/
        cardViewModel.emvMessage.observe(this, Observer {
            it?.let(::processMessage)
        })
        cardViewModel.setupTransaction(0, terminalInfo)

        // ensure device supports finger  print
        val supportsFingerPrint = IswPos.getInstance().device.hasFingerPrintReader
        if (!supportsFingerPrint) {
            isw_skip_fingerprint.text = resources.getString(R.string.isw_finish)
            isw_link_fingerprint.visibility = View.GONE
        } else {
            isw_link_fingerprint.setOnClickListener {
                val direction = MerchantCardFragmentDirections.iswActionGotoFragmentPhoneNumber()
                navigate(direction)
            }
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

        isw_button_proceed.setOnClickListener {
            val enteredPin = isw_et_merchant_pin.text.toString()
            val hashedPin = SecurityUtils.getHash(enteredPin)
           // val cardPAN = cardViewModel.getCardPAN()!!

            if (enteredPin == "") {
                context?.toast("Pin Field is empty. Please enter your pin")
            } else {
                setupViewModel.saveMerchantPIN(hashedPin)
                //setupViewModel.saveMerchantPAN(cardPAN)
                isw_imageview.visibility = View.VISIBLE
                isw_insert_card_layout.visibility = View.GONE
                isw_card_detected_layout.visibility = View.GONE
                isw_enter_pin_layout.visibility = View.GONE
                isw_card_and_pin_set_layout.visibility = View.VISIBLE
            }

    }

    }

    private fun proceedToTerminalSettingsActivity() {
        IswPos.showSettingsUpdateScreen()
        requireActivity().finish()
    }

    private fun processMessage(message: EmvMessage) {

        // assigns value to ensure the when expression is exhausted
        when (message) {

            // when card is detected
            is EmvMessage.CardDetected -> {
                isw_insert_card_layout.visibility = View.GONE
                isw_card_detected_layout.visibility = View.VISIBLE
                isw_enter_pin_layout.visibility = View.GONE
            }

            is EmvMessage.EmptyPin -> {
            }

            is EmvMessage.CardDetails -> {
                isw_imageview.visibility = View.INVISIBLE
                isw_insert_card_layout.visibility = View.GONE
                isw_card_detected_layout.visibility = View.GONE
                isw_enter_pin_layout.visibility = View.VISIBLE
                val cardPAN = cardViewModel.getCardPAN()
                setupViewModel.saveMerchantPAN(cardPAN!!)
            }

            // when card should be inserted
            is EmvMessage.InsertCard -> {
                isw_insert_card_layout.visibility = View.VISIBLE
                isw_card_detected_layout.visibility = View.GONE
                isw_enter_pin_layout.visibility = View.GONE
            }

            // when card has been read
            is EmvMessage.CardRead -> {
                cardViewModel.startTransaction(requireContext())
            }

            // when card gets removed
            is EmvMessage.CardRemoved -> {
                store.saveBoolean("SETUP", false)
                proceedToTerminalSettingsActivity()
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
                //isw_card_pan.text = cardViewModel.getCardPAN()
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
                store.saveBoolean("SETUP", false)
                proceedToTerminalSettingsActivity()
            }

            // when transaction is processing
            is EmvMessage.ProcessingTransaction -> {
                if(deviceName == PAX) {
                    isw_insert_card_layout.visibility = View.GONE
                    isw_card_detected_layout.visibility = View.GONE
                    isw_enter_pin_layout.visibility = View.VISIBLE
                    //isw_card_pan.text = cardViewModel.getCardPAN()
                    toast("Pin OK")
                } else{

                }
            }
            EmvMessage.EmptyPin -> {

            }
        }
    }

    companion object {
        const val TAG = "Merchant Card Fragment"
        const val PAX ="PAX"
    }
}