package com.goodideas.pixelparade.data.analityc.event

import android.os.Bundle

abstract class AnalyticEvent {

    abstract val name: String

    abstract val params: Bundle
}
