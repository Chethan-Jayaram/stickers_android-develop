package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class GoHome(
    private val source: Source
) : AnalyticEvent() {

    enum class Source(val textValue: String) {
        MAIN("main"),
        PHOTO("photo")
    }

    companion object {
        private const val EVENT_NAME = "Go_home"
        private const val SOURCE_PARAMETER_NAME = "source"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(SOURCE_PARAMETER_NAME, source.textValue)
        }
}
