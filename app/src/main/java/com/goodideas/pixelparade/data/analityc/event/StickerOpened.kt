package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle
import com.goodideas.pixelparade.data.analityc.event.model.StickerType

class StickerOpened(
    private val packName: String,
    private val packSize: Int,
    private val stickerNumberInPack: Int,
    private val stickerType: StickerType
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Sticker_opened"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
        private const val STICKER_NUMBER_IN_PACK_PARAMETER_NAME = "sticker_number_in_a_pack"
        private const val STICKER_TYPE_PARAMETER_NAME = "sticker_type"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
            putInt(STICKER_NUMBER_IN_PACK_PARAMETER_NAME, stickerNumberInPack)
            putString(STICKER_TYPE_PARAMETER_NAME, stickerType.textValue)
        }
}
