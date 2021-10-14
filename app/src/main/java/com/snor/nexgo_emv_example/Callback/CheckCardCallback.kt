package com.snor.nexgo_emv_example.Callback

import android.util.Log
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener

open class CheckCardCallback : OnCardInfoListener {
    override fun onCardInfo(p0: Int, p1: CardInfoEntity?) {
    }

    override fun onSwipeIncorrect() {
        Log.e("dd--", "Bad EMV Swipe")
    }

    override fun onMultipleCards() {
        Log.e("dd--", "Multiple cards detected...")
    }
}