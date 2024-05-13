package com.goodideas.pixelparade.html

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class GetBitmapFromUrl(private val onExecute: (Bitmap) -> Unit) : AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg params: String?): Bitmap? {
        return params[0]?.let { getBitmapFromURL(it) }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        result?.let { onExecute.invoke(result) }
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
