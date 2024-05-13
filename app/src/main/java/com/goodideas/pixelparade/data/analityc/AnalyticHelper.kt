package com.goodideas.pixelparade.data.analityc

import android.app.Application
import com.goodideas.pixelparade.data.analityc.event.AnalyticEvent
import com.goodideas.pixelparade.data.analityc.event.model.StickerType
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticHelper {

    companion object {
        private var firebaseAnalytics: FirebaseAnalytics? = null

        fun init(app: Application) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(app.applicationContext)
        }

        fun sendEvent(event: AnalyticEvent) {
            if (firebaseAnalytics == null) Timber.e("AnalyticHelper not initialized!")

            firebaseAnalytics?.logEvent(event.name, event.params)
        }

        fun getStickerTypeByFileName(fileName: String): StickerType {
            val extension = fileName.substring(fileName.lastIndexOf(".") + 1)
            for (stickerType in StickerType.values()) {
                if (stickerType.textValue == extension) return stickerType
            }
            Timber.e("Illegal file extension: $fileName")
            return StickerType.UNKNOWN
        }
    }
}
