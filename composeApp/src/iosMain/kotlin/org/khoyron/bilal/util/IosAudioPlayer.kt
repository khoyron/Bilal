package org.khoyron.bilal.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.*
import platform.darwin.NSObjectProtocol

class IosAudioPlayer : AudioPlayer {
    private var player: AVQueuePlayer? = null
    private var onCompletion: (() -> Unit)? = null
    private var onUrlChange: ((String?) -> Unit)? = null
    private var observer: NSObjectProtocol? = null

    @OptIn(ExperimentalForeignApi::class)
    private fun setupAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayback,
            error = null
        )
        session.setActive(true, error = null)
    }

    private var observers: MutableList<NSObjectProtocol> = mutableListOf()

    override fun stop() {
        player?.pause()
        player?.removeAllItems()
        // ✅ remove semua observer
        observers.forEach {
            NSNotificationCenter.defaultCenter.removeObserver(it)
        }
        observers.clear()
    }

    override fun play(urls: List<String>) {
        stop()
        setupAudioSession()

        val items = urls.mapNotNull { url ->
            NSURL.URLWithString(url)?.let {
                AVPlayerItem.playerItemWithURL(it)
            }
        }

        if (items.isEmpty()) return

        player = AVQueuePlayer(items = items)
        onUrlChange?.invoke(urls.firstOrNull())

        items.forEachIndexed { index, item ->
            val obs = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = item,
                queue = NSOperationQueue.mainQueue
            ) { _ ->
                val nextUrl = urls.getOrNull(index + 1)
                if (nextUrl != null) {
                    onUrlChange?.invoke(nextUrl)
                } else {
                    onCompletion?.invoke()
                    onUrlChange?.invoke(null)
                }
            }
            obs?.let { observers.add(it) }  // ✅ simpan semua observer
        }

        player?.play()
    }


    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        player?.play()
    }


    @OptIn(ExperimentalForeignApi::class)
    override fun release() {
        stop()
        AVAudioSession.sharedInstance().setActive(false, error = null)
        player = null
    }

    override fun isPlaying(): Boolean {
        return (player?.rate ?: 0f) != 0.0f
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletion = listener
    }

    override fun setOnUrlChangeListener(listener: (String?) -> Unit) {
        onUrlChange = listener
    }
}
