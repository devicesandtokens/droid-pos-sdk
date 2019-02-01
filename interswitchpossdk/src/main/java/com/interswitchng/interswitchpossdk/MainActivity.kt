package com.interswitchng.interswitchpossdk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.interswitchng.interswitchpossdk.modules.card.CardActivity
import com.interswitchng.interswitchpossdk.modules.paycode.PayCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.QrCodeActivity
import com.interswitchng.interswitchpossdk.modules.ussdqr.UssdActivity
import com.interswitchng.interswitchpossdk.shared.Constants.KEY_PAYMENT_INFO
import com.interswitchng.interswitchpossdk.shared.models.PaymentInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_payment_options.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get payment info
        val paymentInfo: PaymentInfo = intent.getParcelableExtra(KEY_PAYMENT_INFO)
        // set up the UI
        setupUI(paymentInfo)
    }

    private fun setupUI(info: PaymentInfo) {
        ussdPayment.setOnClickListener {
            val ussdIntent = Intent(this, UssdActivity::class.java)
            ussdIntent.putExtra(KEY_PAYMENT_INFO, info)
            ussdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(ussdIntent)
        }

        qrPayment.setOnClickListener {
            val qrIntent = Intent(this, QrCodeActivity::class.java)
            qrIntent.putExtra(KEY_PAYMENT_INFO, info)
            qrIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(qrIntent)
        }

        cardPayment.setOnClickListener {
            val cardIntent = Intent(this, CardActivity::class.java)
            cardIntent.putExtra(KEY_PAYMENT_INFO, info)
            cardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(cardIntent)
        }

        payCodePayment.setOnClickListener {
            val payCodeIntent = Intent(this, PayCodeActivity::class.java)
            payCodeIntent.putExtra(KEY_PAYMENT_INFO, info)
            payCodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(payCodeIntent)
        }
    }
}
