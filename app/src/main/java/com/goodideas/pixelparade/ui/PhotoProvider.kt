package com.goodideas.pixelparade.ui

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import java.io.File

class PhotoProvider : ContentProvider() {
    companion object {
        private const val CONTENT_PROVIDER_AUTHORITY = "com.goodideas.pixelparade.ui.PhotoProvider";

        fun getPhotoUri(file: Uri): Uri {
            val builder = Uri.Builder()
                    .authority(CONTENT_PROVIDER_AUTHORITY)
                    .scheme("file")
                    .path(file.path)
                    .query(file.query)
                    .fragment(file.fragment)

            return builder.build()
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }
}