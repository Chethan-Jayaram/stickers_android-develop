package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class GetPack(
    private val packName: String,
    private val packSize: Int
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Get_pack"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
        }
}
