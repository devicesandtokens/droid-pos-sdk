package com.interswitchng.smartpos.modules.main.billPayment.repository

import com.interswitchng.smartpos.modules.main.billPayment.models.BillDisplayDataModel
import com.interswitchng.smartpos.modules.main.billPayment.models.BillPaymentCategoriesModel
import com.interswitchng.smartpos.modules.main.billPayment.models.NetworksModel
import com.interswitchng.smartpos.shared.Constants

class billRepo {

    fun getAirtimeBillers(): ArrayList<NetworksModel> {
        return Constants.NETWORKS_LIST
    }

    fun getCableTvProviders(): ArrayList<BillDisplayDataModel> {
        return Constants.CABLE_TV_PROVIDERS
    }

    fun getDstvPackages(): ArrayList<BillPaymentCategoriesModel> {
        return Constants.DSTV_PACKAGES
    }
}