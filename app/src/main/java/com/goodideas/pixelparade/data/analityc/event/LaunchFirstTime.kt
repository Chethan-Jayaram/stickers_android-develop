package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class LaunchFirstTime : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Launch_first_time"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle()
}
