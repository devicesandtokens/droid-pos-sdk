package com.interswitchng.smartpos.modules.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.dialogs.FingerprintBottomDialog
import com.interswitchng.smartpos.modules.main.dialogs.MerchantCardDialog
import com.interswitchng.smartpos.shared.activities.BaseFragment
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.utilities.DialogUtils
import kotlinx.android.synthetic.main.isw_settings_home.*
import org.koin.android.ext.android.inject

class SettingsHomeFragment : BaseFragment(TAG) {

    override val layoutId: Int
        get() = R.layout.isw_settings_home

    private val store: KeyValueStore by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.isw_settings_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //isw_settings_page_switch.isChecked = store.getBoolean("SETTINGS_PAGE")

        handleClicks()
    }

    private fun handleClicks() {
        isw_account_container.setOnClickListener {
            authorizeAndPerformAction { it.findNavController().navigate(R.id.isw_goto_account_fragment_action) }
        }

        isw_settings_toolbar_label.setOnClickListener {
            navigateUp()
        }

        isw_download_settings_btn.setOnClickListener {
            authorizeAndPerformAction { iswPos.goToSettingsUpdatePage() }
            //iswPos.goToSettingsUpdatePage()
        }

        /*   isw_settings_page_switch.setOnCheckedChangeListener { button, _ ->

               if (button.isChecked) {
                   store.saveBoolean("SETUP",false)
               }
               else{
                   store.saveBoolean("SETUP",true)
               }

               store.saveBoolean("SETTINGS_PAGE", button.isChecked)
           }*/

    }

    private lateinit var dialog: MerchantCardDialog
    private  fun performOperation(){

    }




    //authorizeAndPerformAction { it.findNavController().navigate(R.id.isw_goto_account_fragment_action) }
    private fun authorizeAndPerformAction(action: () -> Unit) {
        val fingerprintDialog = FingerprintBottomDialog (isAuthorization = true) { isValidated ->
            if (isValidated) {
                store.saveBoolean("SETUP", false)
                action.invoke()
            } else {
                toast("Unauthorized Access!!")
            }
        }

        val alert by lazy {
            DialogUtils.getAlertDialog(requireContext())
                    .setTitle("Invalid Configuration")
                    .setMessage("The configuration contains invalid parameters, please fix the errors and try saving again")
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->

                        dialog.dismiss()
                        action.invoke()
                    }
        }
         dialog = MerchantCardDialog {
            when (it) {
                MerchantCardDialog.AUTHORIZED -> {
                    store.saveBoolean("SETUP", false)
                    action.invoke()
                }
                MerchantCardDialog.FAILED -> toast("Unauthorized Access!!")
//                MerchantCardDialog.NOT_ENROLLED -> { alert.setTitle("Supervisor's card not enrolled")
//                    alert.setMessage("You have not yet enrolled a supervisor's card. Please enroll a supervisor's card on the settings page after downloading terminal configuration.")
//                    alert.show()
//
//                }


                MerchantCardDialog.USE_FINGERPRINT -> fingerprintDialog.show(requireFragmentManager(), FingerprintBottomDialog.TAG)
            }
        }
        dialog.show(requireFragmentManager(), MerchantCardDialog.TAG)
    }

    companion object {
        const val TAG = "Setting Home Fragment"
    }
}
