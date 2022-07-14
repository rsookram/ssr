package io.github.rsookram.util

import android.widget.SeekBar

/**
 * Stub/no-op implementations of all methods of [SeekBar.OnSeekBarChangeListener].
 * Override this if you only care about a few of the available callback methods.
 */
open class SimpleSeekBarListener : SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(
        seekBar: SeekBar,
        progress: Int,
        fromUser: Boolean
    ) = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
}
