package com.interswitchng.smartpos.emv.pax.models

import com.interswitchng.smartpos.emv.pax.utilities.EmvUtils
import com.interswitchng.smartpos.emv.pax.utilities.EmvUtils.str2Bcd
import com.pax.jemv.clcommon.EMV_APPLIST
import com.pax.jemv.clcommon.EMV_CAPK

internal class EmvAIDs {
    var cards: ArrayList<EmvCard> = arrayListOf()


    fun getCapks(): List<EMV_CAPK> {
        return cards.flatMap { card ->
            // use aid to get rid for keys
            val rid = card.aid.substring(0..9)
            return@flatMap card.keys.map { it.toCapk(rid) }
        }
    }
}

internal class EmvCard {
    var name: String = ""
    var aid: String = ""
    var partialMatch: Boolean = false
    var keys: ArrayList<EmvCapk> = arrayListOf()



    /// function to perform conversion from CARD AID TO APP LIST
    fun toAPPListItem(config: TerminalConfig): EMV_APPLIST {
        val acquirerId = "000000123456"
        val partialMatch = if (partialMatch) PART_MATCH else FULL_MATCH


        return EMV_APPLIST().also {
            it.appName = name.toByteArray()
            it.aid = aid.let(EmvUtils::str2Bcd)

            it.aidLen = it.aid.size.toByte()
            it.selFlag = partialMatch.toByte()
            it.floorLimit = config.floorlimit.toLong()
            it.floorLimitCheck = if (config.floorlimitcheck) 1 else 0
            it.tacDenial = config.tacdenial.let(EmvUtils::str2Bcd)
            it.tacOnline = config.taconline.let(EmvUtils::str2Bcd)
            it.tacDefault = config.tacdefault.let(EmvUtils::str2Bcd)
            it.acquierId = acquirerId.let(EmvUtils::str2Bcd)
            it.dDOL = config.ddol.let(EmvUtils::str2Bcd)
            it.version = config.version.let(EmvUtils::str2Bcd)
            it.tDOL = config.tdol.let(EmvUtils::str2Bcd)

            // -----------------------------
            // unknown attributes config
            // -----------------------------
            it.priority = 0
            it.threshold = 1
            it.targetPer = 0
            it.maxTargetPer = 0
            it.randTransSel = 1
            it.velocityCheck = 1
        }
    }
}


internal class EmvCapk {
        var id: String = ""
        var expiry: String = ""
        var modulus: String = ""
        var exponent: String = ""
        var checksum: String = ""



    // function to get CAPKs
    fun toCapk(rid: String): EMV_CAPK {
        // get the key  index
        val keyId = Integer.parseInt(id, 16)

        return EMV_CAPK().also {
            it.rID = str2Bcd(rid)
            it.keyID = keyId.toByte()
            it.hashInd = 0x01
            it.arithInd = 0x01
            it.modul = str2Bcd(modulus)
            it.modulLen = it.modul.size.toShort()
            it.exponent = str2Bcd(exponent)
            it.exponentLen = it.exponent.size.toByte()
            it.expDate = str2Bcd(expiry)
            it.checkSum = str2Bcd(checksum)
        }
    }

}


private val PART_MATCH = 0
private val FULL_MATCH = 1
