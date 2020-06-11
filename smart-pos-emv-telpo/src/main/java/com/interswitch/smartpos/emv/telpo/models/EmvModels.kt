package com.interswitch.smartpos.emv.telpo.models

import com.interswitchng.smartpos.shared.models.posconfig.EmvAIDs
import com.interswitchng.smartpos.shared.models.posconfig.EmvCapk
import com.interswitchng.smartpos.shared.models.posconfig.EmvCard
import com.interswitchng.smartpos.shared.models.posconfig.TerminalConfig
import com.telpo.emv.EmvApp
import com.telpo.emv.EmvCAPK
import com.telpo.tps550.api.util.StringUtil

fun EmvAIDs.getAllCapks(): List<EmvCAPK> {
    return cards.flatMap { card ->
        // use aid to get rid for keys
        val rid = card.aid.substring(0..9)
        return@flatMap card.keys.map { it.toEmvCAPK(rid) }
    }
}

fun EmvCapk.toEmvCAPK(rid: String): EmvCAPK {
    // get the key  index
    val keyId = Integer.parseInt(id, 16)

    return EmvCAPK().also {
        it.apply {
            RID = rid.hexStringToByteArray()
            KeyID = keyId.toByte()
            HashInd = 0x01.toByte()
            ArithInd = 0x01.toByte()
            Modul = modulus.hexStringToByteArray()
            Exponent = exponent.hexStringToByteArray()
            ExpDate = expiry.hexStringToByteArray()
            CheckSum = checksum.hexStringToByteArray()


            /*  RID = byteArrayOf(0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte())
              KeyID = 0x05.toByte()
              HashInd = 0x01.toByte()
              ArithInd = 0x01.toByte()
              Modul = byteArrayOf(0xB8.toByte(), 0x04.toByte(), 0x8A.toByte(), 0xBC.toByte(), 0x30.toByte(), 0xC9.toByte(), 0x0D.toByte(), 0x97.toByte(), 0x63.toByte(), 0x36.toByte(), 0x54.toByte(), 0x3E.toByte(), 0x3F.toByte(), 0xD7.toByte(), 0x09.toByte(), 0x1C.toByte(), 0x8F.toByte(), 0xE4.toByte(), 0x80.toByte(), 0x0D.toByte(), 0xF8.toByte(), 0x20.toByte(), 0xED.toByte(), 0x55.toByte(), 0xE7.toByte(), 0xE9.toByte(), 0x48.toByte(), 0x13.toByte(), 0xED.toByte(), 0x00.toByte(), 0x55.toByte(), 0x5B.toByte(), 0x57.toByte(), 0x3F.toByte(), 0xEC.toByte(), 0xA3.toByte(), 0xD8.toByte(), 0x4A.toByte(), 0xF6.toByte(), 0x13.toByte(), 0x1A.toByte(), 0x65.toByte(), 0x1D.toByte(), 0x66.toByte(), 0xCF.toByte(), 0xF4.toByte(), 0x28.toByte(), 0x4F.toByte(), 0xB1.toByte(), 0x3B.toByte(), 0x63.toByte(), 0x5E.toByte(), 0xDD.toByte(), 0x0E.toByte(), 0xE4.toByte(), 0x01.toByte(), 0x76.toByte(), 0xD8.toByte(), 0xBF.toByte(), 0x04.toByte(), 0xB7.toByte(), 0xFD.toByte(), 0x1C.toByte(), 0x7B.toByte(), 0xAC.toByte(), 0xF9.toByte(), 0xAC.toByte(), 0x73.toByte(), 0x27.toByte(), 0xDF.toByte(), 0xAA.toByte(), 0x8A.toByte(), 0xA7.toByte(), 0x2D.toByte(), 0x10.toByte(), 0xDB.toByte(), 0x3B.toByte(), 0x8E.toByte(), 0x70.toByte(), 0xB2.toByte(), 0xDD.toByte(), 0xD8.toByte(), 0x11.toByte(), 0xCB.toByte(), 0x41.toByte(), 0x96.toByte(), 0x52.toByte(), 0x5E.toByte(), 0xA3.toByte(), 0x86.toByte(), 0xAC.toByte(), 0xC3.toByte(), 0x3C.toByte(), 0x0D.toByte(), 0x9D.toByte(), 0x45.toByte(), 0x75.toByte(), 0x91.toByte(), 0x64.toByte(), 0x69.toByte(), 0xC4.toByte(), 0xE4.toByte(), 0xF5.toByte(), 0x3E.toByte(), 0x8E.toByte(), 0x1C.toByte(), 0x91.toByte(), 0x2C.toByte(), 0xC6.toByte(), 0x18.toByte(), 0xCB.toByte(), 0x22.toByte(), 0xDD.toByte(), 0xE7.toByte(), 0xC3.toByte(), 0x56.toByte(), 0x8E.toByte(), 0x90.toByte(), 0x02.toByte(), 0x2E.toByte(), 0x6B.toByte(), 0xBA.toByte(), 0x77.toByte(), 0x02.toByte(), 0x02.toByte(), 0xE4.toByte(), 0x52.toByte(), 0x2A.toByte(), 0x2D.toByte(), 0xD6.toByte(), 0x23.toByte(), 0xD1.toByte(), 0x80.toByte(), 0xE2.toByte(), 0x15.toByte(), 0xBD.toByte(), 0x1D.toByte(), 0x15.toByte(), 0x07.toByte(), 0xFE.toByte(), 0x3D.toByte(), 0xC9.toByte(), 0x0C.toByte(), 0xA3.toByte(), 0x10.toByte(), 0xD2.toByte(), 0x7B.toByte(), 0x3E.toByte(), 0xFC.toByte(), 0xCD.toByte(), 0x8F.toByte(), 0x83.toByte(), 0xDE.toByte(), 0x30.toByte(), 0x52.toByte(), 0xCA.toByte(), 0xD1.toByte(), 0xE4.toByte(), 0x89.toByte(), 0x38.toByte(), 0xC6.toByte(), 0x8D.toByte(), 0x09.toByte(), 0x5A.toByte(), 0xAC.toByte(), 0x91.toByte(), 0xB5.toByte(), 0xF3.toByte(), 0x7E.toByte(), 0x28.toByte(), 0xBB.toByte(), 0x49.toByte(), 0xEC.toByte(), 0x7E.toByte(), 0xD5.toByte(), 0x97.toByte());
              Exponent = byteArrayOf(0x00, 0x00, 0x03);
              ExpDate = byteArrayOf(0x24, 0x12, 0x31);
              CheckSum = byteArrayOf(0xEB.toByte(), 0xFA.toByte(), 0x0D.toByte(), 0x5D.toByte(), 0x06.toByte(), 0xD8.toByte(), 0xCE.toByte(), 0x70.toByte(), 0x2D.toByte(), 0xA3.toByte(), 0xEA.toByte(), 0xE8.toByte(), 0x90.toByte(), 0x70.toByte(), 0x1D.toByte(), 0x45.toByte(), 0xE2.toByte(), 0x74.toByte(), 0xC8.toByte(), 0x45.toByte());
          */
        }

    }
}

fun EmvCard.toAPPListItem(config: TerminalConfig): EmvApp {
    val acquirerId = "000000123456"
    val partialMatch = if (partialMatch) PART_MATCH else FULL_MATCH


    return EmvApp().also {
        it.apply {
            AppName = name.toByteArray()
            AID = StringUtil.toBytes(aid)
            SelFlag = partialMatch.toByte()
            FloorLimit = byteArrayOf(config.floorlimit.toByte())
            FloorLimitCheck = if (config.floorlimitcheck) 1 else 0
            TACDenial = StringUtil.toBytes(config.tacdenial)
            TACOnline = StringUtil.toBytes(config.taconline)
            TACDefault = StringUtil.toBytes(config.tacdefault)
            AcquierId = StringUtil.toBytes(acquirerId)
            DDOL = StringUtil.toBytes(config.ddol)
            Version = StringUtil.toBytes(config.version)
            TDOL = StringUtil.toBytes(config.tdol)

            // -----------------------------
            // unknown attributes config
            // -----------------------------
            Priority = 0
            Threshold = byteArrayOf(1.toByte())
            TargetPer = 0
            MaxTargetPer = 0
            RandTransSel = 1
            VelocityCheck = 1
        }
    }
}

private fun stringToByteArray(string: String): ByteArray {
    val splitString = string.split("-")
    val list = byteArrayOf()
    for (i in splitString) {
        list + i.toByte()
    }
    return list
}

fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

private val PART_MATCH = 0
private val FULL_MATCH = 1