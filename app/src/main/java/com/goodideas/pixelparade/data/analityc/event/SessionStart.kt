package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class SessionStart : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Session_start"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle()
}
