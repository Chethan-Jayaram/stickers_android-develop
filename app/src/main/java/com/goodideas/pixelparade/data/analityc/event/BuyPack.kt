package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class BuyPack(
    private val packName: String,
    private val packSize: Int,
    private val packCost: Int
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Buy_pack"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
        private const val PACK_COST_PARAMETER_NAME = "pack_cost"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
            putInt(PACK_COST_PARAMETER_NAME, packCost)
        }
}
