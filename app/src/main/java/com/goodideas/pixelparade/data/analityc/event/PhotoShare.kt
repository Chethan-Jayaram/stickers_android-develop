package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class PhotoShare : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Photo_share"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle()
}
