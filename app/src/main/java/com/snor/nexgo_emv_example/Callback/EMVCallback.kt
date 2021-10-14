package com.snor.nexgo_emv_example.Callback

import android.util.Log
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.emv.*
import java.time.LocalDate

open class EMVCallback : OnEmvProcessListener {
    override fun onRequestAmount() {
        Log.e("dd--", "onRequestAmount")
    }

    override fun onSelApp(
        p0: MutableList<String>?,
        p1: MutableList<CandidateAppInfoEntity>?,
        p2: Boolean
    ) {
        Log.e("dd--", "onSelApp")
    }

    override fun onConfirmEcSwitch() {
        Log.e("dd--", "onConfirmEcSwitch")
    }

    override fun onConfirmCardNo(p0: CardInfoEntity?) {
        Log.e("dd--", "onConfirmCardNo")
    }

    override fun onCardHolderInputPin(p0: Boolean, p1: Int) {
        Log.e("dd--", "onCardHolderInputPin")
    }

    override fun onOnlineProc() {
        Log.e("dd--", "onOnlineProc")
    }

    override fun onReadCardAgain() {
        Log.e("dd--", "onReadCardAgain")
    }

    override fun onAfterFinalSelectedApp() {
        Log.e("dd--", "onAfterFinalSelectedApp")
    }

    override fun onPrompt(p0: PromptEnum?) {
        Log.e("dd--", "onPrompt")
    }

    override fun onCertVerify(p0: String?, p1: String?) {
        Log.e("dd--", "onCertVerify")
    }

    override fun onRemoveCard() {
        Log.e("dd--", "onRemoveCard")
    }

    override fun onFinish(p0: Int, p1: EmvProcessResultEntity?) {
        Log.e("dd--", "onFinish")
    }


}