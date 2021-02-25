package com.interswitchng.smartpos.modules.main.transfer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.modules.main.transfer.models.BankModel as Bank

class BankAutoCompleteAdapter (context: Context, resource: Int, banks: ArrayList<Bank>): ArrayAdapter<Bank>(context, resource, banks) {
    val _resourceId = resource
    var _banks = banks
    var _bankList = banks

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView?: createView(position, parent)
    }

    private fun createView(position: Int, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(_resourceId, parent, false)
        view?.findViewById<TextView>(R.id.bank_display_name)?.text = _banks[position].bankName
        return view
    }

    override fun getFilter() = filter

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        convertView ?: LayoutInflater.from(context).inflate(_resourceId, parent, false)
        convertView?.findViewById<TextView>(R.id.bank_display_name)?.text = _banks[position].bankName
        return super.getDropDownView(position, convertView, parent)
    }

    override fun getCount() = _banks.size

    override fun getItem(position: Int) = _banks[position]

    private var filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = FilterResults()
            Log.d("TRackFiltering", constraint.toString())

            val query =
                    if (constraint != null && constraint.isNotEmpty()) autocomplete(constraint.toString())
                    else _bankList

            Log.d("Some Constriant", (constraint != null).toString())
            Log.d("Some Constriant", query.toString())

            results.values = query
            results.count = query.size

            return results
        }

        private fun autocomplete(input: String): ArrayList<Bank> {
            val results = arrayListOf<Bank>()

            for (bank in _bankList) {
                if (bank.bankName.toLowerCase().contains(input.toLowerCase()) || input == null) results.add(bank)
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
            _banks = results.values as ArrayList<Bank>
            notifyDataSetInvalidated()
        }

        override fun convertResultToString(result: Any) = (result as Bank).bankName
    }

    override fun notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated()
        Log.i("Invalidate", "it got invalid")
    }
}
