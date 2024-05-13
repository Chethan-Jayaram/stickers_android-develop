package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle
import com.goodideas.pixelparade.data.analityc.event.model.StickerType

class QuickShare(
    private val packName: String,
    private val packSize: Int,
    private val stickerNumberInPack: Int,
    private val stickerType: StickerType,
    private val shareButtonName: String
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Quick_share"
        private const val PACK_NAME_PARAMETER_NAME = "pack_name"
        private const val PACK_SIZE_PARAMETER_NAME = "pack_size"
        private const val STICKER_NUMBER_IN_PACK_PARAMETER_NAME = "sticker_number_in_a_pack"
        private const val STICKER_TYPE_PARAMETER_NAME = "sticker_type"
        private const val SHARE_BUTTON_NAME_PARAMETER_NAME = "share_button_name"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(PACK_NAME_PARAMETER_NAME, packName)
            putInt(PACK_SIZE_PARAMETER_NAME, packSize)
            putInt(STICKER_NUMBER_IN_PACK_PARAMETER_NAME, stickerNumberInPack)
            putString(STICKER_TYPE_PARAMETER_NAME, stickerType.textValue)
            putString(SHARE_BUTTON_NAME_PARAMETER_NAME, shareButtonName)
        }
}
