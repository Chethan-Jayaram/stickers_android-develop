package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class SearchResults(
    private val input: String
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Search_results"
        private const val INPUT_PARAMETER_NAME = "input"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putString(INPUT_PARAMETER_NAME, input)
        }
}
