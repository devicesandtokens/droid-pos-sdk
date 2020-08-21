package com.interswitchng.smartpos.modules.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import kotlinx.android.synthetic.main.isw_fragment_authentication.*
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
 */
class AuthenticationFragment : BaseFragment(TAG) {

    private val authenticationFragmentArgs by navArgs<AuthenticationFragmentArgs>()
    private val payment by lazy { authenticationFragmentArgs.PaymentModel }


    override val layoutId: Int
        get() = R.layout.isw_fragment_authentication

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideViews()
        handleClick()
    }

    private fun hideViews() {
        if (terminalInfo.isKimono) {
            isw_authorization_id_label.visibility = View.GONE
            isw_original_date_time.visibility = View.GONE
        } else{
            isw_auth_id_label.visibility = View.GONE
            isw_auth_id_value.visibility = View.GONE
        }
    }

    private fun handleClick() {
        isw_proceed.setOnClickListener {
            val originalDateTime = isw_original_date_time.text.toString()
            val stan = isw_stan_value.text.toString()
            val authId = isw_auth_id_value.text.toString()

            if (terminalInfo.isKimono) {
                if (stan.isEmpty() || authId.isEmpty()) {
                    Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                } else {
                    payment.newPayment {
                        this.originalDateAndTime = originalDateTime
                        this.originalStan = stan
                        this.authorizationId = authId
                    }

                    Toast.makeText(context, payment.type.toString(), Toast.LENGTH_LONG ).show()

                    val direction = AuthenticationFragmentDirections.iswActionGotoAmountFromAuthorization(payment)
                    navigate(direction)
                }
            } else{
                if (originalDateTime.isEmpty() || stan.isEmpty()) {
                    Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                } else {
                    payment.newPayment {
                        this.originalDateAndTime = originalDateTime
                        this.originalStan = stan
                        this.authorizationId = authId
                    }

                    Toast.makeText(context, payment.type.toString(), Toast.LENGTH_LONG ).show()

                    val direction = AuthenticationFragmentDirections.iswActionGotoAmountFromAuthorization(payment)
                    navigate(direction)
                }
            }

        }
    }

    companion object {
        const val TAG = "AUTHENTICATION FRAGMENT"
    }
}
