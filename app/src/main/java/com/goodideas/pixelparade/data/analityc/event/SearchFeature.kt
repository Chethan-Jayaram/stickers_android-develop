package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class SearchFeature : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Search_feature"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle()
}
