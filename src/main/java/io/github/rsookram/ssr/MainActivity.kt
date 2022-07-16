package io.github.rsookram.ssr

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.reader.Direction
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewModel
import io.github.rsookram.ssr.reader.ReaderViewState
import io.github.rsookram.ssr.reader.menu.ReaderMenuDialog
import io.github.rsookram.ssr.reader.view.MainView
import io.github.rsookram.util.enterImmersiveMode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    private var bookUri: Uri? = null
    private var reader: Reader? = null

    private val vm: ReaderViewModel by viewModels()

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.enterImmersiveMode()

        val uri = intent.data
        if (uri == null) {
            finish()
            return
        }
        bookUri = uri

        vm.loadBook(uri)

        val view = MainView(findViewById(android.R.id.content), vm)

        var lastState: ReaderViewState? = null
        vm.states
            .onEach { state ->
                val newMode = state.book?.mode ?: ReadingMode.SCROLL_VERTICAL
                val currentReader = if (newMode != lastState?.book?.mode) {
                    view.createReader(newMode)
                } else {
                    reader ?: view.createReader(newMode)
                }

                reader = currentReader
                lastState = state

                if (state.pages != null) {
                    currentReader.bind(state)
                }
            }
            .launchIn(lifecycleScope)

        vm.menuShows.observe(this, this::showMenu)
    }

    private fun showMenu(uri: Uri) {
        dialog?.dismiss()
        dialog = ReaderMenuDialog(uri).show(this)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val uri = bookUri

        if (keyCode == KeyEvent.KEYCODE_MENU && uri != null) {
            showMenu(uri)
            return true
        }

        val direction = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> Direction.LEFT
            KeyEvent.KEYCODE_DPAD_UP -> Direction.UP
            KeyEvent.KEYCODE_DPAD_RIGHT -> Direction.RIGHT
            KeyEvent.KEYCODE_DPAD_DOWN -> Direction.DOWN
            else -> null
        } ?: return super.onKeyUp(keyCode, event)

        reader?.moveTo(direction)

        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.enterImmersiveMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }
}
