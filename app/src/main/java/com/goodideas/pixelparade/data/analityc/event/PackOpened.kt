package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class PackOpened(
    private val packName: String,
    private val packSize: Int,
    private val source: Source
) : AnalyticEvent() {

    enum class Source(val textValue: String) {
        MAIN("main"),
        PHOTO("photo")
    }

    companion object {
        private const val EVENT_NAME = "Pack_opened"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
        private const val SOURCE_PARAMETER_NAME = "source"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
            putString(SOURCE_PARAMETER_NAME, source.textValue)
        }
}
