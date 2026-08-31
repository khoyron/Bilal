package org.khoyron.bilal.util

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private var exoPlayer: ExoPlayer? = null
    private var onCompletion: (() -> Unit)? = null
    private var onUrlChange: ((String?) -> Unit)? = null

    private fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onCompletion?.invoke()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        onUrlChange?.invoke(mediaItem?.mediaId)
                    }
                })
            }
        }
        return exoPlayer!!
    }

    override fun play(urls: List<String>) {
        getPlayer().apply {
            stop()
            clearMediaItems()
            val items = urls.map { url ->
                MediaItem.Builder()
                    .setUri(url)
                    .setMediaId(url)
                    .build()
            }
            addMediaItems(items)
            prepare()
            playWhenReady = true
        }
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun resume() {
        exoPlayer?.play()
    }

    override fun stop() {
        exoPlayer?.stop()
    }

    override fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        this.onCompletion = listener
    }

    override fun setOnUrlChangeListener(listener: (String?) -> Unit) {
        this.onUrlChange = listener
    }
}
