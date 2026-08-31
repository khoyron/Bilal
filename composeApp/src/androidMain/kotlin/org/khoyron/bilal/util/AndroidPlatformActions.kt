package org.khoyron.bilal.util

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    override fun openMaps(
        destLat: Double,
        destLon: Double,
        label: String,
        fromLat: Double?,
        fromLon: Double?
    ) {
        val uri = if (fromLat != null && fromLon != null) {
            "https://www.google.com/maps/dir/?api=1&origin=$fromLat,$fromLon&destination=$destLat,$destLon"
        } else {
            "https://www.google.com/maps/search/?api=1&query=$destLat,$destLon"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
