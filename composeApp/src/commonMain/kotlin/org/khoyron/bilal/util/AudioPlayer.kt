package org.khoyron.bilal.util

interface AudioPlayer {
    fun play(urls: List<String>)
    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
    fun setOnUrlChangeListener(listener: (String?) -> Unit)
}
