package com.interswitchng.smartpos.modules.ussdqr.views

import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.ussdqr.adapters.BankListAdapter
import com.interswitchng.smartpos.shared.models.transaction.ussdqr.response.Bank
import kotlinx.android.synthetic.main.isw_select_bank_bottom_sheet.*

internal class SelectBankBottomSheet : BottomSheetDialogFragment() {

    private val adapter: BankListAdapter = BankListAdapter {
        if (proceedSelectBank.visibility != View.VISIBLE)
            proceedSelectBank.visibility = View.VISIBLE
    }

    private val _selectedBank = MutableLiveData<Bank>()
    val selectedBank: LiveData<Bank> get() = _selectedBank


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val sheetDialog = it as BottomSheetDialog
            val bottomSheet: FrameLayout? = sheetDialog.findViewById(android.support.design.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED)
        }

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.isw_select_bank_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        rvBankLists.layoutManager = GridLayoutManager(activity, 3)
        rvBankLists.adapter = adapter
        closeSheetButton.setOnClickListener { dismiss() }

        proceedSelectBank.setOnClickListener {
            adapter.selectedBank?.also {
                // set the selected bank
                _selectedBank.value = it
                dismiss()
            } ?: toast("Please select a Bank") // else prompt user to select bank
        }
    }

    fun loadBanks(banks: List<Bank>) {
        adapter.setBanks(banks)
        progressBarSelectBank.visibility = View.GONE
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance() = SelectBankBottomSheet()
    }
}