package org.khoyron.bilal.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.UIKit.UIWindow

class IosPlatformActions : PlatformActions {
    override fun shareText(text: String) {
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        if (rootViewController != null) {
            val activityViewController = UIActivityViewController(listOf(text), null)
            rootViewController.presentViewController(activityViewController, animated = true, completion = null)
        }
    }

    override fun openMaps(
        destLat: Double,
        destLon: Double,
        label: String,
        fromLat: Double?,
        fromLon: Double?
    ) {
        val urlString = if (fromLat != null && fromLon != null) {
            "http://maps.apple.com/?saddr=$fromLat,$fromLon&daddr=$destLat,$destLon"
        } else {
            "http://maps.apple.com/?q=$destLat,$destLon"
        }
        val url = NSURL(string = urlString)
        if (url != null) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
