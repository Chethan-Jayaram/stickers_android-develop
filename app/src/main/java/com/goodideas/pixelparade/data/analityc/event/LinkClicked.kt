package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class LinkClicked(
    private val packName: String,
    private val packSize: Int,
    private val link: String
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Link_clicked"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
        private const val LINK_PARAMETER_NAME = "link"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
            putString(LINK_PARAMETER_NAME, link)
        }
}
