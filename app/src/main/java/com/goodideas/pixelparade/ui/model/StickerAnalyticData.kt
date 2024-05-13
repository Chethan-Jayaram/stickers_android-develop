package com.goodideas.pixelparade.ui.model

import java.io.Serializable

data class StickerAnalyticData(
    val packName: String,
    val packSize: Int,
    val stickerNumberInPack: Int
) : Serializable
