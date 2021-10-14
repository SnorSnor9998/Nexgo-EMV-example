package com.snor.nexgo_emv_example.Callback

import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener

open class PinPadCallback : OnPinPadInputListener {
    override fun onInputResult(p0: Int, p1: ByteArray?) {
    }

    override fun onSendKey(p0: Byte) {
    }
}