package io.github.rsookram.ssr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.rsookram.ssr.entity.ReadingMode
import io.github.rsookram.ssr.reader.Direction
import io.github.rsookram.ssr.reader.Reader
import io.github.rsookram.ssr.reader.ReaderViewModel
import io.github.rsookram.ssr.reader.ReaderViewState
import io.github.rsookram.ssr.reader.menu.ReaderMenuFragment
import io.github.rsookram.ssr.reader.view.MainView
import io.github.rsookram.util.enterImmersiveMode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var bookFile: File? = null
    private var reader: Reader? = null

    private val vm: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.enterImmersiveMode()

        val file = intent.getSerializableExtra(EXTRA_FILE) as File?
        if (file == null) {
            finish()
            return
        }
        bookFile = file

        vm.loadBook(file)

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

    private fun showMenu(file: File) {
        // TODO: Move away from fragments
        ReaderMenuFragment.newInstance(file).show(supportFragmentManager, null)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val file = bookFile

        if (keyCode == KeyEvent.KEYCODE_MENU && file != null) {
            showMenu(file)
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

    companion object {

        private const val EXTRA_FILE = "file"

        fun newIntent(context: Context, file: File) =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_FILE, file)
    }
}
