package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class EmailPopup : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Email_popup"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle()
}
