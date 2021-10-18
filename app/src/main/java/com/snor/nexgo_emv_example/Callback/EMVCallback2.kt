package com.snor.nexgo_emv_example.Callback

import android.util.Log
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.emv.*
import java.time.LocalDate

open class EMVCallback2 : OnEmvProcessListener2 {
    override fun onSelApp(
        p0: MutableList<String>?,
        p1: MutableList<CandidateAppInfoEntity>?,
        p2: Boolean
    ) {
        Log.e("dd--", "onSelApp")
    }

    override fun onTransInitBeforeGPO() {
        Log.e("dd--", "onTransInitBeforeGPO")
    }

    override fun onConfirmCardNo(p0: CardInfoEntity?) {
        Log.e("dd--", "onConfirmCardNo")
    }

    override fun onCardHolderInputPin(p0: Boolean, p1: Int) {
        Log.e("dd--", "onCardHolderInputPin")
    }

    override fun onContactlessTapCardAgain() {
        Log.e("dd--", "onContactlessTapCardAgain")
    }

    override fun onOnlineProc() {
        Log.e("dd--", "onOnlineProc")
    }

    override fun onPrompt(p0: PromptEnum?) {
        Log.e("dd--", "onPrompt")
        Log.e("dd--",p0.toString())
    }

    override fun onRemoveCard() {
        Log.e("dd--", "onRemoveCard")
    }

    override fun onFinish(p0: Int, p1: EmvProcessResultEntity?) {
        Log.e("dd--", "onFinish")
    }


}