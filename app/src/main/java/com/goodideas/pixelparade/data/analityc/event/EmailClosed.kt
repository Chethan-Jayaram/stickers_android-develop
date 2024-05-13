package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

class EmailClosed(
    private val emailProvided: Boolean
) : AnalyticEvent() {

    companion object {
        private const val EVENT_NAME = "Email_closed"
        private const val EMAIL_PROVIDED_PARAMETER_NAME = "email_provided"
    }

    override val name: String
        get() = EVENT_NAME

    override val params: Bundle
        get() = Bundle().apply {
            putBoolean(EMAIL_PROVIDED_PARAMETER_NAME, emailProvided)
        }
}
