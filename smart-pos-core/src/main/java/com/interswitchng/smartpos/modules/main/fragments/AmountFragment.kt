package com.interswitchng.smartpos.modules.main.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.MainActivity
import com.interswitchng.smartpos.modules.main.dialogs.FingerprintBottomDialog
import com.interswitchng.smartpos.modules.main.dialogs.MerchantCardDialog
import com.interswitchng.smartpos.modules.main.dialogs.PaymentTypeDialog
import com.interswitchng.smartpos.modules.main.models.PaymentModel
import com.interswitchng.smartpos.modules.main.models.payment
import com.interswitchng.smartpos.shared.Constants.EMPTY_STRING
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.utilities.DialogUtils
import com.interswitchng.smartpos.shared.utilities.DisplayUtils
import com.interswitchng.smartpos.shared.utilities.Logger
import kotlinx.android.synthetic.main.isw_fragment_amount.*
import java.text.NumberFormat

const val AMOUNT_LIMIT = 20000000
class AmountFragment : BaseFragment(TAG) {

    private val amountFragmentArgs by navArgs<AmountFragmentArgs>()
    private val payment by lazy { amountFragmentArgs.PaymentModel }

    private val DEFAULT_AMOUNT = "0.00"

    private var amount = EMPTY_STRING

    override val layoutId: Int
        get() = R.layout.isw_fragment_amount

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUI()
        initializeAmount()
        handleProceedToolbarClicks()
        handleDigitsClicks()
    }

    private fun setUpUI() {
        DisplayUtils.hideKeyboard(activity as MainActivity)
        when (payment.type) {
            PaymentModel.TransactionType.PRE_AUTHORIZATION -> {
                isw_proceed.text = getString(R.string.isw_pre_authorize)
            }

            PaymentModel.TransactionType.REFUND -> {
                isw_proceed.text = getString(R.string.isw_refund)
            }
        }
    }

    private fun handleProceedToolbarClicks() {
        isw_proceed.setOnClickListener {
            if (amount == EMPTY_STRING || amount == DEFAULT_AMOUNT) {
                displayInvalidAmountToast()
            } else if(isAmountLimit()){
                displayLimitAmountToast()
            } else {
                proceedWithPayment()
            }
        }
    }

    private fun displayLimitAmountToast() {
        toast("Limit Amount is $AMOUNT_LIMIT")
    }

    private fun proceedWithPayment() {
        val latestAmount = isw_amount.text.toString()
        Logger.with("Amount Fragment").logErr(latestAmount)
        val latestAmountWithoutComma = latestAmount.replace("[$,.]".toRegex(), "")
        Logger.with("Amount Fragment").logErr(latestAmountWithoutComma)
//        val dotIndex = latestAmountWithoutComma.indexOfFirst {
//            it == '.'
//        }

        //val stringWithoutCommaAndDot =  latestAmountWithoutComma.substring(0, dotIndex)
        payment.newPayment {
            amount = latestAmountWithoutComma.toInt()//latestAmount.toDouble()
            formattedAmount = latestAmount
            logger.log("i: $amount in amount fragment")

        }

        when (payment.type) {
            PaymentModel.TransactionType.CARD_PURCHASE -> {
                val bottomDialog = PaymentTypeDialog {
                    when (it) {
                        PaymentModel.PaymentType.QR_CODE -> {
                            payment.newPayment {
                                paymentType = it
                            }

                            val direction = AmountFragmentDirections.iswActionGotoFragmentQrCodeFragment(payment)
                            navigate(direction)
                        }
                        PaymentModel.PaymentType.PAY_CODE -> {
                            payment.newPayment {
                                paymentType = it
                            }

                            val direction = AmountFragmentDirections.iswActionGotoFragmentPayCode(payment)
                            navigate(direction)
                        }
                        PaymentModel.PaymentType.USSD -> {
                            payment.newPayment {
                                paymentType = it
                            }

                            val direction = AmountFragmentDirections.iswActionGotoFragmentUssd(payment)
                            navigate(direction)
                        }

                        PaymentModel.PaymentType.CARD -> {
                            payment.newPayment {
                                paymentType = it
                            }
                            val direction = AmountFragmentDirections.iswActionGotoFragmentCardTransactions(payment)
                            navigate(direction)
                        }

                        PaymentModel.PaymentType.CARD_NOT_PRESENT -> {

                            val fingerprintDialog = FingerprintBottomDialog(isAuthorization = true) { isValidated ->
                                if (isValidated) {
                                    payment.newPayment {
                                        paymentType = it
                                    }
                                    val direction = AmountFragmentDirections.iswActionGotoFragmentCardDetails(payment)
                                    navigate(direction)
                                } else {
                                    toast("Fingerprint Verification Failed!!")
                                    navigateUp()
                                }
                            }
                            val dialog = MerchantCardDialog(isUseCard = true) { type ->
                                when (type) {
                                    MerchantCardDialog.AUTHORIZED -> {payment.newPayment {
                                        paymentType = it
                                    }
                                        val direction = AmountFragmentDirections.iswActionGotoFragmentCardDetails(payment)
                                        navigate(direction)
                                    }
                                    MerchantCardDialog.FAILED -> {
                                        toast("Unauthorized Access!!")
                                        navigateUp()
                                    }
                                    MerchantCardDialog.USE_FINGERPRINT -> {
                                        fingerprintDialog.show(childFragmentManager, FingerprintBottomDialog.TAG)
                                    }
                                }
                            }
                            dialog.show(childFragmentManager, MerchantCardDialog.TAG)
                        }
                    }
                }
                bottomDialog.show(childFragmentManager, TAG)

            }

            PaymentModel.TransactionType.PRE_AUTHORIZATION -> {
                val direction = AmountFragmentDirections.iswActionGotoFragmentCardTransactions(payment)
                navigate(direction)
            }

            PaymentModel.TransactionType.COMPLETION -> {
               val direction =
                    AmountFragmentDirections.iswActionGotoFragmentCardTransactions(payment)
                navigate(direction)
            }

            PaymentModel.TransactionType.REFUND -> {
                val direction =
                    AmountFragmentDirections.iswActionGotoFragmentCardTransactions(payment)
                navigate(direction)
            }

            PaymentModel.TransactionType.ECHANGE -> {
                val direction = AmountFragmentDirections.iswActionIswFragmentAmountToIswPinfragment(payment)
                navigate(direction)
            }

            PaymentModel.TransactionType.ECASH -> {
                val direction = AmountFragmentDirections.iswActionIswFragmentAmountToIswPinfragment(payment)
                navigate(direction)
            }
            PaymentModel.TransactionType.CASH_OUT -> {
                val direction = AmountFragmentDirections.iswActionGotoFragmentCardTransactions(payment)
                navigate(direction)

            }
        }
    }

    private fun displayInvalidAmountToast() {
        toast("Enter a valid amount")
    }

    private fun initializeAmount() {
        amount = DEFAULT_AMOUNT
        isw_amount.text = amount
    }

    private fun updateAmount() {
        val cleanString = amount.replace("[$,.]".toRegex(), "")

        val parsed = java.lang.Double.parseDouble(cleanString)
        val numberFormat = NumberFormat.getInstance()
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
        val formatted = numberFormat.format(parsed / 100)

        isw_amount.text = formatted
    }

    private fun isAmountLimit(): Boolean {
        val cleanString = amount.replace("[$,.]".toRegex(), "")
        val parsed = java.lang.Double.parseDouble(cleanString)
        val parsedAmount = parsed/100

        return parsedAmount > AMOUNT_LIMIT
    }

    private fun handleClickWithAmountLimit(digit: String) {
        if(!isAmountLimit()) {
            amount+= digit
            updateAmount()
        } else {
            toast("Limit is $AMOUNT_LIMIT")
        }
    }

    private fun handleDigitsClicks() {
        isw_keypad_zero.setOnClickListener {
            handleClickWithAmountLimit("0")
        }

        isw_keypad_one.setOnClickListener {
            handleClickWithAmountLimit("1")
        }

        isw_keypad_two.setOnClickListener {
            handleClickWithAmountLimit("2")
        }

        isw_keypad_three.setOnClickListener {
            handleClickWithAmountLimit("3")
        }

        isw_keypad_four.setOnClickListener {
            handleClickWithAmountLimit("4")
        }

        isw_keypad_five.setOnClickListener {
            handleClickWithAmountLimit("5")
        }

        isw_keypad_six.setOnClickListener {
            handleClickWithAmountLimit("6")
        }

        isw_keypad_seven.setOnClickListener {
            handleClickWithAmountLimit("7")
        }

        isw_keypad_eight.setOnClickListener {
            handleClickWithAmountLimit("8")
        }

        isw_keypad_nine.setOnClickListener {
            handleClickWithAmountLimit("9")
        }

        isw_dot_button.setOnClickListener {
            handleClickWithAmountLimit(".")
        }

        isw_back_delete_button.setOnClickListener {
            if (amount.isNotEmpty()) {
                amount = amount.substring(0 until amount.length - 1)
                updateAmount()
            }
        }

        isw_back_delete_button.setOnLongClickListener {
            amount = DEFAULT_AMOUNT
            isw_amount.text = amount
            true
        }
    }

    companion object {
        const val TAG = "AMOUNT FRAGMENT"
    }
}