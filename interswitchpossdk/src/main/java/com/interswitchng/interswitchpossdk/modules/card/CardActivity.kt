package com.interswitchng.interswitchpossdk.modules.card

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.interswitchng.interswitchpossdk.R
import com.interswitchng.interswitchpossdk.shared.errors.DeviceError
import com.interswitchng.interswitchpossdk.shared.interfaces.CardInsertedCallback
import com.interswitchng.interswitchpossdk.shared.interfaces.POSDevice
import com.interswitchng.interswitchpossdk.shared.models.CardDetail
import org.koin.android.ext.android.inject

class CardActivity : AppCompatActivity() {

    private val pos: POSDevice by inject()
    private val cardCallback = CardCallback()

    private lateinit var insertCardContainer: LinearLayout
    private lateinit var insertPinContainer: LinearLayout
    private lateinit var submitButton: LinearLayout
    private lateinit var pinEditText: EditText
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        // show insert card
        // show insert pin
        // on submit, check pin/ get card info


        // on card read, interact with NIBBS

        // attach callback to detect card
        pos.attachCallback(cardCallback)

        // init views
        insertCardContainer = findViewById(R.id.insert_card_instruction_container)
        insertPinContainer = findViewById(R.id.layout_enter_pin_container)
        submitButton = findViewById(R.id.btn_submit_pin)
        pinEditText = findViewById(R.id.edt_pin)
        statusTextView = findViewById(R.id.tv_result)

        submitButton.setOnClickListener {

            val pin = pinEditText.text.toString().trim()
            if (pin.isEmpty()) {
                toast("Enter your pin")
                return@setOnClickListener
            }

            // pos.setTransaction(Transaction(20, pin))
            submitButton.isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pos.detachCallback(cardCallback)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    internal inner class CardCallback : CardInsertedCallback {

        override fun onCardDetected() {
            insertPinContainer.visibility = View.VISIBLE
            insertCardContainer.visibility = View.GONE
        }

        override fun onCardRead(card: CardDetail) {
            insertCardContainer.visibility = View.GONE
            runOnUiThread {
                val status = "CardDetail number is ${card.pan}, Now we are going to contact NIBBS"
                statusTextView.text = status
                submitButton.isEnabled = true
            }
        }

        override fun onCardRemoved() {
            TODO("not implemented")
        }

        override fun onError(error: DeviceError) {
            TODO("not implemented")
        }
    }
}
